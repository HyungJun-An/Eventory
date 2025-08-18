import React from "react";
import "../assets/css/WebsiteLogos.css";

const WebsiteLogos = ({
  color,
  size,
  className,
  removebgPreviewClassName,
  removebgPreviewClassNameOverride,
}) => {
  return (
    <div className={`website-logos ${className}`} onClick={()=>window.location.href="/"}>
      <img
        className={`removebg-preview ${removebgPreviewClassName}`}
        alt="Removebg preview"
        src="https://c.animaapp.com/mdwp8vqiW7o1mV/img/----removebg-preview-1-1.png"
      />

      <img
        className={`removebg-preview-2 ${removebgPreviewClassNameOverride}`}
        alt="Removebg preview"
        src="https://c.animaapp.com/mdwp8vqiW7o1mV/img/-------removebg-preview-1-1.png"
      />
    </div>
  );
};

export default WebsiteLogos;