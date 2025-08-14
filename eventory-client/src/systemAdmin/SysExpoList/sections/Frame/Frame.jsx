import React from "react";
import "./style.css";

export const Frame = () => {
  return (
    <div className="frame">
      <div className="navbar">
        <div className="text-wrapper">박람회명</div>

        <div className="div">상태</div>

        <div className="text-wrapper-2">상세정보</div>

        <div className="text-wrapper-3">신청일</div>

        <div className="text-wrapper-4">카테고리</div>

        <div className="text-wrapper-5">승인</div>

        <div className="text-wrapper-6">거절</div>
      </div>

      <div className="rectangle" />

      <div className="div-2">
        <div className="text-wrapper-7">박람회123</div>

        <div className="text-wrapper-8">2025-08-01</div>

        <div className="text-wrapper-9">가나다</div>

        <div className="text-wrapper-10">대기</div>

        <div className="overlap-group-wrapper">
          <div className="overlap-group">
            <div className="text-wrapper-11">거절</div>

            <div className="rectangle-2" />
          </div>
        </div>

        <div className="overlap-wrapper">
          <div className="overlap-group">
            <div className="text-wrapper-12">승인</div>

            <div className="rectangle-3" />
          </div>
        </div>

        <div className="group-2">
          <div className="overlap-group">
            <div className="text-wrapper-13">보기</div>

            <div className="rectangle-4" />
          </div>
        </div>
      </div>

      <div className="rectangle-5" />

      <div className="div-2">
        <div className="text-wrapper-7">박람회123</div>

        <div className="text-wrapper-8">2025-08-01</div>

        <div className="text-wrapper-9">가나다</div>

        <div className="text-wrapper-14">승인</div>

        <div className="overlap-group-wrapper">
          <div className="overlap-group-2">
            <div className="text-wrapper-16">거절</div>
          </div>
        </div>

        <div className="overlap-wrapper">
          <div className="overlap-group-2">
            <div className="text-wrapper-16">승인</div>
          </div>
        </div>

        <div className="group-2">
          <div className="overlap-group">
            <div className="text-wrapper-13">보기</div>

            <div className="rectangle-4" />
          </div>
        </div>
      </div>

      <div className="rectangle-5" />

      <div className="div-2">
        <div className="text-wrapper-7">박람회123</div>

        <div className="text-wrapper-8">2025-08-01</div>

        <div className="text-wrapper-9">가나다</div>

        <div className="text-wrapper-17">거절</div>

        <div className="overlap-group-wrapper">
          <div className="overlap-group-2">
            <div className="text-wrapper-16">거절</div>
          </div>
        </div>

        <div className="overlap-wrapper">
          <div className="overlap-group-2">
            <div className="text-wrapper-16">승인</div>
          </div>
        </div>

        <div className="group-2">
          <div className="overlap-group">
            <div className="text-wrapper-13">보기</div>

            <div className="rectangle-4" />
          </div>
        </div>
      </div>

      <div className="rectangle-5" />
    </div>
  );
};
