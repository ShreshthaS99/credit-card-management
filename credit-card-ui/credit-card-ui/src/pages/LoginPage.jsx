import {
  useState
} from "react";

import {
  Navigate,
  useNavigate
} from "react-router-dom";

import useAuth from "../auth/useAuth";
import Message from "../components/Message";

export default function LoginPage() {
  const {
    authenticated,
    login
  } = useAuth();

  const navigate = useNavigate();

  const [username, setUsername] =
    useState("");

  const [password, setPassword] =
    useState("");

  const [loading, setLoading] =
    useState(false);

  const [error, setError] =
    useState("");

  if (authenticated) {
    return (
      <Navigate
        to="/"
        replace
      />
    );
  }

  async function handleSubmit(event) {
    event.preventDefault();

    if (!username.trim()) {
      setError("Username is required.");
      return;
    }

    if (!password) {
      setError("Password is required.");
      return;
    }

    try {
      setLoading(true);
      setError("");

      await login(
        username.trim(),
        password
      );

      navigate(
        "/",
        { replace: true }
      );

    } catch (requestError) {
      setError(
        requestError.status === 401
          ? "Invalid username or password."
          : requestError.message
      );

    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="login-page">
      <section className="login-card">
        <span className="login-logo">
          CW
        </span>

        <p className="eyebrow">
          Secure card portal
        </p>

        <h1>Welcome to CardWise</h1>

        <p className="muted">
          Sign in to access your cards,
          transactions and payments.
        </p>

        <form
          className="form-stack"
          onSubmit={handleSubmit}
        >
          <label htmlFor="username">
            Username
          </label>

          <input
            id="username"
            value={username}
            onChange={event =>
              setUsername(event.target.value)
            }
            autoComplete="username"
            placeholder="Enter username"
            required
          />

          <label htmlFor="password">
            Password
          </label>

          <input
            id="password"
            type="password"
            value={password}
            onChange={event =>
              setPassword(event.target.value)
            }
            autoComplete="current-password"
            placeholder="Enter password"
            required
          />

          <Message type="error">
            {error}
          </Message>

          <button
            type="submit"
            disabled={loading}
          >
            {loading
              ? "Signing in..."
              : "Sign in"}
          </button>
        </form>

        <p className="demo-note">
          Local demo account: aarav
        </p>
      </section>
    </main>
  );
}