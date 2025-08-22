import React from "react";
import { useState, useEffect } from "react";
import "../assets/css/systemAdmin/systemAdmin.css";
import "../assets/css/systemAdmin/ExpoManagerManagement.css";

import { Button, message } from "antd";
import Divider from "../components/Divider";
import SysAdminButton from "../components/SysAdminButton";
import ExpoDetailModal from "./ExpoDetailModal";
import { getManagers } from "../api/sysExpoAdminApi";
import ManagerInfoEditModal from "./managerInfoEditModal";
import AdminSidebar from "./adminSidebar";
import SysHeader from "./SysHeader";

export const ExpoManagerManagement = () => {
  const [firstPage, setFirstPage] = useState(1);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPagesize] = useState(10);
  const [showModal, setShowModal] = useState(false);
  const [modalId, setModalId] = useState();
  const [manager, setManager] = useState();
  const [searchText, setSearchText] = useState("");
  const [showManagerEditModal, setShowManagerEditModal] = useState(false);
  const [managers, setManagers] = useState([
    {
      id: 1,
      name: "Admin One",
      phone: "010-1234-5678",
      email: "admin1@example.com",
      createdAt: "2025-08-08T15:28:37",
      lastAppliedAt: "2025-08-15T18:26:37",
    },
    {
      id: 2,
      name: "adminUser",
      phone: "010-1234-5678",
      email: "admin@admin.com",
      createdAt: "2025-08-13T10:59:47",
      lastAppliedAt: "2025-08-08T15:28:37",
    },
  ]);

  useEffect(() => {
    // initial fetch
    async function fetchInitData() {
      try {
        let manager = await getManagers();
        setManagers(manager.content);
        setPagesize(manager.totalPage);
      } catch (error) {}
    }
    fetchInitData();
  }, []);

  async function fetchDataOnPageChange() {
    try {
      let manager = await getManagers(searchText, currentPage - 1, 20);
      setManagers(manager.content);
      setPagesize(manager.totalPage);
    } catch (error) {}
  }

  useEffect(() => {
    fetchDataOnPageChange();
  }, [currentPage]);

  const handlePrevNext = (direction) => {
    if (direction === "next" && currentPage + 1 <= pageSize) {
      if (currentPage === firstPage + 4) {
        setFirstPage((prevPage) => prevPage + 5);
      }
      setCurrentPage((prevPage) => prevPage + 1);
    } else if (direction === "prev" && currentPage - 1 >= 1) {
      if (currentPage === firstPage) {
        setFirstPage((prevPage) => prevPage - 5);
      }
      setCurrentPage((prevPage) => prevPage - 1);
    }
  };

  const handlePageClick = (pageNum) => {
    setCurrentPage(pageNum);
  };

  const handleSearchTextChange = (e) => {
    setSearchText(e.target.value);
  };

  const managerList = managers.map((manager) => (
    <>
      <div
        className=" managerTable-row"
        style={{
          alignItems: "center",
        }}
      >
        <div
          className="pointer"
          onClick={() => {
            setShowManagerEditModal(true);
            setManager(manager);
          }}
        >
          {manager.name}
        </div>
        <div
          className="pointer"
          onClick={() => {
            setShowManagerEditModal(true);
            setManager(manager);
          }}
        >
          {manager.phone}
        </div>
        <div
          className="pointer"
          onClick={() => {
            setShowManagerEditModal(true);
            setManager(manager);
          }}
        >
          {manager.email}
        </div>
        <div>{manager.createdAt.slice(0, 10)}</div>
        <div>{manager.lastAppliedAt.slice(0, 10)}</div>
        <SysAdminButton
          onClick={() => {
            setShowModal(true);
            setModalId(manager.id);
          }}
          text="보기"
          textColor={"#232323"}
          borderColor={"#232323"}
        ></SysAdminButton>
      </div>
      <Divider verticalMargin="1rem"></Divider>
    </>
  ));
  return (
    <>
      <SysHeader></SysHeader>
      {showModal && (
        <ExpoDetailModal
          closeModal={() => setShowModal(false)}
          id={modalId}
        ></ExpoDetailModal>
      )}
      {showManagerEditModal && (
        <ManagerInfoEditModal
          onClose={() => setShowManagerEditModal(false)}
          manager={manager}
        ></ManagerInfoEditModal>
      )}

      <div className="wrapper1">
        <div style={{ display: "flex", flexDirection: "row" }}>
          <AdminSidebar></AdminSidebar>
          <div style={{ marginTop: "5vh", marginLeft: "2vw", flex: "auto" }}>
            {/* 검색어창 */}
            <div
              style={{
                display: "flex",
                flexDirection: "row",
                alignItems: "center",
              }}
            >
              <div
                style={{
                  // marginLeft: "1vw",
                  marginRight: "0.5vw",
                  color: "#8BA3CB",
                  backgroundColor: "white",
                  padding: "0.7rem 1.5rem",
                  borderRadius: "1.5rem",

                  width: "10vw",
                }}
              >
                <input
                  name=""
                  type="text"
                  style={{
                    border: "none",
                    outline: "none",
                  }}
                  placeholder="검색어를 입력해주세요."
                  onChange={handleSearchTextChange}
                />
              </div>
              <div
                onClick={() => {
                  fetchDataOnPageChange();
                }}
              >
                <img
                  style={{ marginTop: "0.5rem", cursor: "pointer" }}
                  src="https://img.icons8.com/?size=30&id=e4NkZ7kWAD7f&format=png&color=007bff"
                ></img>
              </div>
            </div>

            {/*  헤더 */}
            <div
              style={{
                backgroundColor: "white",
                marginRight: "2vw",
                borderRadius: "1.5rem",
                marginBottom: "5vh",
                marginTop: "3vh",
                minHeight: "65vh",
                maxHeight: "65vh",
                padding: "1rem 0rem",
              }}
            >
              <div className="mainThemeColor managerTable-row">
                <div>이름</div>
                <div>전화번호</div>
                <div>이메일</div>
                <div>계정 신청일</div>
                <div>마지막 박람회 신청일</div>
                <div>개최한 박람회</div>
              </div>
              <Divider></Divider>
              {managerList}
            </div>

            {/* Paging */}
            <div
              className="blue"
              style={{
                display: "flex",
                flexDirection: "row",
                gap: "1rem",
                marginRight: "5vw",
                justifyContent: "flex-end",
                alignContent: "center",
                marginBottom: "3vh",
                cursor: "pointer",
                alignItems: "center",
              }}
            >
              <div
                style={{ userSelect: "none" }}
                onClick={() => handlePrevNext("prev")}
              >
                {"< Previous"}
              </div>
              <div
                onClick={() => handlePageClick(firstPage)}
                className={
                  currentPage === firstPage
                    ? "pagingButton-active"
                    : "pagingButton-deactive"
                }
              >
                {firstPage}
              </div>
              {firstPage + 1 < pageSize && (
                <div
                  onClick={() => handlePageClick(firstPage + 1)}
                  className={
                    currentPage === firstPage + 1
                      ? "pagingButton-active"
                      : "pagingButton-deactive"
                  }
                >
                  {firstPage + 1}
                </div>
              )}
              {firstPage + 2 < pageSize && (
                <div
                  onClick={() => handlePageClick(firstPage + 2)}
                  className={
                    currentPage === firstPage + 2
                      ? "pagingButton-active"
                      : "pagingButton-deactive"
                  }
                >
                  {firstPage + 2}
                </div>
              )}
              {firstPage + 3 < pageSize && (
                <div
                  onClick={() => handlePageClick(firstPage + 3)}
                  className={
                    currentPage === firstPage + 3
                      ? "pagingButton-active"
                      : "pagingButton-deactive"
                  }
                >
                  {firstPage + 3}
                </div>
              )}
              {firstPage + 4 < pageSize && (
                <div
                  onClick={() => handlePageClick(firstPage + 4)}
                  className={
                    currentPage === firstPage + 4
                      ? "pagingButton-active"
                      : "pagingButton-deactive"
                  }
                >
                  {firstPage + 4}
                </div>
              )}
              <div
                style={{ userSelect: "none" }}
                onClick={() => handlePrevNext("next")}
              >
                {"Next >"}
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};
export default ExpoManagerManagement;
