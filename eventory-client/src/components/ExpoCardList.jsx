import ExpoCard from "./ExpoCard";
import "../assets/css/ExpoCardList.css";

export default function ExpoCardList({ title, items = [], onItemClick }) {
  return (
    <section className="expo-card-list">
      <div className="expo-card-list__header">
        <h2 className="expo-card-list__title">{title}</h2>
      </div>
      <div className="expo-card-list__grid">
        {items.map((e) => (
          <ExpoCard key={e.expoId || e.expo_id || e.id} expo={e} onClick={onItemClick} />
        ))}
      </div>
    </section>
  );
}
