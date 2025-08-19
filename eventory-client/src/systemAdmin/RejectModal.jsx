import React from "react";
import { useState, useEffect } from "react";
import { Modal, notification } from "antd";
import { CheckOutlined } from "@ant-design/icons";
import "../../src/assets/css/systemAdmin/rejectModal.css";
import { rejectExpo } from "../api/sysExpoApi";

export const RejectModal = ({ id, closeModal, openRejectNoti }) => {
  const [rejectReason, setRejectReason] = useState("");
  const [api, contextHolder] = notification.useNotification();

  const handleReject = () => {
    if (rejectReason !== "") {
      console.log(id, rejectReason);
      rejectExpo(id, rejectReason);
      closeModal();
      openRejectNoti("bottomRight");
    }
  };
  const handleRejectReasonChange = (e) => {
    setRejectReason(e.target.value);
  };

  return (
    <>
      {contextHolder}
      <Modal
        title="거절 사유를 입력해주세요"
        open={true}
        onOk={handleReject}
        cancelText={"취소"}
        okText={"확인"}
        onCancel={closeModal}
      >
        <textarea
          name=""
          type="text"
          rows="5"
          style={{
            padding: "1rem",
            width: "95%",
            outline: "none",
          }}
          placeholder="거절 사유를 입력해주세요."
          text={rejectReason}
          onChange={handleRejectReasonChange}
        />
      </Modal>
    </>
  );
};

export default RejectModal;
