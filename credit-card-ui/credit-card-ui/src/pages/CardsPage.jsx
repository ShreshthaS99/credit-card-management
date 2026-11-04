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
  getCards,
  activateCard,
  freezeCard,
  unfreezeCard
} from "../api/cardwiseApi";

export default function CardsPage() {
  const { user } = useAuth();

  const [cards, setCards] =
    useState([]);

  const [loading, setLoading] =
    useState(true);

  const [workingCardId, setWorkingCardId] =
    useState(null);

  const [message, setMessage] =
    useState("");

  const [error, setError] =
    useState("");

  const loadCards = useCallback(
    async () => {
      const accounts =
        await getCustomerAccounts(
          user.customerId
        );

      const account = accounts[0];

      if (!account) {
        setCards([]);
        return;
      }

      const result =
        await getCards(account.id);

      setCards(result);
    },
    [user.customerId]
  );

  useEffect(() => {
    async function load() {
      try {
        setLoading(true);
        setError("");

        await loadCards();

      } catch (requestError) {
        setError(
          requestError.message
        );

      } finally {
        setLoading(false);
      }
    }

    load();
  }, [loadCards]);

  async function handleActivate(cardId) {
    try {
      setWorkingCardId(cardId);
      setMessage("");
      setError("");

      await activateCard(cardId);

      setMessage(
        "Card activated successfully."
      );

      await loadCards();

    } catch (requestError) {
      setError(
        requestError.message
      );

    } finally {
      setWorkingCardId(null);
    }
  }

  async function handleFreeze(cardId) {
    const confirmed =
      window.confirm(
        "Are you sure you want to freeze this card?"
      );

    if (!confirmed) {
      return;
    }

    try {
      setWorkingCardId(cardId);
      setMessage("");
      setError("");

      await freezeCard(cardId);

      setMessage(
        "Card frozen successfully."
      );

      await loadCards();

    } catch (requestError) {
      setError(
        requestError.message
      );

    } finally {
      setWorkingCardId(null);
    }
  }

  async function handleUnfreeze(cardId) {
    const confirmed =
      window.confirm(
        "Do you want to unfreeze this card?"
      );

    if (!confirmed) {
      return;
    }

    try {
      setWorkingCardId(cardId);
      setMessage("");
      setError("");

      await unfreezeCard(cardId);

      setMessage(
        "Card unfrozen successfully."
      );

      await loadCards();

    } catch (requestError) {
      setError(
        requestError.message
      );

    } finally {
      setWorkingCardId(null);
    }
  }

  if (loading) {
    return (
      <Loading text="Loading cards..." />
    );
  }

  return (
    <>
      <section className="page-heading">
        <div>
          <p className="eyebrow">
            Card controls
          </p>

          <h1>My cards</h1>

          <p className="muted">
            Activate, freeze or unfreeze
            your card.
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
        {cards.map(card => (
          <article
            key={card.id}
            className="panel"
          >
            <div className="panel-header">
              <div>
                <h2>
                  {card.nickname}
                </h2>

                <p>
                  {card.maskedPan}
                </p>
              </div>

              <span className="status-badge">
                {card.status}
              </span>
            </div>

            <dl className="details-grid">
              <div>
                <dt>
                  Online transactions
                </dt>

                <dd>
                  {card.onlineTransactionsEnabled
                    ? "Enabled"
                    : "Disabled"}
                </dd>
              </div>

              <div>
                <dt>Contactless</dt>

                <dd>
                  {card.contactlessEnabled
                    ? "Enabled"
                    : "Disabled"}
                </dd>
              </div>

              <div>
                <dt>
                  International usage
                </dt>

                <dd>
                  {card.internationalEnabled
                    ? "Enabled"
                    : "Disabled"}
                </dd>
              </div>

              <div>
                <dt>Card state</dt>

                <dd>
                  {card.frozen
                    ? "Frozen"
                    : "Available"}
                </dd>
              </div>
            </dl>

            <div className="button-row">
              {card.status === "ISSUED" &&
                !card.frozen && (
                  <button
                    type="button"
                    disabled={
                      workingCardId === card.id
                    }
                    onClick={() =>
                      handleActivate(card.id)
                    }
                  >
                    {workingCardId === card.id
                      ? "Please wait..."
                      : "Activate card"}
                  </button>
                )}

              {card.status === "ACTIVE" &&
                !card.frozen && (
                  <button
                    type="button"
                    className="danger-button"
                    disabled={
                      workingCardId === card.id
                    }
                    onClick={() =>
                      handleFreeze(card.id)
                    }
                  >
                    {workingCardId === card.id
                      ? "Please wait..."
                      : "Freeze card"}
                  </button>
                )}

              {card.status === "BLOCKED" &&
                card.frozen && (
                  <button
                    type="button"
                    className="unfreeze-button"
                    disabled={
                      workingCardId === card.id
                    }
                    onClick={() =>
                      handleUnfreeze(card.id)
                    }
                  >
                    {workingCardId === card.id
                      ? "Please wait..."
                      : "Unfreeze card"}
                  </button>
                )}
            </div>
          </article>
        ))}
      </section>

      {cards.length === 0 &&
        !error && (
          <p>
            No cards were found.
          </p>
        )}
    </>
  );
}