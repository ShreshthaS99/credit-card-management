import { Link } from "react-router-dom";

export default function HomePage() {
  return (
    <main className="home-page">

      <header className="home-header">
        <div className="home-brand">
          <span className="home-logo">
            CW
          </span>

          <div>
            <strong>CardWise</strong>
            <small>
              Credit Card Management
            </small>
          </div>
        </div>

        <Link
          to="/login"
          className="home-login-button"
        >
          Login
        </Link>
      </header>

      <section className="home-hero">

        <div className="hero-content">

          <p className="eyebrow">
            Credit Card Management Portal
          </p>

          <h1>
            Manage your credit card
            securely and simply.
          </h1>

          <p className="hero-description">
            View your card details,
            transactions and statements,
            make payments, manage rewards,
            request credit limits and review
            security alerts from one place.
          </p>

          <div className="hero-actions">
            <Link
              to="/login"
              className="primary-home-button"
            >
              Login to CardWise
            </Link>

            <a
              href="#features"
              className="secondary-home-link"
            >
              Explore features
            </a>
          </div>

        </div>

        <div className="home-card-preview">

          <div className="preview-card-top">
            <strong>CardWise</strong>
            <span>SECURE</span>
          </div>

          <div className="preview-chip">
            <span />
            <span />
            <span />
          </div>

          <p className="preview-number">
            XXXX XXXX XXXX 4242
          </p>

          <div className="preview-card-bottom">
            <span>YOUR CARD</span>
            <span>INR</span>
          </div>

        </div>

      </section>

      <section
        id="features"
        className="home-features"
      >

        <div className="home-section-heading">
          <p className="eyebrow">
            Everything in one place
          </p>

          <h2>
            Manage your card with confidence
          </h2>
        </div>

        <div className="feature-grid">

          <article className="feature-card">
            <span className="feature-icon">
              ◉
            </span>

            <h3>Manage Cards</h3>

            <p>
              View masked card details,
              activate your card and
              temporarily freeze it.
            </p>
          </article>

          <article className="feature-card">
            <span className="feature-icon">
              ₹
            </span>

            <h3>Payments</h3>

            <p>
              View your outstanding balance
              and securely submit card
              payments.
            </p>
          </article>

          <article className="feature-card">
            <span className="feature-icon">
              ≡
            </span>

            <h3>Transactions</h3>

            <p>
              Review recent transactions
              including pending and posted
              activity.
            </p>
          </article>

          <article className="feature-card">
            <span className="feature-icon">
              ★
            </span>

            <h3>Rewards</h3>

            <p>
              Check available reward points,
              review history and redeem
              points.
            </p>
          </article>

          <article className="feature-card">
            <span className="feature-icon">
              ▣
            </span>

            <h3>Statements</h3>

            <p>
              Review previous statements,
              balances, due dates and
              statement transactions.
            </p>
          </article>

          <article className="feature-card">
            <span className="feature-icon">
              !
            </span>

            <h3>Security Alerts</h3>

            <p>
              Review suspicious transaction
              alerts and report activity you
              don't recognize.
            </p>
          </article>

        </div>

      </section>

      <section className="home-security">

        <div>
          <p className="eyebrow">
            Secure access
          </p>

          <h2>
            Your account stays protected.
          </h2>

          <p>
            CardWise uses authenticated
            access to protect account
            features and displays masked
            card information rather than
            exposing sensitive card data.
          </p>
        </div>

        <Link
          to="/login"
          className="primary-home-button"
        >
          Sign in securely
        </Link>

      </section>

      <footer className="home-footer">
        <strong>CardWise</strong>

        <span>
          Credit Card Management System
        </span>
      </footer>

    </main>
  );
}