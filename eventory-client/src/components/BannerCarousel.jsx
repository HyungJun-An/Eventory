import { useEffect, useRef, useState } from "react";
import "../assets/css/BannerCarousel.css";

const DEFAULT_INTERVAL = 5000;

export default function BannerCarousel({ images = [], interval = DEFAULT_INTERVAL }) {
  const [index, setIndex] = useState(0);
  const timerRef = useRef(null);

  useEffect(() => {
    if (!images.length) return;
    timerRef.current = setInterval(() => {
      setIndex((prev) => (prev + 1) % images.length);
    }, interval);
    return () => clearInterval(timerRef.current);
  }, [images.length, interval]);

  return (
    <section className="banner-carousel" aria-label="Featured Banners">
  <div className="banner-viewport">
    {images.map((src, i) => (
      <img key={src} src={src} alt={`banner ${i + 1}`}
           className={i === index ? "banner-slide banner-slide--active" : "banner-slide"} />
    ))}

    {/* ← 여기로 이동: 뷰포트 내부 */}
    <div className="banner-dots">
      {images.map((_, i) => (
        <button key={i}
                aria-label={`Go to slide ${i + 1}`}
                className={i === index ? "banner-dot banner-dot--active" : "banner-dot"}
                onClick={() => setIndex(i)} />
      ))}
    </div>
  </div>
</section>

  );
}