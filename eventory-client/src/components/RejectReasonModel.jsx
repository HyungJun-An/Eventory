import { useState } from "react";

export default function RejectReasonModal({ open, onClose, onConfirm }) {
  const [reason, setReason] = useState("");

  if (!open) return null;

  const submit = () => {
    if (!reason.trim()) {
      alert("거절 사유를 입력해주세요.");
      return;
    }
    onConfirm(reason);
  };

  return (
    <div className="modal-backdrop">
      <div className="modal small">
        <h3>거절 사유를 입력해주세요</h3>
        <textarea
          rows={5}
          placeholder="거절 사유를 입력해주세요."
          value={reason}
          onChange={(e) => setReason(e.target.value)}
          style={{ width: "100%" }}
        />
        <div className="modal-footer">
          <button className="btn-outline" onClick={onClose}>
            취소
          </button>
          <button className="btn-primary" onClick={submit}>
            확인
          </button>
        </div>
      </div>
    </div>
  );
}
