import { apiClient } from './apiClient';

export const watchSpaceService = {
  createWatchSpace: async (payload) => {
    return await apiClient('/api/v1/watch-spaces', {
      method: 'POST',
      body: payload,
    });
  },

  getWatchSpace: async (id) => {
    return await apiClient(`/api/v1/watch-spaces/${id}`);
  },

  getActiveSpaces: async () => {
    return await apiClient('/api/v1/watch-spaces');
  },

  joinByCode: async (inviteCode) => {
    return await apiClient('/api/v1/watch-spaces/join-by-code', {
      method: 'POST',
      body: { inviteCode },
    });
  },
};
