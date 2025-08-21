import React from "react";
import ContentManagement from "./sections/content/ContentManagement";
import "../assets/css/content/ContentPage.css";

const ContentPage = () => {
  return (
    <div className="content-page" data-model-id="11486:15758">
      <div className="overlap-wrapper">
        <div className="overlap-3">
          <div className="group-instance-wrapper">
            <ContentManagement />
          </div>
        </div>
      </div>
    </div>
  );
};

export default ContentPage;
