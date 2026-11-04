import {
  useEffect,
  useState
} from "react";

import useAuth from "../auth/useAuth";
import Loading from "../components/Loading";
import Message from "../components/Message";

import {
  getCustomerAccounts,
  getStatements,
  getStatementLines
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

export default function StatementsPage() {
  const { user } = useAuth();

  const [statements, setStatements] =
    useState([]);

  const [selectedStatement, setSelectedStatement] =
    useState(null);

  const [lines, setLines] =
    useState([]);

  const [loading, setLoading] =
    useState(true);

  const [loadingLines, setLoadingLines] =
    useState(false);

  const [error, setError] =
    useState("");

  useEffect(() => {
    async function loadStatements() {
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
          await getStatements(account.id);

        setStatements(result);

      } catch (requestError) {
        setError(requestError.message);

      } finally {
        setLoading(false);
      }
    }

    loadStatements();
  }, [user.customerId]);

  async function handleView(statement) {
    try {
      setSelectedStatement(statement);
      setLoadingLines(true);
      setError("");

      const result =
        await getStatementLines(
          statement.id
        );

      setLines(result);

    } catch (requestError) {
      setError(requestError.message);

    } finally {
      setLoadingLines(false);
    }
  }

  if (loading) {
    return (
      <Loading text="Loading statements..." />
    );
  }

  return (
    <>
      <section className="page-heading">
        <div>
          <p className="eyebrow">
            Billing history
          </p>

          <h1>Statements</h1>
        </div>
      </section>

      <Message type="error">
        {error}
      </Message>

      <div className="two-column-grid">
        <section className="panel">
          <h2>Available statements</h2>

          <div className="item-list">
            {statements.map(statement => (
              <button
                key={statement.id}
                type="button"
                className="statement-button"
                onClick={() =>
                  handleView(statement)
                }
              >
                <span>
                  {statement.cycleStart}
                  {" to "}
                  {statement.cycleEnd}
                </span>

                <strong>
                  {formatMoney(
                    statement.closingBalance
                  )}
                </strong>
              </button>
            ))}
          </div>

          {statements.length === 0 && (
            <p>No statements found.</p>
          )}
        </section>

        <section className="panel">
          <h2>Statement details</h2>

          {!selectedStatement ? (
            <p className="muted">
              Select a statement to view
              its line items.
            </p>
          ) : (
            <>
              <dl className="details-grid">
                <div>
                  <dt>Opening balance</dt>
                  <dd>
                    {formatMoney(
                      selectedStatement.openingBalance
                    )}
                  </dd>
                </div>

                <div>
                  <dt>Closing balance</dt>
                  <dd>
                    {formatMoney(
                      selectedStatement.closingBalance
                    )}
                  </dd>
                </div>

                <div>
                  <dt>Minimum due</dt>
                  <dd>
                    {formatMoney(
                      selectedStatement.minimumDue
                    )}
                  </dd>
                </div>

                <div>
                  <dt>Due date</dt>
                  <dd>
                    {selectedStatement.dueDate}
                  </dd>
                </div>
              </dl>

              {loadingLines ? (
                <Loading text="Loading statement lines..." />
              ) : (
                <div className="item-list">
                  {lines.map(line => (
                    <div
                      key={line.id}
                      className="list-item"
                    >
                      <div>
                        <strong>
                          {line.description}
                        </strong>

                        <small>
                          {line.type}
                        </small>
                      </div>

                      <strong>
                        {formatMoney(line.amount)}
                      </strong>
                    </div>
                  ))}
                </div>
              )}
            </>
          )}
        </section>
      </div>
    </>
  );
}