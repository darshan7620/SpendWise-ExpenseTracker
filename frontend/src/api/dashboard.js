import { api } from './client'

export const dashboardApi = {
  summary: (params) => api.get('/dashboard/summary', { params }).then((r) => r.data),
  monthlyTrends: (months) =>
    api.get('/dashboard/monthly-trends', { params: { months } }).then((r) => r.data),
  categoryBreakdown: (params) =>
    api.get('/dashboard/category-breakdown', { params }).then((r) => r.data),
}
