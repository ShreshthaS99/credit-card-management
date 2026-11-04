import {
  useEffect,
  useState
} from "react";

import useAuth from "../auth/useAuth";

import Loading from "../components/Loading";
import Message from "../components/Message";

import {
  getCustomer,
  updateCustomer
} from "../api/cardwiseApi";

export default function ProfilePage() {
  const { user } = useAuth();

  const [form, setForm] = useState({
    fullName: "",
    email: "",
    phone: ""
  });

  const [customerStatus, setCustomerStatus] =
    useState("");

  const [loading, setLoading] =
    useState(true);

  const [saving, setSaving] =
    useState(false);

  const [message, setMessage] =
    useState("");

  const [error, setError] =
    useState("");

  useEffect(() => {
    async function loadProfile() {
      try {
        setLoading(true);
        setError("");

        const customer =
          await getCustomer(
            user.customerId
          );

        setForm({
          fullName:
            customer.fullName ?? "",
          email:
            customer.email ?? "",
          phone:
            customer.phone ?? ""
        });

        setCustomerStatus(
          customer.status ?? ""
        );

      } catch (requestError) {
        setError(
          requestError.message
        );

      } finally {
        setLoading(false);
      }
    }

    loadProfile();

  }, [user.customerId]);

  function handleChange(event) {
    const {
      name,
      value
    } = event.target;

    setForm(previous => ({
      ...previous,
      [name]: value
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();

    setMessage("");
    setError("");

    if (!form.fullName.trim()) {
      setError(
        "Full name is required."
      );

      return;
    }

    if (!form.email.trim()) {
      setError(
        "Email address is required."
      );

      return;
    }

    if (!form.phone.trim()) {
      setError(
        "Phone number is required."
      );

      return;
    }

    try {
      setSaving(true);

      const updatedCustomer =
        await updateCustomer(
          user.customerId,
          {
            fullName:
              form.fullName.trim(),

            email:
              form.email
                .trim()
                .toLowerCase(),

            phone:
              form.phone.trim()
          }
        );

      setForm({
        fullName:
          updatedCustomer.fullName ?? "",

        email:
          updatedCustomer.email ?? "",

        phone:
          updatedCustomer.phone ?? ""
      });

      setCustomerStatus(
        updatedCustomer.status ?? ""
      );

      setMessage(
        "Profile updated successfully."
      );

    } catch (requestError) {
      if (
        requestError.fieldErrors &&
        Object.keys(
          requestError.fieldErrors
        ).length > 0
      ) {
        const firstError =
          Object.values(
            requestError.fieldErrors
          )[0];

        setError(firstError);

      } else {
        setError(
          requestError.message
        );
      }

    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <Loading text="Loading profile..." />
    );
  }

  return (
    <>
      <section className="page-heading">
        <div>
          <p className="eyebrow">
            Customer profile
          </p>

          <h1>My profile</h1>

          <p className="muted">
            Review and update your
            personal contact information.
          </p>
        </div>

        {customerStatus && (
          <span className="status-badge">
            {customerStatus}
          </span>
        )}
      </section>

      <div className="profile-grid">
        <section className="panel">
          <div className="profile-avatar">
            {form.fullName
              ? form.fullName
                  .charAt(0)
                  .toUpperCase()
              : "C"}
          </div>

          <h2>
            {form.fullName ||
              user.username}
          </h2>

          <p className="muted">
            {form.email}
          </p>

          <dl className="profile-details">
            <div>
              <dt>Username</dt>
              <dd>
                {user.username}
              </dd>
            </div>

            <div>
              <dt>Role</dt>
              <dd>
                {user.role}
              </dd>
            </div>

            <div>
              <dt>Customer status</dt>
              <dd>
                {customerStatus ||
                  "Not available"}
              </dd>
            </div>
          </dl>
        </section>

        <form
          className="panel form-stack"
          onSubmit={handleSubmit}
        >
          <div>
            <p className="eyebrow">
              Personal details
            </p>

            <h2>
              Update profile
            </h2>
          </div>

          <label htmlFor="fullName">
            Full name
          </label>

          <input
            id="fullName"
            name="fullName"
            type="text"
            value={form.fullName}
            onChange={handleChange}
            placeholder="Enter full name"
            autoComplete="name"
            required
          />

          <label htmlFor="email">
            Email address
          </label>

          <input
            id="email"
            name="email"
            type="email"
            value={form.email}
            onChange={handleChange}
            placeholder="Enter email address"
            autoComplete="email"
            required
          />

          <label htmlFor="phone">
            Phone number
          </label>

          <input
            id="phone"
            name="phone"
            type="tel"
            value={form.phone}
            onChange={handleChange}
            placeholder="Enter phone number"
            autoComplete="tel"
            required
          />

          <Message>
            {message}
          </Message>

          <Message type="error">
            {error}
          </Message>

          <button
            type="submit"
            disabled={saving}
          >
            {saving
              ? "Saving..."
              : "Save changes"}
          </button>
        </form>
      </div>
    </>
  );
}