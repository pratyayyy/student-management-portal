import axios from 'axios';

const api = axios.create({
  baseURL: '/',
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      window.location.href = '/login';
    }
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
  uploadPicture: (studentId, file) => {
    const form = new FormData();
    form.append('file', file);
    return api.post(`/students/${studentId}/upload-picture`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  deletePicture: (studentId) => api.delete(`/students/${studentId}/delete-picture`),
  profilePicUrl: (studentId) => `/students/${studentId}/profile-picture`,
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
  templateInfo: () => api.get('/bulk-import/template-info'),
};
