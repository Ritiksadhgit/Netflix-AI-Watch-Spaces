import { tokenStorage } from '../utils/tokenStorage';

const API_BASE_URL = import.meta.env.VITE_API_URL;

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) prom.reject(error);
    else prom.resolve(token);
  });

  failedQueue = [];
};

const buildUrl = (endpoint) => {
  if (endpoint.startsWith('http')) {
    return endpoint;
  }

  return `${API_BASE_URL}${endpoint}`;
};

export async function apiClient(endpoint, options = {}) {
  const { headers = {}, body, ...customConfig } = options;

  const token = tokenStorage.getAccessToken();

  const defaultHeaders = {
    'Content-Type': 'application/json',
    'X-Correlation-Id': `web_${Math.random().toString(36).substring(2, 10)}`,
  };

  if (token) {
    defaultHeaders['Authorization'] = `Bearer ${token}`;
  }

  const config = {
    method: body ? 'POST' : 'GET',
    ...customConfig,
    headers: {
      ...defaultHeaders,
      ...headers,
    },
  };

  if (body) {
    config.body =
      typeof body === 'string'
        ? body
        : JSON.stringify(body);
  }

  let response;

  try {
    response = await fetch(buildUrl(endpoint), config);
  } catch (err) {
    throw {
      title: 'Network Error',
      detail: 'Unable to connect to the server. Please check your network.',
      status: 0,
    };
  }

  // Handle 401 Unauthorized
  if (
    response.status === 401 &&
    !endpoint.includes('/api/v1/auth/')
  ) {
    const refreshToken = tokenStorage.getRefreshToken();

    if (!refreshToken) {
      tokenStorage.clearAll();
      window.dispatchEvent(new Event('auth:unauthorized'));
      throw await parseError(response);
    }

    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      }).then(newToken => {
        config.headers['Authorization'] = `Bearer ${newToken}`;

        return fetch(buildUrl(endpoint), config).then(res =>
          res.ok ? res.json() : parseError(res)
        );
      });
    }

    isRefreshing = true;

    try {
      const refreshRes = await fetch(
        buildUrl('/api/v1/auth/refresh'),
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ refreshToken }),
        }
      );

      if (!refreshRes.ok) {
        throw new Error('Refresh token expired');
      }

      const refreshData = await refreshRes.json();

      tokenStorage.setAccessToken(refreshData.accessToken);

      if (refreshData.refreshToken) {
        tokenStorage.setRefreshToken(refreshData.refreshToken);
      }

      processQueue(null, refreshData.accessToken);
      isRefreshing = false;

      // Retry original request
      config.headers['Authorization'] =
        `Bearer ${refreshData.accessToken}`;

      const retryResponse = await fetch(
        buildUrl(endpoint),
        config
      );

      return retryResponse.ok
        ? retryResponse.json()
        : parseError(retryResponse);

    } catch (refreshErr) {
      processQueue(refreshErr, null);

      isRefreshing = false;

      tokenStorage.clearAll();

      window.dispatchEvent(
        new Event('auth:unauthorized')
      );

      throw {
        title: 'Session Expired',
        detail: 'Your session has expired. Please sign in again.',
        status: 401,
      };
    }
  }

  if (response.status === 204) {
    return null;
  }

  if (!response.ok) {
    throw await parseError(response);
  }

  const contentType = response.headers.get('content-type');

  if (
    contentType &&
    contentType.includes('application/json')
  ) {
    return await response.json();
  }

  return await response.text();
}

async function parseError(response) {
  try {
    const data = await response.json();

    return {
      title: data.title || response.statusText,
      detail: data.detail || 'An error occurred',
      status: response.status,
      code: data.code,
      validationErrors: data.validationErrors,
    };

  } catch {
    return {
      title: response.statusText,
      detail: `Request failed with status ${response.status}`,
      status: response.status,
    };
  }
}