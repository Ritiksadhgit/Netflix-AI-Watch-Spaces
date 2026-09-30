import { apiClient } from './apiClient';

export const authService = {
  register: async (payload) => {
    return await apiClient('/api/v1/auth/register', {
      method: 'POST',
      body: payload,
    });
  },

  login: async (payload) => {
    return await apiClient('/api/v1/auth/login', {
      method: 'POST',
      body: payload,
    });
  },

  refreshToken: async (refreshToken) => {
    return await apiClient('/api/v1/auth/refresh', {
      method: 'POST',
      body: { refreshToken },
    });
  },

  logout: async () => {
    try {
      await apiClient('/api/v1/auth/logout', { method: 'POST' });
    } catch {
      // Ignore errors on logout
    }
  },

  getMe: async () => {
    return await apiClient('/api/v1/auth/me');
  },

  updateProfile: async (payload) => {
    return await apiClient('/api/v1/auth/profile', {
      method: 'PUT',
      body: payload,
    });
  },
};
