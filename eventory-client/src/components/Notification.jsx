import React from "react";
import { useState, useEffect } from "react";
import { notification } from "antd";

export const Notification = () => {
  const [api, contextHolder] = notification.useNotification();
  export const openNotification = (message, placement) => {
    console.log("open rejectNotification");
    api.info({
      message: message,
      placement,
      icon: (
        <CheckOutlined
          style={{
            fontSize: "16px",
            backgroundColor: "#12703A",
            padding: "0.3rem",
            margin: "0rem 0rem 1rem 0rem",
            borderRadius: "50%",
            color: "white",
          }}
        />
      ),
      style: {
        backgroundColor: "#D1ECDD",
        border: "5px solid #1AA053",
      },
    });
  };

  return (
    <>
      {contextHolder}
      <div></div>
    </>
  );
};

export default Notification;
