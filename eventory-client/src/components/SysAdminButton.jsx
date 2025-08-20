export function SysAdminButton({
  text,
  textColor,
  color = "white",
  disable,
  onClick,
  borderColor,
  padding = "0.4rem 2.5rem",
}) {
  return (
    <button
      onClick={onClick}
      disabled={disable}
      style={{
        outline: "none",
        borderColor: borderColor,
        color: textColor,
        padding: padding,
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
