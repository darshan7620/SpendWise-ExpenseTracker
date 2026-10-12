import { api } from './client'

export const budgetsApi = {
  list: () => api.get('/budgets').then((r) => r.data),
  get: (id) => api.get(`/budgets/${id}`).then((r) => r.data),
  create: (payload) => api.post('/budgets', payload).then((r) => r.data),
  update: (id, payload) => api.put(`/budgets/${id}`, payload).then((r) => r.data),
  remove: (id) => api.delete(`/budgets/${id}`).then((r) => r.data),
}
