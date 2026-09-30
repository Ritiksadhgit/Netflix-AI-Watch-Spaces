import { apiClient } from './apiClient';

export const adminTimelineService = {
  getTimeline: async (titleId) => {
    return apiClient(`/api/v1/admin/titles/${titleId}/timeline`);
  },

  createMarker: async (titleId, markerData) => {
    return apiClient(`/api/v1/admin/titles/${titleId}/timeline/events`, {
      method: 'POST',
      body: JSON.stringify(markerData),
    });
  },

  updateMarker: async (titleId, eventId, markerData) => {
    return apiClient(`/api/v1/admin/titles/${titleId}/timeline/events/${eventId}`, {
      method: 'PUT',
      body: JSON.stringify(markerData),
    });
  },

  deleteMarker: async (titleId, eventId) => {
    return apiClient(`/api/v1/admin/titles/${titleId}/timeline/events/${eventId}`, {
      method: 'DELETE',
    });
  },

  importTimeline: async (titleId, events, replaceExisting = false) => {
    return apiClient(`/api/v1/admin/titles/${titleId}/timeline/import`, {
      method: 'POST',
      body: JSON.stringify({
        events,
        replaceExisting,
      }),
    });
  },

  exportTimeline: async (titleId) => {
    return apiClient(`/api/v1/admin/titles/${titleId}/timeline/export`);
  },
};
