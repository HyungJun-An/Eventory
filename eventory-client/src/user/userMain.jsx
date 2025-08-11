import React from "react";
import "../assets/css/UserMain.css";

export const UserMainPage = () => {
  return (
    <div className="user-screen">
      <div className="container">
        {/* Hero Section */}
        <img
          className="hero-image"
          alt="Hero"
          src="https://c.animaapp.com/me6sahcjqNWWBm/img/frame-54.png"
        />

        {/* Main Content Image */}
        <img
          className="main-content"
          alt="Main Content"
          src="https://c.animaapp.com/me6sahcjqNWWBm/img/frame-1171279273.png"
        />

        {/* Secondary Content */}
        <img
          className="secondary-content"
          alt="Secondary Content"
          src="https://c.animaapp.com/me6sahcjqNWWBm/img/frame-1171279566.png"
        />

        {/* Benefits Section */}
        <div className="benefits-section">
          <div className="benefits-header">
            <div className="section-title">Our Benefits</div>
            <p className="section-subtitle">
              we promise users with the standard of these 4 services
            </p>
          </div>

          <div className="benefits-grid">
            <div className="benefit-card">
              <img
                className="benefit-icon"
                alt="Instalment Payment"
                src="https://c.animaapp.com/me6sahcjqNWWBm/img/7176685-1--1--1.svg"
              />
              <div className="benefit-title">Instalment Payment!</div>
              <p className="benefit-description">
                You can pay a ticket in 2 portions
                <br />
                throughout a fixed period of time.
                <br />
                Start invoicing for free.
              </p>
            </div>

            <div className="benefit-card">
              <img
                className="benefit-icon"
                alt="Online Booking"
                src="https://c.animaapp.com/me6sahcjqNWWBm/img/3886130-3.svg"
              />
              <div className="benefit-content">
                <div className="benefit-title">Online Booking!</div>
                <p className="benefit-description">
                  You can pay a ticket in 2 portions
                  <br />
                  throughout a fixed period of time.
                  <br />
                  Start invoicing for free.
                </p>
              </div>
            </div>

            <div className="benefit-card">
              <img
                className="benefit-icon"
                alt="Refundable Tickets"
                src="https://c.animaapp.com/me6sahcjqNWWBm/img/wavy-tech-19-single-01-1-1.svg"
              />
              <div className="benefit-title">Refundable Tickets!</div>
              <p className="benefit-description">
                You can pay a ticket in 2 portions
                <br />
                throughout a fixed period of time.
                <br />
                Start invoicing for free.
              </p>
            </div>

            <div className="benefit-card">
              <img
                className="benefit-icon"
                alt="Cheapest Tickets"
                src="https://c.animaapp.com/me6sahcjqNWWBm/img/wavy-gen-04-single-03-1-1.svg"
              />
              <div className="benefit-title">Cheapest Tickets!</div>
              <p className="benefit-description">
                You can pay a ticket in 2 portions
                <br />
                throughout a fixed period of time.
                <br />
                Start invoicing for free.
              </p>
            </div>
          </div>
        </div>

        {/* Steps Section */}
        <div className="steps-wrapper">
          <div className="steps-section">
            <div className="steps-header">
              <div className="steps-info">
                <p className="steps-title">4 Easy Steps To Buy a Ticket!</p>
                <p className="steps-subtitle">
                  Get Familiar with our 4 easy working process
                </p>
              </div>
              <button className="buy-ticket-btn">Buy Ticket</button>
            </div>

            <div className="steps-content">
              <div className="steps-background">
                <div className="steps-grid">
                  <div className="step-item">
                    <img
                      className="step-icon"
                      alt="Choose Concert"
                      src="https://c.animaapp.com/me6sahcjqNWWBm/img/20944608-1-1.svg"
                    />
                    <div className="step-content">
                      <div className="step-title">Choose A Concert</div>
                      <p className="step-description">
                        You can see concert tickets in our website and check
                        which one is good for you.
                      </p>
                    </div>
                  </div>

                  <div className="step-item">
                    <img
                      className="step-icon"
                      alt="Choose Date & Time"
                      src="https://c.animaapp.com/me6sahcjqNWWBm/img/3886130-2-1.svg"
                    />
                    <div className="step-content">
                      <div className="step-title">Choose Date & Time</div>
                      <p className="step-description">
                        You Can check date and time of your favorite concert in
                        our website
                      </p>
                    </div>
                  </div>

                  <div className="step-item">
                    <img
                      className="step-icon"
                      alt="Pay Your Bill"
                      src="https://c.animaapp.com/me6sahcjqNWWBm/img/hand-holding-phone-with-credit-card-screen-man-making-purchase-s.svg"
                    />
                    <div className="step-title">Pay Your Bill</div>
                    <p className="step-description">
                      After choosing your date and time and your preferred seat
                      you can pay ticket online
                    </p>
                  </div>

                  <div className="step-item">
                    <img
                      className="step-icon"
                      alt="Download Ticket"
                      src="https://c.animaapp.com/me6sahcjqNWWBm/img/hand-drawn-online-ticket-illustration-23-2151074791-1.svg"
                    />
                    <div className="step-title">Download Your Ticket!</div>
                    <p className="step-description">
                      After completing checkout process you can download your
                      ticket and get ready for concert
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Footer */}
        <img
          className="footer"
          alt="Footer"
          src="https://c.animaapp.com/me6sahcjqNWWBm/img/footer.svg"
        />

        {/* FAQ Section */}
        <div className="faq-wrapper">
          <div className="faq-section">
            <div className="faq-left">
              <div className="faq-title">Frequent ly Asked Questions</div>
              <div className="faq-contact">
                <div className="contact-info">
                  <div className="contact-item">
                    <div className="contact-icon">
                      <img
                        alt="Mail"
                        src="https://c.animaapp.com/me6sahcjqNWWBm/img/base-mail.svg"
                      />
                    </div>
                    <div className="contact-text">helpcenter@ticketer.com</div>
                  </div>

                  <div className="contact-item">
                    <div className="contact-icon">
                      <img
                        alt="Phone"
                        src="https://c.animaapp.com/me6sahcjqNWWBm/img/base-phone-telephone.svg"
                      />
                    </div>
                    <div className="contact-text">(010) 123-4567</div>
                  </div>
                </div>

                <div className="contact-cta">
                  <div className="cta-title">Still Have Questions?</div>
                  <p className="cta-description">
                    Can't find the answer you're looking for? Please contact our
                    help center.
                  </p>
                  <button className="contact-btn">Contact Us</button>
                </div>
              </div>
            </div>

            <div className="faq-right">
              <div className="faq-list">
                <div className="faq-item expanded">
                  <div className="faq-question">
                    <p className="question-text">
                      I haven't received any order confirmation yet. Did my
                      booking go through?
                    </p>
                    <img
                      className="arrow-icon"
                      alt="Arrow Up"
                      src="https://c.animaapp.com/me6sahcjqNWWBm/img/arrows-up-c.svg"
                    />
                  </div>
                  <p className="faq-answer">
                    Lorem ipsum dolor sit amet consectetur. Eleifend nunc habi
                    loremut egestas. Convallis praesent egestas suscipit
                    hendrerit sem eualiquet feugiat. Amet vulputate rhoncus
                    falectus duis in ultricies pharetra.
                  </p>
                  <div className="divider" />
                </div>

                <div className="faq-item">
                  <div className="faq-question">
                    <p className="question-text">
                      I am not able/do not want to attend an already booked
                      event for personal reasons. Is there a possibility to
                      cancel/rebook the tickets?
                    </p>
                    <img
                      className="arrow-icon"
                      alt="Arrow Down"
                      src="https://c.animaapp.com/me6sahcjqNWWBm/img/arrows-down-c.svg"
                    />
                  </div>
                  <div className="divider" />
                </div>

                <div className="faq-item">
                  <div className="faq-question">
                    <p className="question-text">
                      I lost my e-Ticket. What can I do?
                    </p>
                    <img
                      className="arrow-icon"
                      alt="Arrow Down"
                      src="https://c.animaapp.com/me6sahcjqNWWBm/img/arrows-down-c.svg"
                    />
                  </div>
                  <div className="divider" />
                </div>

                <div className="faq-item">
                  <div className="faq-question">
                    <p className="question-text">
                      An event was canceled/postponed/relocated, and I am not
                      able/do not want to attend the event. Is it possible to
                      cancel my tickets?
                    </p>
                    <img
                      className="arrow-icon"
                      alt="Arrow Down"
                      src="https://c.animaapp.com/me6sahcjqNWWBm/img/arrows-down-c.svg"
                    />
                  </div>
                  <div className="divider" />
                </div>

                <div className="faq-item last">
                  <div className="faq-question">
                    <p className="question-text">
                      I've already ordered tickets and now want to add another
                      one. Is it possible yet to sit together?
                    </p>
                    <img
                      className="arrow-icon"
                      alt="Arrow Down"
                      src="https://c.animaapp.com/me6sahcjqNWWBm/img/arrows-down-c.svg"
                    />
                  </div>
                </div>
              </div>

              <button className="read-more-btn">Read More</button>
            </div>
          </div>
        </div>

        {/* Sponsors Section */}
        <div className="sponsors-section">
          <img
            className="sponsor-logo"
            alt="Minty"
            src="https://c.animaapp.com/me6sahcjqNWWBm/img/v1032-v547-minty-11-logo-1.png"
          />
          <img
            className="sponsor-logo"
            alt="Preview"
            src="https://c.animaapp.com/me6sahcjqNWWBm/img/preview-1.png"
          />
          <div className="sponsor-placeholder" />
          <div className="sponsor-placeholder" />
          <div className="sponsor-placeholder" />
          <div className="sponsor-placeholder" />
          <div className="sponsor-placeholder" />
          <div className="sponsor-placeholder" />
        </div>

        {/* Carousel */}
        <img
          className="carousel"
          alt="Expo carousel"
          src="https://c.animaapp.com/me6sahcjqNWWBm/img/expo-carousel.svg"
        />
      </div>
    </div>
  );
};

export default UserMainPage;