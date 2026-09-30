const ACCESS_TOKEN_KEY = 'watchspaces_access_token';
const REFRESH_TOKEN_KEY = 'watchspaces_refresh_token';
const USER_KEY = 'watchspaces_user';

export const tokenStorage = {
  getAccessToken: () => localStorage.getItem(ACCESS_TOKEN_KEY),
  setAccessToken: (token) => {
    if (token) localStorage.setItem(ACCESS_TOKEN_KEY, token);
    else localStorage.removeItem(ACCESS_TOKEN_KEY);
  },

  getRefreshToken: () => localStorage.getItem(REFRESH_TOKEN_KEY),
  setRefreshToken: (token) => {
    if (token) localStorage.setItem(REFRESH_TOKEN_KEY, token);
    else localStorage.removeItem(REFRESH_TOKEN_KEY);
  },

  getUser: () => {
    try {
      const data = localStorage.getItem(USER_KEY);
      return data ? JSON.parse(data) : null;
    } catch {
      return null;
    }
  },
  setUser: (user) => {
    if (user) localStorage.setItem(USER_KEY, JSON.stringify(user));
    else localStorage.removeItem(USER_KEY);
  },

  clearAll: () => {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }
};
