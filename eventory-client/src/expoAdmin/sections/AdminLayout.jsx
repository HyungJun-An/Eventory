import React, { useState } from "react";
import SideBar from "./ExpoAdminSideBar";
import Header from "../../components/Header";

const AdminLayout = ({ children }) => {
    const [expoId, setExpoId] = useState(null);

    const childrenWithProps = React.Children.map(children, (child) => {
    return React.cloneElement(child, { expoId, setExpoId });
  });

    return (
        <>
        <SideBar />
        <div className="admin-layout">
        <div className="main-content">
                <Header expoId={expoId} setExpoId={setExpoId}/>
                {childrenWithProps}
            </div>
        </div>
        </>
    );
};

export default AdminLayout;