import React, { useState } from "react";
import SideBar from "./SideBar";
import Header from "./Header";

const AdminLayout = ({ children }) => {
    const [expoId, setExpoId] = useState(null);

    const childrenWithProps = React.Children.map(children, (child) => {
    return React.cloneElement(child, { expoId, setExpoId });
  });

    return (
        <div className="admin-layout">
            <SideBar />
            <div className="main-content">
                <Header expoId={expoId} setExpoId={setExpoId}/>
                {childrenWithProps}
            </div>
        </div>
    );
};

export default AdminLayout;