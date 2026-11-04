import {
  useCallback,
  useEffect,
  useState
} from "react";

import useAuth from "../auth/useAuth";
import Loading from "../components/Loading";
import Message from "../components/Message";

import {
  getCustomerAccounts,
  getFraudAlerts,
  respondToFraudAlert
} from "../api/cardwiseApi";

export default function FraudPage() {
  const { user } = useAuth();

  const [account, setAccount] =
    useState(null);

  const [alerts, setAlerts] =
    useState([]);

  const [loading, setLoading] =
    useState(true);

  const [workingAlertId, setWorkingAlertId] =
    useState(null);

  const [message, setMessage] =
    useState("");

  const [error, setError] =
    useState("");

  const loadAlerts = useCallback(
    async selectedAccount => {
      const result =
        await getFraudAlerts(
          selectedAccount.id
        );

      setAlerts(result);
    },
    []
  );

  useEffect(() => {
    async function load() {
      try {
        setLoading(true);

        const accounts =
          await getCustomerAccounts(
            user.customerId
          );

        const selectedAccount =
          accounts[0];

        if (!selectedAccount) {
          return;
        }

        setAccount(selectedAccount);

        await loadAlerts(
          selectedAccount
        );

      } catch (requestError) {
        setError(requestError.message);

      } finally {
        setLoading(false);
      }
    }

    load();
  }, [
    user.customerId,
    loadAlerts
  ]);

  async function handleResponse(
    alertId,
    response
  ) {
    const confirmed = window.confirm(
      response === "NOT_RECOGNIZED"
        ? "Report this transaction as unrecognized?"
        : "Confirm that you recognize this transaction?"
    );

    if (!confirmed) {
      return;
    }

    try {
      setWorkingAlertId(alertId);
      setMessage("");
      setError("");

      await respondToFraudAlert(
        alertId,
        response
      );

      setMessage(
        "Fraud-alert response recorded."
      );

      await loadAlerts(account);

    } catch (requestError) {
      setError(requestError.message);

    } finally {
      setWorkingAlertId(null);
    }
  }

  if (loading) {
    return (
      <Loading text="Loading fraud alerts..." />
    );
  }

  return (
    <>
      <section className="page-heading">
        <div>
          <p className="eyebrow">
            Account security
          </p>

          <h1>Fraud alerts</h1>

          <p className="muted">
            Review unusual transaction alerts
            and record your response.
          </p>
        </div>
      </section>

      <Message>
        {message}
      </Message>

      <Message type="error">
        {error}
      </Message>

      <section className="card-grid">
        {alerts.map(alert => (
          <article
            key={alert.id}
            className="panel alert-panel"
          >
            <div className="panel-header">
              <div>
                <h2>Transaction alert</h2>

                <p className="muted">
                  {alert.riskReasonCode}
                </p>
              </div>

              <span className="status-badge">
                {alert.status}
              </span>
            </div>

            <dl className="details-grid">
              <div>
                <dt>Created</dt>
                <dd>
                  {alert.createdAt
                    ? new Date(
                        alert.createdAt
                      ).toLocaleString()
                    : "-"}
                </dd>
              </div>

              <div>
                <dt>Case reference</dt>
                <dd>
                  {alert.caseReference ??
                    "Not created"}
                </dd>
              </div>
            </dl>

            {alert.customerResponse ? (
              <Message>
                Response recorded:{" "}
                {alert.customerResponse}
              </Message>
            ) : (
              <div className="button-row">
                <button
                  type="button"
                  disabled={
                    workingAlertId === alert.id
                  }
                  onClick={() =>
                    handleResponse(
                      alert.id,
                      "CONFIRMED"
                    )
                  }
                >
                  I recognize this
                </button>

                <button
                  type="button"
                  className="danger-button"
                  disabled={
                    workingAlertId === alert.id
                  }
                  onClick={() =>
                    handleResponse(
                      alert.id,
                      "NOT_RECOGNIZED"
                    )
                  }
                >
                  I don't recognize this
                </button>
              </div>
            )}
          </article>
        ))}
      </section>

      {alerts.length === 0 && !error && (
        <p>No fraud alerts were found.</p>
      )}
    </>
  );
}