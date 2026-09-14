package com.eventory.qr.conroller;

import com.eventory.qr.dto.CheckinResponse;
import com.eventory.qr.dto.ScanBody;
import com.eventory.qr.service.CheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 체크인 (QR 스캔 결과 토큰을 전달받아 검증/처리)
 * - 입장 처리 규칙은 CheckinService 에 있고, 관리자 수동 체크인과 공유한다.
 */
@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    @PostMapping("/scan")
    public ResponseEntity<CheckinResponse> scan(@RequestBody @Validated ScanBody body) {
        CheckinResponse response = checkinService.checkInByToken(body.getToken());
        return "OK".equals(response.getStatus())
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }
}
