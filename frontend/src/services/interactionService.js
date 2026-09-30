import { apiClient } from './apiClient';

export const interactionService = {
  recordInteraction: async (titleId, watchedSeconds, completed = false, rating = null) => {
    return apiClient('/api/v1/interactions', {
      method: 'POST',
      body: JSON.stringify({
        titleId,
        watchedSeconds,
        completed,
        rating,
      }),
    });
  },

  getWatchHistory: async () => {
    return apiClient('/api/v1/interactions/history');
  },
};
