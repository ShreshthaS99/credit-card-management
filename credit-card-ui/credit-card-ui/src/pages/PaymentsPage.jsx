import {
  useCallback,
  useEffect,
  useRef,
  useState
} from "react";

import useAuth from "../auth/useAuth";
import Loading from "../components/Loading";
import Message from "../components/Message";

import {
  getCustomerAccounts,
  createPayment,
  getPayments
} from "../api/cardwiseApi";

function createIdempotencyKey() {
  if (window.crypto?.randomUUID) {
    return window.crypto.randomUUID();
  }

  return `payment-${Date.now()}`;
}

function formatMoney(amount, currency) {
  return new Intl.NumberFormat(
    "en-IN",
    {
      style: "currency",
      currency
    }
  ).format(Number(amount));
}

export default function PaymentsPage() {
  const { user } = useAuth();

  const [account, setAccount] =
    useState(null);

  const [payments, setPayments] =
    useState([]);

  const [amount, setAmount] =
    useState("");

  const [loading, setLoading] =
    useState(true);

  const [submitting, setSubmitting] =
    useState(false);

  const [message, setMessage] =
    useState("");

  const [error, setError] =
    useState("");

  const idempotencyKey =
    useRef(createIdempotencyKey());

  const loadPayments = useCallback(
    async selectedAccount => {
      const result = await getPayments(
        selectedAccount.id
      );

      setPayments(result);
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

        await loadPayments(selectedAccount);

      } catch (requestError) {
        setError(requestError.message);

      } finally {
        setLoading(false);
      }
    }

    load();
  }, [
    user.customerId,
    loadPayments
  ]);

  async function handleSubmit(event) {
    event.preventDefault();

    const numericAmount =
      Number(amount);

    if (!Number.isFinite(numericAmount)
        || numericAmount <= 0) {
      setError(
        "Enter a payment amount greater than zero."
      );
      return;
    }

    if (!account) {
      setError("No account is available.");
      return;
    }

    if (
      numericAmount >
      Number(account.currentBalance)
    ) {
      setError(
        "Payment cannot exceed the current balance."
      );
      return;
    }

    try {
      setSubmitting(true);
      setError("");
      setMessage("");

      const result = await createPayment(
        {
          accountId: account.id,
          amount: numericAmount,
          currency: account.currency
        },
        idempotencyKey.current
      );

      setMessage(
        `Payment ${result.paymentReference} ` +
        `was submitted with status ${result.status}.`
      );

      setAmount("");

      idempotencyKey.current =
        createIdempotencyKey();

      await loadPayments(account);

    } catch (requestError) {
      setError(requestError.message);

    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return (
      <Loading text="Loading payments..." />
    );
  }

  return (
    <>
      <section className="page-heading">
        <div>
          <p className="eyebrow">
            Balance payment
          </p>

          <h1>Payments</h1>

          <p className="muted">
            Make a payment and review
            previous payment requests.
          </p>
        </div>
      </section>

      <div className="two-column-grid">
        <form
          className="panel form-stack"
          onSubmit={handleSubmit}
        >
          <h2>Make a payment</h2>

          <p className="muted">
            Current balance:{" "}
            {account
              ? formatMoney(
                  account.currentBalance,
                  account.currency
                )
              : "Not available"}
          </p>

          <label htmlFor="paymentAmount">
            Payment amount
          </label>

          <input
            id="paymentAmount"
            type="number"
            min="0.01"
            step="0.01"
            value={amount}
            onChange={event =>
              setAmount(event.target.value)
            }
            placeholder="Enter amount"
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
              : "Submit payment"}
          </button>

          <Message>
            {message}
          </Message>

          <Message type="error">
            {error}
          </Message>
        </form>

        <section className="panel">
          <h2>Payment history</h2>

          {payments.length === 0 ? (
            <p className="muted">
              No payment records found.
            </p>
          ) : (
            <div className="item-list">
              {payments.map(payment => (
                <div
                  key={payment.paymentId}
                  className="list-item"
                >
                  <div>
                    <strong>
                      {payment.paymentReference}
                    </strong>

                    <small>
                      {payment.status}
                    </small>
                  </div>

                  <strong>
                    {formatMoney(
                      payment.amount,
                      payment.currency
                    )}
                  </strong>
                </div>
              ))}
            </div>
          )}
        </section>
      </div>
    </>
  );
}