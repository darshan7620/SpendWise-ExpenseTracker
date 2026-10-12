import { api } from './client'

export const reportsApi = {
  monthly: (params) => api.get('/reports/monthly', { params }).then((r) => r.data),
  yearly: (params) => api.get('/reports/yearly', { params }).then((r) => r.data),
}
