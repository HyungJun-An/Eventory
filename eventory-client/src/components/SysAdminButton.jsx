export function SysAdminButton({
  text,
  textColor,
  color = "white",
  disable,
  onClick,
}) {
  return (
    <button
      onClick={onClick}
      disabled={disable}
      style={{
        outline: "none",
        borderColor: textColor,
        color: textColor,
        padding: "0.4rem 2.5rem",
        borderRadius: "1rem",
        width: "fit-content",
        justifySelf: "center",
        backgroundColor: color,
      }}
    >
      {text}
    </button>
  );
}

export default SysAdminButton;
