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
  getRewardBalance,
  getRewardLedger,
  redeemRewards
} from "../api/cardwiseApi";

export default function RewardsPage() {
  const { user } = useAuth();

  const [account, setAccount] =
    useState(null);

  const [balance, setBalance] =
    useState(0);

  const [ledger, setLedger] =
    useState([]);

  const [points, setPoints] =
    useState("");

  const [loading, setLoading] =
    useState(true);

  const [submitting, setSubmitting] =
    useState(false);

  const [message, setMessage] =
    useState("");

  const [error, setError] =
    useState("");

  const loadRewards = useCallback(
    async selectedAccount => {
      const [
        balanceResult,
        ledgerResult
      ] = await Promise.all([
        getRewardBalance(
          selectedAccount.id
        ),
        getRewardLedger(
          selectedAccount.id
        )
      ]);

      setBalance(
        balanceResult.points ?? 0
      );

      setLedger(ledgerResult);
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

        await loadRewards(
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
    loadRewards
  ]);

  async function handleSubmit(event) {
    event.preventDefault();

    const numericPoints =
      Number(points);

    if (
      !Number.isInteger(numericPoints)
      || numericPoints <= 0
    ) {
      setError(
        "Enter a whole number greater than zero."
      );
      return;
    }

    if (numericPoints > balance) {
      setError(
        "You do not have enough reward points."
      );
      return;
    }

    try {
      setSubmitting(true);
      setMessage("");
      setError("");

      await redeemRewards({
        accountId: account.id,
        points: numericPoints
      });

      setMessage(
        "Reward points redeemed successfully."
      );

      setPoints("");

      await loadRewards(account);

    } catch (requestError) {
      setError(requestError.message);

    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return (
      <Loading text="Loading rewards..." />
    );
  }

  return (
    <>
      <section className="page-heading">
        <div>
          <p className="eyebrow">
            Loyalty rewards
          </p>

          <h1>Rewards</h1>
        </div>

        <div className="points-card">
          <span>Available points</span>
          <strong>{balance}</strong>
        </div>
      </section>

      <div className="two-column-grid">
        <form
          className="panel form-stack"
          onSubmit={handleSubmit}
        >
          <h2>Redeem points</h2>

          <label htmlFor="rewardPoints">
            Number of points
          </label>

          <input
            id="rewardPoints"
            type="number"
            min="1"
            step="1"
            value={points}
            onChange={event =>
              setPoints(event.target.value)
            }
            required
          />

          <button
            type="submit"
            disabled={
              submitting || !account
            }
          >
            {submitting
              ? "Redeeming..."
              : "Redeem points"}
          </button>

          <Message>
            {message}
          </Message>

          <Message type="error">
            {error}
          </Message>
        </form>

        <section className="panel">
          <h2>Reward history</h2>

          <div className="item-list">
            {ledger.map(entry => (
              <div
                key={entry.id}
                className="list-item"
              >
                <div>
                  <strong>
                    {entry.type}
                  </strong>

                  <small>
                    {entry.description}
                  </small>
                </div>

                <strong
                  className={
                    entry.points >= 0
                      ? "positive-points"
                      : "negative-points"
                  }
                >
                  {entry.points >= 0
                    ? "+"
                    : ""}
                  {entry.points}
                </strong>
              </div>
            ))}
          </div>

          {ledger.length === 0 && (
            <p>No reward entries found.</p>
          )}
        </section>
      </div>
    </>
  );
}