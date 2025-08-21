import { useState } from "react";

export default function BannerManageModal({ open, onClose, banner, onSave }) {
  const [visible, setVisible] = useState(banner?.visible ?? true);
  const [period, setPeriod] = useState({
    startDate: banner?.startDate || "",
    endDate: banner?.endDate || "",
  });

  if (!open) return null;

  const save = () => {
    onSave({ visible, ...period });
  };

  return (
    <div className="modal-backdrop">
      <div className="modal">
        <h3>배너 관리</h3>
        <div className="modal-body">
          <div>
            <p className="label">배너 이미지</p>
            <img
              src={banner?.imageUrl}
              alt="배너"
              className="modal-image"
            />
          </div>

          <div className="field">
            <label>공개 설정</label>
            <input
              type="checkbox"
              checked={visible}
              onChange={(e) => setVisible(e.target.checked)}
            />
          </div>

          <div className="field-row">
            <div>
              <label>공개 시작일</label>
              <input
                type="date"
                value={period.startDate}
                onChange={(e) =>
                  setPeriod((p) => ({ ...p, startDate: e.target.value }))
                }
              />
            </div>
            <div>
              <label>공개 종료일</label>
              <input
                type="date"
                value={period.endDate}
                onChange={(e) =>
                  setPeriod((p) => ({ ...p, endDate: e.target.value }))
                }
              />
            </div>
          </div>
        </div>
        <div className="modal-footer">
          <button className="btn-outline" onClick={onClose}>
            닫기
          </button>
          <button className="btn-primary" onClick={save}>
            저장
          </button>
        </div>
      </div>
    </div>
  );
}
