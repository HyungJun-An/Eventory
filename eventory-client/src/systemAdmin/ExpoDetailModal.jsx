import React from "react";
import { useState, useEffect } from "react";
import { Modal, notification } from "antd";
import SysAdminButton from "../components/SysAdminButton";
import Divider from "../components/Divider";
import "../assets/css/systemAdmin/ExpoDetailModal.css";
import { getExposByManager } from "../api/sysExpoAdminApi"

export const ExpoDetailModal = ({ id = 1, closeModal, openRejectNoti }) => {
  const [firstPage, setFirstPage] = useState(1);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPagesize] = useState(10);
  const [expos, setExpos] = useState([
    {
      id: 1,
      title: "서울 IT 박람회",
      category: "IT",
      createdAt: "2025-08-08T15:28:37",
      status: "PENDING",
    },
    {
      id: 2,
      title: "헬스케어 박람회",
      category: "Healthcare",
      createdAt: "2025-08-08T15:28:37",
      status: "APPROVED",
    },
    {
      id: 3,
      title: " 박람회",
      category: "Healthcare",
      createdAt: "2025-08-08T15:28:37",
      status: "REJECTED",
    },
  ]);

  useEffect(() => {
    // initial fetch
    async function fetchInitData() {
      try {
        let expoData = await getExposByManager(id);
        setExpos(expoData.content);
        setPagesize(expoData.totalPage);
      } catch (error) {}
    }
    fetchInitData();
  }, []);

  useEffect(() => {
    async function fetchDataOnPageChange() {
      try {
        let expoData = await getExposByManager(id, currentPage - 1, 20);
        setExpos(expoData.content);
        setPagesize(expoData.totalPage);
      } catch (error) {}
    }
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

  // const handle = () => {
  //   closeModal()
  // };

  const expoItems = expos.map((expo) => (
    <>
      <div
        className=" expoByManagerTable-row"
        style={{ justifyItems: "center" }}
      >
        <div style={{ justifySelf: "start" }}>{expo.title}</div>
        <div>{expo.category}</div>
        <div>{expo.createdAt.slice(0, 10)}</div>
        <div
          className={
            expo.status === "APPROVED"
              ? "blue"
              : expo.status === "PENDING"
              ? "black"
              : "red"
          }
        >
          {expo.status === "APPROVED"
            ? "승인"
            : expo.status === "PENDING"
            ? "대기"
            : "거절"}
        </div>
        <SysAdminButton text="보기" textColor={"#232323"}></SysAdminButton>
      </div>
      <Divider verticalMargin="1rem"></Divider>
    </>
  ));

  return (
    <>
      <Modal
        title="개최한 박람회"
        open={true}
        // onOk={closeModal}
        // cancelText={"취소"}
        // okText={"확인"}
        footer={null}
        onCancel={closeModal}
        width="60%"
      >
        {/* Header */}
        <div
          style={{
            backgroundColor: "white",
            marginRight: "2vw",
            borderRadius: "1.5rem",
            marginBottom: "5vh",
            marginTop: "3vh",
            minHeight: "45vh",
            maxHeight: "5vh",
            padding: "1rem 0rem",
          }}
        >
          <div
            className="mainThemeColor expoByManagerTable-row"
            style={{ justifyItems: "center" }}
          >
            <div style={{ justifySelf: "start" }}>박람회명</div>
            <div>카테고리</div>
            <div>신청일</div>
            <div>상태</div>
            <div>상세정보</div>
          </div>
          <Divider></Divider>
          {expoItems}
        </div>
        {/* Paging */}
        <div
          className="blue"
          style={{
            display: "flex",
            flexDirection: "row",
            gap: "1rem",
            marginRight: "5vw",
            justifyContent: "center",
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
      </Modal>
    </>
  );
};

export default ExpoDetailModal;
