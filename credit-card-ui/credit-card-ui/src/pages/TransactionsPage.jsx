import {
  useEffect,
  useState
} from "react";

import useAuth from "../auth/useAuth";
import Loading from "../components/Loading";
import Message from "../components/Message";

import {
  getCustomerAccounts,
  getTransactions
} from "../api/cardwiseApi";

function formatMoney(amount, currency) {
  return new Intl.NumberFormat(
    "en-IN",
    {
      style: "currency",
      currency
    }
  ).format(Number(amount));
}

export default function TransactionsPage() {
  const { user } = useAuth();

  const [transactions, setTransactions] =
    useState([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState("");

  useEffect(() => {
    async function loadTransactions() {
      try {
        setLoading(true);

        const accounts =
          await getCustomerAccounts(
            user.customerId
          );

        const account = accounts[0];

        if (!account) {
          return;
        }

        const result =
          await getTransactions(
            account.id,
            0,
            50
          );

        setTransactions(
          result.content ?? []
        );

      } catch (requestError) {
        setError(requestError.message);

      } finally {
        setLoading(false);
      }
    }

    loadTransactions();
  }, [user.customerId]);

  if (loading) {
    return (
      <Loading text="Loading transactions..." />
    );
  }

  return (
    <>
      <section className="page-heading">
        <div>
          <p className="eyebrow">
            Account activity
          </p>

          <h1>Transactions</h1>

          <p className="muted">
            Pending and posted transaction
            statuses are displayed separately.
          </p>
        </div>
      </section>

      <Message type="error">
        {error}
      </Message>

      <div className="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>Date</th>
              <th>Merchant</th>
              <th>Status</th>
              <th>Reference</th>
              <th className="number-column">
                Amount
              </th>
            </tr>
          </thead>

          <tbody>
            {transactions.map(transaction => (
              <tr key={transaction.id}>
                <td>
                  {new Date(
                    transaction.transactionTime
                  ).toLocaleString()}
                </td>

                <td>
                  {transaction.merchant}
                </td>

                <td>
                  <span className="status-badge">
                    {transaction.status.replaceAll(
                      "_",
                      " "
                    )}
                  </span>
                </td>

                <td>
                  {transaction.processorReference ??
                    "-"}
                </td>

                <td className="number-column">
                  {formatMoney(
                    transaction.amount,
                    transaction.currency
                  )}
                </td>
              </tr>
            ))}

            {transactions.length === 0 && (
              <tr>
                <td colSpan="5">
                  No transactions found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </>
  );
}