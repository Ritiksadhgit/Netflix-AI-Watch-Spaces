import { apiClient } from './apiClient';

export const titleService = {
  getAllTitles: async () => {
    return apiClient('/api/v1/titles');
  },

  getTitleById: async (id) => {
    return apiClient(`/api/v1/titles/${id}`);
  },

  getTitleTimeline: async (titleId) => {
    return apiClient(`/api/v1/titles/${titleId}/timeline`);
  },
};
