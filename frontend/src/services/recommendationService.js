import { apiClient } from './apiClient';

export const recommendationService = {
  getRecommendations: async () => {
    return apiClient('/api/v1/recommendations');
  },

  getCategorizedRecommendations: async () => {
    return apiClient('/api/v1/recommendations/categories');
  },
};
