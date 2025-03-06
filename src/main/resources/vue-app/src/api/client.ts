import axios from 'axios';

// Create a base API client with default configuration
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
  timeout: 10000, // 10 seconds
});

// Request interceptor for adding auth token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for handling common errors
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // Handle 401 Unauthorized errors (token expired)
    if (error.response && error.response.status === 401) {
      // Clear local storage and redirect to login
      localStorage.removeItem('auth_token');
      window.location.href = '/login';
    }
    
    // Handle 403 Forbidden errors
    if (error.response && error.response.status === 403) {
      console.error('Permission denied');
    }
    
    // Handle 500 server errors
    if (error.response && error.response.status >= 500) {
      console.error('Server error occurred');
    }
    
    return Promise.reject(error);
  }
);

export default apiClient; 