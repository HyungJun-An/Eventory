import { useState } from "react";
import { Copy } from "lucide-react";
import Modal from "../../expoAdmin/components/Modal";

/** 발급된 관리자 로그인 정보를 한 번만 보여준다 (서버는 해시만 저장하므로 다시 볼 수 없음) */
export default function CredentialModal({ title, description, credential, onClose }) {
  const [copied, setCopied] = useState("");

  const copy = async (label, value) => {
    try {
      await navigator.clipboard.writeText(value);
      setCopied(label);
    } catch {
      setCopied("");
    }
  };

  return (
    <Modal
      title={title}
      onClose={onClose}
      footer={
        <button className="adm-btn adm-btn--primary" onClick={onClose}>
          확인했습니다
        </button>
      }
    >
      {description && <p className="adm-modal__lead">{description}</p>}
      <div className="adm-cred">
        {[
          ["아이디", credential.loginId],
          ["임시 비밀번호", credential.temporaryPassword],
        ].map(([label, value]) => (
          <div className="adm-cred__row" key={label}>
            <div>
              <span>{label}</span>
              <div>
                <code>{value}</code>
              </div>
            </div>
            <button type="button" className="adm-btn adm-btn--sm adm-btn--outline" onClick={() => copy(label, value)}>
              <Copy size={14} />
              {copied === label ? "복사됨" : "복사"}
            </button>
          </div>
        ))}
      </div>
      <p className="adm-cred__warn">이 창을 닫으면 비밀번호를 다시 볼 수 없습니다. 담당자에게 안전하게 전달해주세요.</p>
    </Modal>
  );
}
