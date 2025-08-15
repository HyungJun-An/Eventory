import React from "react";
import { useState, useEffect } from "react";

import "../../assets/css/systemAdmin/systemAdmin.css";
import { getExpos, approveExpo, rejectExpo } from "../../api/sysExpoApi";

export const SysExpoList = () => {
  const [statusFilter, setStatusFilter] = useState("ALL");
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
  const [firstPage, setFirstPage] = useState(1);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPagesize] = useState(10);
  const [searchText, setSearchText] = useState("");

  useEffect(() => {
    // initial fetch
    async function fetchData() {
      try {
        let expoData = await getExpos();
    // console.log("data: ", expoData)
    setExpos(expoData.content);
    setPagesize(expoData.totalPage);
      }
      catch (error)
      {
      }
    }
    fetchData()
  }, []);

  useEffect(() => {
    // fetch expos on page change
    
    async function fetchData() {
      try {
        let expoData = await getExpos( "", searchText, currentPage - 1, 20);
    setExpos(expoData.content);
    setPagesize(expoData.totalPage);
      }
      catch (error)
      {
      }
    }
    fetchData()
    
  }, [currentPage]);

  const handleStatusChange = (status) => {
    console.log(status);
    setStatusFilter(status);
  };

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

  const getExpobyTitle = () => {
    // alert(searchText);
    getExpos((title = searchText)); // add statusFilter
  };

  const expoItems = expos.map(
    (expo) =>
      (statusFilter === "ALL" || expo.status === statusFilter) && (
        <>
          <div
            className=" expoTable-row"
            style={{
              alignItems: "center",
            }}
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
            <ExpoStatusButtons
              text="보기"
              textColor={"#232323"}
            ></ExpoStatusButtons>
            <ExpoStatusButtons
              onClick={() => {
                approveExpo(expo.id);
              }}
              disable={expo.status !== "PENDING"}
              text="승인"
              textColor={expo.status === "PENDING" ? "#007BFF" : "white"}
              color={expo.status === "PENDING" ? "white" : "#D9D9D9"}
            ></ExpoStatusButtons>
            <ExpoStatusButtons
              onClick={() => {
                rejectExpo(expo.id);
              }}
              disable={expo.status !== "PENDING"}
              text="거절"
              textColor={expo.status === "PENDING" ? "#FE5C73" : "white"}
              color={expo.status === "PENDING" ? "white" : "#D9D9D9"}
            ></ExpoStatusButtons>
          </div>
          <Divider verticalMargin="1rem"></Divider>
        </>
      )
  );

  return (
    <div className="wrapper">
      <div style={{ marginTop: "5vh", marginLeft: "3vw" }}>
        <div
          style={{
            display: "flex",
            flexDirection: "row",
            alignItems: "center",
          }}
        >
          {/* 전체 대기 승인 거절 버튼그룹 */}
          <div
            className="statusButtonGroup"
            style={{
              display: "flex",
              flexDirection: "row",
              gap: "0vw",
              backgroundColor: "white",
              padding: "0.2rem",
              borderRadius: "0.5rem",
              width: "fit-content",
            }}
          >
            <div
              onClick={() => handleStatusChange("ALL")}
              className={
                statusFilter == "ALL"
                  ? "statusButton-active"
                  : "statusButton-deactive"
              }
            >
              전체
            </div>
            <div
              onClick={() => handleStatusChange("PENDING")}
              className={
                statusFilter == "PENDING"
                  ? "statusButton-active"
                  : "statusButton-deactive"
              }
            >
              대기
            </div>
            <div
              onClick={() => handleStatusChange("APPROVED")}
              className={
                statusFilter == "APPROVED"
                  ? "statusButton-active"
                  : "statusButton-deactive"
              }
            >
              승인
            </div>
            <div
              onClick={() => handleStatusChange("REJECTED")}
              className={
                statusFilter == "REJECTED"
                  ? "statusButton-active"
                  : "statusButton-deactive"
              }
            >
              거절
            </div>
          </div>
          {/* 박람회명 검색창 */}
          <div
            style={{
              marginLeft: "1vw",
              marginRight: "0.5vw",
              color: "#8BA3CB",
              backgroundColor: "white",
              padding: "0.7rem 1.5rem",
              borderRadius: "1.5rem",
            }}
          >
            <input
              name=""
              type="text"
              style={{
                border: "none",
                outline: "none",
              }}
              placeholder="박람회명을 입력해주세요."
              text={searchText}
              onChange={handleSearchTextChange}
            />
          </div>
          <div
            onClick={() => {
              getExpobyTitle;
              setStatusFilter("ALL");
              setCurrentPage(1);
              setFirstPage(1);
            }}
          >
            <img
              style={{ marginTop: "0.5rem", cursor: "pointer" }}
              src="https://img.icons8.com/?size=30&id=e4NkZ7kWAD7f&format=png&color=007bff"
            ></img>
          </div>
        </div>
        <div>Status: {statusFilter}</div>
        <div>CurrentPage: {currentPage}</div>
        {/* Main Wrapper */}
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
          {/* 박람회 헤더 */}
          <div className="mainThemeColor expoTable-row">
            <div style={{ justifySelf: "start" }}>박람회명</div>
            <div>카테고리</div>
            <div>신청일</div>
            <div>상태</div>
            <div>상세정보</div>
            <div>승인</div>
            <div>거절</div>
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
          <div
            style={{ userSelect: "none" }}
            onClick={() => handlePrevNext("next")}
          >
            {"Next >"}
          </div>
        </div>
      </div>
    </div>
  );
};

export function Divider({ verticalMargin = "0.8rem" }) {
  return (
    <hr
      className="mainThemeColor"
      style={{
        borderTop: "1px solid #E6EFF5",
        margin: "0.7rem 2rem 0.7rem 2rem",
        marginTop: verticalMargin,
        marginBottom: verticalMargin,
      }}
      class="dashed"
    ></hr>
  );
}

export function ExpoStatusButtons({
  text,
  textColor,
  color = "white",
  disable,
  onClick,
}) {
  return (
    <button
      onClick={onClick}
      disabled={disable}
      style={{
        outline: "none",
        borderColor: textColor,
        color: textColor,
        padding: "0.4rem 2.5rem",
        borderRadius: "1rem",
        width: "fit-content",
        justifySelf: "center",
        backgroundColor: color,
      }}
    >
      {text}
    </button>
  );
}

export default SysExpoList;
