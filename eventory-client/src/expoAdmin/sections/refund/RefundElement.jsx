import React, { useEffect, useState } from "react";
import api from "../../../api/axiosInstance";
import "../../../assets/css/refund/RefundElement.css";

const RefundElement = ({ expoId, status }) => {
  const [refunds, setRefunds] = useState([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedRefund, setSelectedRefund] = useState(null);
  const [reasonOption, setReasonOption] = useState(""); // 선택된 사유 옵션
  const [customReason, setCustomReason] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    if (!expoId) return;

    const fetchRefunds = async () => {
      try {
        const response = await api.get(`/admin/expos/${expoId}/refund`, {
          params: {
            page: 0,
            size: 7,
            status: status === "ALL" ? undefined : status,
          },
        });
        console.log("refunds fetched:", response.data);
        setRefunds(response.data);
      } catch (error) {
        console.error("환불 조회 실패:", error);
      }
    };

    fetchRefunds();
  }, [expoId, status]);

  useEffect(() => {
    console.log("selectedRefund changed:", selectedRefund);
  }, [selectedRefund]);

  const openModal = (refund) => {
    console.log("openModal refund:", refund);
    setSelectedRefund(refund);
    setReasonOption(""); 
    setCustomReason("");
    setError("");
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
    setSelectedRefund(null);
    setError("");
    setReasonOption("");
    setCustomReason("");
  };

  const handleStatusChange = async (newStatus) => {
    if (!selectedRefund) {
      setError("환불 정보가 없습니다.");
      return;
    }

    let finalReason = "";
    // 반려 선택 시 사유 입력 필수
    if (newStatus === "REJECTED") {
      if (!reasonOption) {
        setError("반려 사유를 선택해주세요.");
        return;
      }
      if (reasonOption === "기타" && customReason.trim() === "") {
        setError("반려 사유를 입력해주세요.");
        return;
      }
      finalReason =
        reasonOption === "기간 초과"
          ? "환불 요청 시점이 환불 가능 기간을 초과하였습니다."
          : reasonOption === "정책 기반"
          ? "환불 정책 기반으로 거절되었습니다."
          : customReason;
    }

    try {
      await api.patch(`/admin/refund/${selectedRefund.refundId}`, {
        status: newStatus,
        reason: finalReason,
      });
      // 로컬 상태 업데이트
      setRefunds((prev) =>
        prev.map((r) =>
          r.refundId === selectedRefund.refundId
            ? { ...r, status: newStatus }
            : r
        )
      );
      closeModal();
    } catch (error) {
      console.error("환불 상태 변경 실패:", error);
      setError("상태 변경 중 오류가 발생했습니다.");
    }
  };

  return (
    <div className="refund-element">
      <div className="navbar">
        <div className="text-wrapper-10">예약 번호</div>

        <div className="text-wrapper-11">SL No</div>

        <div className="text-wrapper-12">결제 수단</div>

        <div className="text-wrapper-13">결제 금액</div>

        <div className="text-wrapper-14">결제 시각</div>

        <div className="text-wrapper-15">환불 사유</div>

        <div className="text-wrapper-16">환불 상태</div>
      </div>

      {refunds.length === 0 && (
        <div className="text-wrapper-27">환불 내역이 없습니다.</div>
      )}

      {refunds.map((refund, index) => (
        <div className="navbar-2" key={`${refund.refundId}-${index}`}>
          <div className="text-wrapper-17">{refund.code}</div>
          <div className="text-wrapper-18">
            {String(index + 1).padStart(2, "0")}
          </div>
          <div className="text-wrapper-19">{refund.method}</div>
          <div className="text-wrapper-20">{refund.amount}</div>
          <div className="text-wrapper-21">
            {new Date(refund.paidAt).toLocaleString()}
          </div>
          <div className="text-wrapper-22">{refund.reason}</div>
          <div
            className="group-5"
            onClick={() => openModal(refund)}
            style={{ cursor: "pointer" }}
          >
            <div className="overlap-group-2">
              <div className="text-wrapper-23">{refund.status}</div>
              <div
                className={`rectangle-${
                  refund.status === "PENDING"
                    ? 3
                    : refund.status === "APPROVED"
                    ? 4
                    : 7
                }`}
              />
            </div>
          </div>
        </div>
      ))}
      {modalOpen && selectedRefund && (
        <div className="modal-backdrop">
          <div className="modal">
            <h3>환불 상태 변경</h3>

            {selectedRefund.status === "PENDING" && (
              <div className="modal-row">
                <label>반려 사유 선택:</label>
                <div>
                  <label>
                    <input
                      type="radio"
                      name="reason"
                      value="기간 초과"
                      checked={reasonOption === "기간 초과"}
                      onChange={(e) => setReasonOption(e.target.value)}
                    />
                    환불 요청 시점이 환불 가능 기간을 초과하였습니다.
                  </label>
                  <label>
                    <input
                      type="radio"
                      name="reason"
                      value="정책 기반"
                      checked={reasonOption === "정책 기반"}
                      onChange={(e) => setReasonOption(e.target.value)}
                    />
                    환불 정책 기반으로 거절되었습니다.
                  </label>
                  <label>
                    <input
                      type="radio"
                      name="reason"
                      value="기타"
                      checked={reasonOption === "기타"}
                      onChange={(e) => setReasonOption(e.target.value)}
                    />
                    기타
                  </label>
                </div>
                {reasonOption === "기타" && (
                  <input
                    type="text"
                    value={customReason}
                    onChange={(e) => setCustomReason(e.target.value)}
                    placeholder="사유 입력"
                  />
                )}
              </div>
            )}

            {error && <div className="modal-error">{error}</div>}

            <div className="modal-buttons">
              <button onClick={() => handleStatusChange("APPROVED")}>
                승인
              </button>
              <button onClick={() => handleStatusChange("REJECTED")}>반려</button>
              <button onClick={closeModal}>취소</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default RefundElement;
