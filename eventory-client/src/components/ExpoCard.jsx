import "../assets/css/ExpoCard.css";
import { DEFAULT_EXPO_IMAGE } from "../constants/images";

export default function ExpoCard({ expo, onClick }) {
  const s = new Date(expo.startDate || expo.start_date);
  const e = new Date(expo.endDate || expo.end_date);
  const valid = !Number.isNaN(s.getTime()) && !Number.isNaN(e.getTime());
  const range = valid ? `${s.toLocaleDateString()} - ${e.toLocaleDateString()}` : "";

  //  안전한 타이틀/이미지 계산
  const title = expo.title ?? expo.expoName ?? "";
  const img =
    expo.imageUrl ?? expo.image_url ?? expo.thumbnailUrl ?? DEFAULT_EXPO_IMAGE;

  return (
    <article
      className="expo-card"
      role="button"
      tabIndex={0}
      onClick={() => onClick?.(expo)}
      onKeyDown={(e) => e.key === "Enter" && onClick?.(expo)}
    >
      <div className="expo-thumb-wrap">
        <img
          src={img}
          alt={title}
          className="expo-thumb"
          loading="lazy"
          onError={(e) => {
            // 네트워크/404라도 반드시 기본이미지로
            e.currentTarget.onerror = null;
            e.currentTarget.src = DEFAULT_EXPO_IMAGE;
          }}
        />
      </div>

      <div className="expo-body">
        <h3 className="expo-title">{title}</h3>
        <p className="expo-meta">
          {range} {expo.location ? `· ${expo.location}` : ""}
        </p>
        {expo.price && (
          <p className="expo-price">₩{Number(expo.price).toLocaleString()}</p>
        )}
        <button
          className="expo-cta"
          type="button"
          onClick={(e) => {
            e.stopPropagation();           // 카드 onClick과 중복 네비 방지
            onClick?.(expo);
          }}
        >
          View Details
        </button>
      </div>
    </article>
  );
}
