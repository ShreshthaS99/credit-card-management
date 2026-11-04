import {
  BrowserRouter,
  Navigate,
  Route,
  Routes
} from "react-router-dom";

import ProtectedRoute from "./auth/ProtectedRoute";
import AppLayout from "./components/AppLayout";

import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import DashboardPage from "./pages/DashboardPage";
import CardsPage from "./pages/CardsPage";
import TransactionsPage from "./pages/TransactionsPage";
import PaymentsPage from "./pages/PaymentsPage";
import StatementsPage from "./pages/StatementsPage";
import LimitsPage from "./pages/LimitsPage";
import RewardsPage from "./pages/RewardsPage";
import FraudPage from "./pages/FraudPage";
import ProfilePage from "./pages/ProfilePage";

import "./styles/app.css";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>

        {/* Public routes */}

        <Route
          path="/"
          element={<HomePage />}
        />

        <Route
          path="/login"
          element={<LoginPage />}
        />


        {/* Protected routes */}

        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>

            <Route
              path="/dashboard"
              element={<DashboardPage />}
            />

            <Route
              path="/cards"
              element={<CardsPage />}
            />

            <Route
              path="/transactions"
              element={<TransactionsPage />}
            />

            <Route
              path="/payments"
              element={<PaymentsPage />}
            />

            <Route
              path="/statements"
              element={<StatementsPage />}
            />

            <Route
              path="/limits"
              element={<LimitsPage />}
            />

            <Route
              path="/rewards"
              element={<RewardsPage />}
            />

            <Route
              path="/fraud"
              element={<FraudPage />}
            />

            <Route
              path="/profile"
              element={<ProfilePage />}
            />

          </Route>
        </Route>


        {/* Unknown route */}

        <Route
          path="*"
          element={
            <Navigate
              to="/"
              replace
            />
          }
        />

      </Routes>
    </BrowserRouter>
  );
}