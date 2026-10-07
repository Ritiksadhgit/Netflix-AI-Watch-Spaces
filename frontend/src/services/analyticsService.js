import { apiClient } from './apiClient';

export const analyticsService = {
  getSessionAnalytics: async (watchSpaceId) => {
    return apiClient(`/api/v1/analytics/session/${watchSpaceId}`);
  },

  reconcileSessionAnalytics: async (watchSpaceId) => {
    return apiClient(`/api/v1/analytics/session/${watchSpaceId}/reconcile`, {
      method: 'POST',
    });
  },

  getDashboardStats: async () => {
    return apiClient('/api/v1/analytics/dashboard');
  },
};
