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
  getLimitRequests,
  createLimitRequest
} from "../api/cardwiseApi";

function formatMoney(
  amount,
  currency = "INR"
) {
  return new Intl.NumberFormat(
    "en-IN",
    {
      style: "currency",
      currency
    }
  ).format(Number(amount ?? 0));
}

export default function LimitsPage() {
  const { user } = useAuth();

  const [account, setAccount] =
    useState(null);

  const [requests, setRequests] =
    useState([]);

  const [requestedLimit, setRequestedLimit] =
    useState("");

  const [reason, setReason] =
    useState("");

  const [loading, setLoading] =
    useState(true);

  const [submitting, setSubmitting] =
    useState(false);

  const [message, setMessage] =
    useState("");

  const [error, setError] =
    useState("");

  const loadRequests = useCallback(
    async selectedAccount => {
      const result =
        await getLimitRequests(
          selectedAccount.id
        );

      setRequests(result);
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

        await loadRequests(
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
    loadRequests
  ]);

  async function handleSubmit(event) {
    event.preventDefault();

    const numericLimit =
      Number(requestedLimit);

    if (!Number.isFinite(numericLimit)) {
      setError(
        "Enter a valid requested limit."
      );
      return;
    }

    if (
      numericLimit <=
      Number(account?.creditLimit ?? 0)
    ) {
      setError(
        "Requested limit must exceed the current limit."
      );
      return;
    }

    try {
      setSubmitting(true);
      setMessage("");
      setError("");

      await createLimitRequest({
        accountId: account.id,
        requestedLimit: numericLimit,
        reason: reason.trim()
      });

      setMessage(
        "Credit-limit request submitted."
      );

      setRequestedLimit("");
      setReason("");

      await loadRequests(account);

    } catch (requestError) {
      setError(requestError.message);

    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return (
      <Loading text="Loading limit requests..." />
    );
  }

  return (
    <>
      <section className="page-heading">
        <div>
          <p className="eyebrow">
            Account limits
          </p>

          <h1>Credit limit</h1>
        </div>
      </section>

      <div className="two-column-grid">
        <form
          className="panel form-stack"
          onSubmit={handleSubmit}
        >
          <h2>Request higher limit</h2>

          <p className="muted">
            Current limit:{" "}
            {formatMoney(
              account?.creditLimit,
              account?.currency
            )}
          </p>

          <label htmlFor="requestedLimit">
            Requested limit
          </label>

          <input
            id="requestedLimit"
            type="number"
            min="1"
            step="0.01"
            value={requestedLimit}
            onChange={event =>
              setRequestedLimit(
                event.target.value
              )
            }
            required
          />

          <label htmlFor="limitReason">
            Reason
          </label>

          <textarea
            id="limitReason"
            value={reason}
            onChange={event =>
              setReason(event.target.value)
            }
            placeholder="Explain why you need a higher limit"
            required
          />

          <button
            type="submit"
            disabled={
              submitting || !account
            }
          >
            {submitting
              ? "Submitting..."
              : "Submit request"}
          </button>

          <Message>
            {message}
          </Message>

          <Message type="error">
            {error}
          </Message>
        </form>

        <section className="panel">
          <h2>Request history</h2>

          <div className="item-list">
            {requests.map(request => (
              <div
                key={request.id}
                className="list-item"
              >
                <div>
                  <strong>
                    {formatMoney(
                      request.requestedLimit,
                      account?.currency
                    )}
                  </strong>

                  <small>
                    {request.reason}
                  </small>
                </div>

                <span className="status-badge">
                  {request.status}
                </span>
              </div>
            ))}
          </div>

          {requests.length === 0 && (
            <p className="muted">
              No limit requests found.
            </p>
          )}
        </section>
      </div>
    </>
  );
}