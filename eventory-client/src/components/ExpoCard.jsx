import "../assets/css/ExpoCard.css";

export default function ExpoCard({ expo, onClick }) {
  const s = new Date(expo.startDate || expo.start_date);
  const e = new Date(expo.endDate || expo.end_date);
  const valid = !Number.isNaN(s.getTime()) && !Number.isNaN(e.getTime());
  const range = valid ? `${s.toLocaleDateString()} - ${e.toLocaleDateString()}` : "";

  return (
    <article
      className="expo-card"
      onClick={() => onClick?.(expo)}
      role="button"
      tabIndex={0}
    >
      <div className="expo-thumb-wrap">
        <img
          src={expo.imageUrl || expo.image_url || "/placeholder.jpg"}
          alt={expo.title}
          className="expo-thumb"
        />
      </div>
      <div className="expo-body">
        <h3 className="expo-title">{expo.title}</h3>
        <p className="expo-meta">{range} {expo.location ? `· ${expo.location}` : ""}</p>
        {expo.price && (
          <p className="expo-price">₩{Number(expo.price).toLocaleString()}</p>
        )}
        <button className="expo-cta" type="button">View Details</button>
      </div>
    </article>
  );
}