import axios from 'axios'

const http = axios.create({ baseURL: '/api', timeout: 20000 })

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error?.response?.data?.message || error?.message || '请求失败'
    return Promise.reject(new Error(message))
  }
)

async function call(promise) {
  const response = await promise
  const body = response.data
  if (!body || body.ok === false) {
    throw new Error((body && body.message) || '请求失败')
  }
  return body.data
}

const params = (o) => ({ params: o })

export default {
  // ---------------- 押运任务 ----------------
  listTasks: () => call(http.get('/escort-tasks')),
  createTask: (d) => call(http.post('/escort-tasks', d)),
  updateTask: (id, d) => call(http.put(`/escort-tasks/${id}`, d)),
  transferTask: (id, status) => call(http.post(`/escort-tasks/${id}/status`, null, params({ status }))),

  // ---------------- 运钞车 ----------------
  listVehicles: () => call(http.get('/vehicles')),
  createVehicle: (d) => call(http.post('/vehicles', d)),
  updateVehicle: (id, d) => call(http.put(`/vehicles/${id}`, d)),
  transferVehicle: (id, status) => call(http.post(`/vehicles/${id}/status`, null, params({ status }))),

  // ---------------- 押运员 ----------------
  listMembers: () => call(http.get('/members')),
  createMember: (d) => call(http.post('/members', d)),
  updateMember: (id, d) => call(http.put(`/members/${id}`, d)),
  transferMember: (id, status) => call(http.post(`/members/${id}/status`, null, params({ status }))),

  // ---------------- 押运线路 ----------------
  listRoutes: () => call(http.get('/routes')),
  createRoute: (d) => call(http.post('/routes', d)),
  updateRoute: (id, d) => call(http.put(`/routes/${id}`, d)),
  transferRoute: (id, status) => call(http.post(`/routes/${id}/status`, null, params({ status })))
}
