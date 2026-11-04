import {
  useMemo,
  useState
} from "react";

import AuthContext from "./AuthContext";
import { login as loginRequest } from "../api/authApi";

function readStoredUser() {
  const storedUser =
    sessionStorage.getItem("currentUser");

  if (!storedUser) {
    return null;
  }

  try {
    return JSON.parse(storedUser);
  } catch {
    sessionStorage.removeItem("currentUser");
    sessionStorage.removeItem("accessToken");

    return null;
  }
}

export default function AuthProvider({
  children
}) {
  const [user, setUser] =
    useState(readStoredUser);

  async function login(username, password) {
    const response = await loginRequest({
      username,
      password
    });

    const currentUser = {
      username: response.username,
      role: response.role,
      customerId: response.customerId
    };

    sessionStorage.setItem(
      "accessToken",
      response.accessToken
    );

    sessionStorage.setItem(
      "currentUser",
      JSON.stringify(currentUser)
    );

    setUser(currentUser);

    return currentUser;
  }

  function logout() {
    sessionStorage.removeItem("accessToken");
    sessionStorage.removeItem("currentUser");

    setUser(null);
  }

  const contextValue = useMemo(
    () => ({
      user,
      authenticated: Boolean(user),
      login,
      logout
    }),
    [user]
  );

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
}