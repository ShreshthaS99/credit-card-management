import {
  NavLink,
  Outlet,
  useNavigate
} from "react-router-dom";

import useAuth from "../auth/useAuth";


  const navigation = [
  ["/dashboard", "Overview"],
  ["/cards", "My cards"],
  ["/transactions", "Transactions"],
  ["/payments", "Payments"],
  ["/statements", "Statements"],
  ["/limits", "Limits"],
  ["/rewards", "Rewards"],
  ["/fraud", "Fraud alerts"],
  ["/profile", "Profile"]
];

export default function AppLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();

    navigate(
      "/login",
      { replace: true }
    );
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <span className="brand-logo">
            CW
          </span>

          <div>
            <strong>CardWise</strong>
            <small>Customer portal</small>
          </div>
        </div>

        <nav aria-label="Application navigation">
          {navigation.map(([path, label]) => (
            <NavLink
              key={path}
              to={path}
              end={path === "/dashboard"}
              className={({ isActive }) =>
                isActive
                  ? "nav-link active"
                  : "nav-link"
              }
            >
              {label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="main-area">
        <header className="topbar">
          <div>
            <strong>
              Credit Card Management System
            </strong>

            <small>
              Secure cardholder portal
            </small>
          </div>

          <div className="user-area">
            <span>
              {user?.username}
            </span>

            <span className="role-badge">
              {user?.role}
            </span>

            <button
              type="button"
              className="secondary-button"
              onClick={handleLogout}
            >
              Sign out
            </button>
          </div>
        </header>

        <main className="page-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}