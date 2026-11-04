import {
  useEffect,
  useState
} from "react";

import useAuth from "../auth/useAuth";
import Loading from "../components/Loading";
import Message from "../components/Message";

import {
  getCustomer,
  getCustomerAccounts,
  getCards,
  getTransactions
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

export default function DashboardPage() {
  const { user } = useAuth();

  const [customer, setCustomer] =
    useState(null);

  const [account, setAccount] =
    useState(null);

  const [card, setCard] =
    useState(null);

  const [transactions, setTransactions] =
    useState([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState("");

  useEffect(() => {
    async function loadDashboard() {
      try {
        setLoading(true);
        setError("");

        const [
          customerResult,
          accountsResult
        ] = await Promise.all([
          getCustomer(user.customerId),
          getCustomerAccounts(user.customerId)
        ]);

        setCustomer(customerResult);

        const selectedAccount =
          accountsResult[0] ?? null;

        setAccount(selectedAccount);

        if (!selectedAccount) {
          return;
        }

        const [
          cardsResult,
          transactionsResult
        ] = await Promise.all([
          getCards(selectedAccount.id),
          getTransactions(
            selectedAccount.id,
            0,
            5
          )
        ]);

        setCard(cardsResult[0] ?? null);

        setTransactions(
          transactionsResult.content ?? []
        );

      } catch (requestError) {
        setError(requestError.message);

      } finally {
        setLoading(false);
      }
    }

    loadDashboard();
  }, [user.customerId]);

  if (loading) {
    return (
      <Loading text="Loading dashboard..." />
    );
  }

  return (
    <>
      <Message type="error">
        {error}
      </Message>

      <section className="page-heading">
        <div>
          <p className="eyebrow">
            Account overview
          </p>

          <h1>
            Welcome,{" "}
            {customer?.fullName ??
              user.username}
          </h1>

          <p className="muted">
            Review your account balance,
            card status and recent activity.
          </p>
        </div>
      </section>

      {!account ? (
        <Message type="error">
          No card account was found for this customer.
        </Message>
      ) : (
        <>
          <section className="summary-grid">
            <article className="summary-card">
              <span>Credit limit</span>

              <strong>
                {formatMoney(
                  account.creditLimit,
                  account.currency
                )}
              </strong>
            </article>

            <article className="summary-card">
              <span>Current balance</span>

              <strong>
                {formatMoney(
                  account.currentBalance,
                  account.currency
                )}
              </strong>
            </article>

            <article className="summary-card">
              <span>Minimum due</span>

              <strong>
                {formatMoney(
                  account.minimumDue,
                  account.currency
                )}
              </strong>
            </article>

            <article className="summary-card">
              <span>Payment due date</span>

              <strong>
                {account.paymentDueDate ??
                  "Not available"}
              </strong>
            </article>
          </section>

          <section className="two-column-grid">
            <article className="card-visual">
              <div className="card-visual-top">
                <strong>CardWise</strong>

                <span>
                  {card?.status ?? "NO CARD"}
                </span>
              </div>

              <p className="masked-pan">
                {card?.maskedPan ??
                  "XXXX XXXX XXXX XXXX"}
              </p>

              <div className="card-visual-bottom">
                <span>
                  {customer?.fullName}
                </span>

                <span>
                  {card?.nickname}
                </span>
              </div>
            </article>

            <article className="panel">
              <h2>Recent transactions</h2>

              {transactions.length === 0 ? (
                <p className="muted">
                  No transactions found.
                </p>
              ) : (
                <div className="item-list">
                  {transactions.map(item => (
                    <div
                      key={item.id}
                      className="list-item"
                    >
                      <div>
                        <strong>
                          {item.merchant}
                        </strong>

                        <small>
                          {item.status.replaceAll(
                            "_",
                            " "
                          )}
                        </small>
                      </div>

                      <strong>
                        {formatMoney(
                          item.amount,
                          item.currency
                        )}
                      </strong>
                    </div>
                  ))}
                </div>
              )}
            </article>
          </section>
        </>
      )}
    </>
  );
}