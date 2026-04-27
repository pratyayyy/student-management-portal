import axios from 'axios';

const api = axios.create({
  baseURL: '/',
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.response.use(
  res => res,
  err => {
    // Don't do a hard redirect on 401 — React components (ProtectedRoute,
    // RootRedirect) handle auth navigation via React Router.  A
    // window.location.href here would cause an infinite reload loop because
    // AuthContext.fetchMe() legitimately receives 401 when the user isn't
    // logged in yet.
    return Promise.reject(err);
  }
);

export default api;

export const authService = {
  login: (username, password) =>
    api.post('/api/auth/login', { username, password }),
  logout: () => api.post('/api/auth/logout'),
  me: () => api.get('/api/auth/me'),
  register: (data) => api.post('/api/auth/register', data),
};

export const studentService = {
  search: (query = '', page = 0, pageSize = 10) =>
    api.get('/api/students/search', { params: { query, page, pageSize } }),
  paginated: (page = 0, pageSize = 10) =>
    api.get('/api/students/paginated', { params: { page, pageSize } }),
  getById: (id) => api.get(`/api/students/${id}`),
  create: (data) => api.post('/api/students', data),
  update: (id, data) => api.put(`/api/students/${id}`, data),
  delete: (id) => api.delete(`/api/students/${id}`),
  uploadPicture: (studentId, file) => {
    const form = new FormData();
    form.append('file', file);
    return api.post(`/api/students/${studentId}/upload-picture`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  deletePicture: (studentId) => api.delete(`/api/students/${studentId}/delete-picture`),
  profilePicUrl: (studentId) => `/api/students/${studentId}/profile-picture`,
};

export const adminService = {
  stats: () => api.get('/api/admin/stats'),
};

export const feeService = {
  getByStudent: (studentId) => api.get(`/api/fees/student/${studentId}`),
  create: (data) => api.post('/api/fees', data),
};

export const bulkImportService = {
  upload: (file) => {
    const form = new FormData();
    form.append('file', file);
    return api.post('/api/bulk-import/upload', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  templateInfo: () => api.get('/api/bulk-import/template-info'),
};
