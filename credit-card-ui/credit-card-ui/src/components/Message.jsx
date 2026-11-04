export default function Message({
  type = "success",
  children
}) {
  if (!children) {
    return null;
  }

  return (
    <p
      className={
        type === "error"
          ? "message error-message"
          : "message success-message"
      }
      role={
        type === "error"
          ? "alert"
          : "status"
      }
    >
      {children}
    </p>
  );
}