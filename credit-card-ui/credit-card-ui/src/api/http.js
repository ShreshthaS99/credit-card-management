import axios from "axios";

const baseURL =
  import.meta.env.VITE_API_BASE_URL;

export const http = axios.create({
  baseURL,
  timeout: 15000,
  headers: {
    "Content-Type": "application/json"
  }
});

http.interceptors.request.use(
  config => {
    const token =
      sessionStorage.getItem(
        "accessToken"
      );

    if (token) {
      config.headers.Authorization =
        `Bearer ${token}`;
    }

    return config;
  },

  error => {
    return Promise.reject(error);
  }
);

http.interceptors.response.use(
  response => response,

  error => {
    const normalizedError = {
      status:
        error.response?.status ?? 0,

      message:
        error.response?.data?.message ??
        "Unable to connect to backend.",

      fieldErrors:
        error.response?.data?.fieldErrors ??
        {}
    };

    return Promise.reject(
      normalizedError
    );
  }
);