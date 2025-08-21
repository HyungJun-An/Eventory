import React from "react";
import { useState, useEffect } from "react";
import { notification, Modal, Input } from "antd";
import { CheckOutlined } from "@ant-design/icons";
import SysAdminButton from "../components/SysAdminButton";
import { updateManager, deleteManager } from "../api/sysExpoAdminApi"

export const ManagerInfoEditModal = ({ onClose, id, manager }) => {
  const [name, setName] = useState(manager.name);
  const [email, setEmail] = useState(manager.email);
  const [phone, setPhone] = useState(manager.phone);

  const handleSave = () => {
    console.log({ name: name, email: email, phone: phone });
    // save API
    updateManager(manager.id, name, phone, email);
    onClose();
  };

  const handleDelete = () => {
    console.log("delete");
    // Delete API
    deleteManager(manager.id);
    onClose();
  };

  return (
    <>
      <Modal
        title={
          <span style={{ fontWeight: "bold", fontSize: "1.4rem" }}>
            박람회 관리자 정보 수정
          </span>
        }
        open={true}
        cancelText={"취소"}
        onCancel={onClose}
        okText={"확인"}
        footer={null}
      >
        <div>
          <div style={{ marginTop: "6vh" }}>박람회 담당자 이름</div>
          <Input
            style={{ marginTop: "1vh" }}
            value={name}
            placeholder={manager.name}
            onChange={(e) => setName(e.target.value)}
          />
          <div style={{ marginTop: "5vh" }}>박람회 담당자 전화번호</div>
          <Input
            value={phone}
            style={{ marginTop: "1vh" }}
            placeholder={manager.phone}
            onChange={(e) => setPhone(e.target.value)}
          />
          <div style={{ marginTop: "5vh" }}>박람회 담당자 이메일</div>
          <Input
            value={email}
            style={{ marginTop: "1vh" }}
            placeholder={manager.email}
            onChange={(e) => setEmail(e.target.value)}
          />
          <div
            onClick={handleDelete}
            style={{ marginTop: "2vh", color: "#FE5C73" }}
          >
            계정 삭제
          </div>

          <div>
            <div
              style={{
                display: "flex",
                flexDirection: "row",
                marginTop: "8vh",
                gap: "2vw",
              }}
            >
              <SysAdminButton
                padding="0.6rem 2.5rem"
                text="관리자 계정 재전송"
                color={"#FFC107"}
                textColor={"black"}
              ></SysAdminButton>
              <SysAdminButton
                onClick={handleSave}
                padding="0.6rem 5rem"
                text="저장"
                color={"#007BFF"}
                textColor={"white"}
              ></SysAdminButton>
            </div>
          </div>
        </div>
      </Modal>
    </>
  );
};
export default ManagerInfoEditModal;
