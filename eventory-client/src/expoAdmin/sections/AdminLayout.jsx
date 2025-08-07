import React from "react";
import SideBar from "./SideBar";
import Header from "./Header";

const AdminLayout = ({ children }) => {
  return (
    <div className="admin-layout">
      <SideBar />
      <div className="main-content">
        <Header />
        {children}
      </div>
    </div>
  );
};

export default AdminLayout;