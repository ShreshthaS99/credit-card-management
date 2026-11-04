export default function Loading({
  text = "Loading..."
}) {
  return (
    <div
      className="loading"
      role="status"
      aria-live="polite"
    >
      <span className="spinner" />
      <span>{text}</span>
    </div>
  );
}