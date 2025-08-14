// src/components/Footer.jsx
import React from "react";
import "../assets/css/Footer.css";
import logo from "../assets/demo/eventory_bluewriting.png";

export default function Footer() {
  const stop = (e) => e.preventDefault();

  return (
    <footer className="site-footer">
      <div className="site-footer__container">
        {/* 상단 그리드 */}
        <div className="site-footer__top">
          {/* 브랜드/소개 */}
          <div className="site-footer__brand">
            <img className="site-footer__logo" src={logo} alt="Eventory" />
            <h3 className="site-footer__who">Who we are?</h3>
            <p className="site-footer__desc">
              Eventory is a global ticketing platform for live experiences that
              allows anyone to create, share, find and attend events that fuel
              their passions and enrich their lives.
            </p>
          </div>

          {/* 컬럼들 */}
          <nav className="site-footer__col">
            <h4 className="site-footer__heading">EVENTORY</h4>
            <ul>
              <li><a href="#">About Us</a></li>
              <li><a href="#">Contact Us</a></li>
              <li><a href="#">FAQs</a></li>
            </ul>
          </nav>

          <nav className="site-footer__col">
            <h4 className="site-footer__heading">Help</h4>
            <ul>
              <li><a href="#">Concert Ticketing</a></li>
              <li><a href="#">Account Support</a></li>
              <li><a href="#">Terms &amp; Conditions</a></li>
            </ul>
          </nav>

          <nav className="site-footer__col">
            <h4 className="site-footer__heading">Legal</h4>
            <ul>
              <li><a href="#">Terms of Use</a></li>
              <li><a href="#">Acceptable Use</a></li>
              <li><a href="#">Privacy Policy</a></li>
            </ul>
          </nav>

          <div className="site-footer__col">
            <h4 className="site-footer__heading">Contact</h4>
            <a className="site-footer__mail" href="mailto:contact@likelion.net">
              contact@likelion.net
            </a>
          </div>
        </div>

        {/* 구독 섹션 */}
        <div className="site-footer__subscribe">
          <p className="site-footer__subscribe-text">
            Join our mailing list to stay in the loop with our…
          </p>
          <form className="site-footer__form" onSubmit={stop}>
            <input type="email" placeholder="Enter your email" />
            <button type="submit">Subscribe</button>
          </form>
        </div>

        {/* 하단 바 */}
        <div className="site-footer__bottom">
          <div className="site-footer__copy">© 2025 EVENTORY PVT. LTD.</div>
          <div className="site-footer__links">
            <a href="#">Terms</a>
            <a href="#">Privacy</a>
            <a href="#">Cookies</a>
          </div>
          <div className="site-footer__social">
            <span className="dot" aria-hidden="true" />
            <span className="dot" aria-hidden="true" />
            <span className="dot" aria-hidden="true" />
          </div>
        </div>
      </div>
    </footer>
  );
}
