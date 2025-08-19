import React from "react";
import { useState, useEffect } from "react";
import { notification, Modal } from "antd";
import { CheckOutlined } from "@ant-design/icons";

export const ManagerInfoEditModal = ({ onClose }) => {
  return (
    <>
      <Modal
        title="박람회 관리자 정보 수정"
        open={true}
        // onOk={handleReject}
        cancelText={"취소"}
        onCancel={onClose}
        okText={"확인"}
        footer={null}
      ></Modal>
    </>
  );
};
export default ManagerInfoEditModal;
