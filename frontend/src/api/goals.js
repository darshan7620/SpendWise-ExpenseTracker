import { api } from './client'

export const goalsApi = {
  list: () => api.get('/goals').then((r) => r.data),
  get: (id) => api.get(`/goals/${id}`).then((r) => r.data),
  create: (payload) => api.post('/goals', payload).then((r) => r.data),
  update: (id, payload) => api.put(`/goals/${id}`, payload).then((r) => r.data),
  updateProgress: (id, amountToAdd) =>
    api.patch(`/goals/${id}/progress`, { amountToAdd }).then((r) => r.data),
  remove: (id) => api.delete(`/goals/${id}`).then((r) => r.data),
}
