export function Divider({ verticalMargin = "0.8rem" }) {
  return (
    <hr
      className="mainThemeColor dashed"
      style={{
        borderTop: "1px solid #E6EFF5",
        margin: "0.7rem 2rem 0.7rem 2rem",
        marginTop: verticalMargin,
        marginBottom: verticalMargin,
      }}
    ></hr>
  );
}

export default Divider;