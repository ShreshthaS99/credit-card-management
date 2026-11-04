import { http } from "./http";

export async function login(credentials) {
  const response = await http.post(
    "/api/auth/login",
    credentials
  );

  return response.data;
}

export async function getCurrentUser() {
  const response = await http.get(
    "/api/auth/me"
  );

  return response.data;
}
