import { useState } from "react";
import "../assets/css/UserMain.css";

export default function HeroSection({ onSearch }) {
  const [keyword, setKeyword] = useState("");
  const [date, setDate] = useState("");
  const [loc, setLoc] = useState("");

  const submit = (e) => {
    e.preventDefault();
    onSearch?.({ keyword, date, loc });
  };

  return (
    <section className="hero">
      <div className="hero__inner">
        <h1 className="hero__title">Book Tickets Of Your Favorite Expo!</h1>
        <p className="hero__subtitle">
          Make Sure Don’t Miss These 5 Up Coming's Expos!
        </p>

        <form className="hero__form" onSubmit={submit}>
          <input
            className="hero__input"
            placeholder="Type of expo"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
          />
          <input
            className="hero__input"
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
          />
          <input
            className="hero__input"
            placeholder="Location"
            value={loc}
            onChange={(e) => setLoc(e.target.value)}
          />
          <button className="hero__btn">Find Expo</button>
        </form>
      </div>
    </section>
  );
}
