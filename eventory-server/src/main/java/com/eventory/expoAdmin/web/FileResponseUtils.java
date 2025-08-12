package com.eventory.expoAdmin.web;

import com.eventory.expoAdmin.dto.FileDownloadDto;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 파일 다운로드 응답(ResponseEntity<Resource>)을 일관된 형태로 만들어주는 유틸.
 * - 서비스는 바이트와 파일명만 돌려준다(FileDownloadDto).
 * - 컨트롤러는 이 유틸로 HTTP 응답 헤더/바디를 조립한다.
 * 계층 분리: 파일 생성은 서비스, HTTP 응답 조립은 웹/컨트롤러.
 */
public final class FileResponseUtils {

    private static final MediaType OCTET_STREAM = MediaType.APPLICATION_OCTET_STREAM;

    private FileResponseUtils() {} // 유틸 클래스: 인스턴스화 방지

    /**
     * FileDownloadDto -> ResponseEntity<Resource>
     * - contentType이 null이면 application/octet-stream 기본값 사용
     * - Content-Disposition: attachment; filename="..."; (UTF-8 안전)
     */
    public static ResponseEntity<Resource> toDownloadResponse(FileDownloadDto file) {
        Objects.requireNonNull(file, "file must not be null");

        // 클라이언트/문서화를 위해 Content-Type을 명확하게 세팅
        String contentType = (file.contentType() != null)
                ? file.contentType()
                : OCTET_STREAM.toString();

        // 브라우저 다운로드 트리거 + UTF-8 파일명 인코딩 처리
        ContentDisposition cd = ContentDisposition.attachment()
                .filename(file.fileName(), StandardCharsets.UTF_8)
                .build();

        // byte[] -> Resource (스프링이 인식 가능한 바디 타입)
        Resource body = new ByteArrayResource(file.content());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .body(body);
    }

    /**
     * 특정 Content-Type을 강제로 지정하고 싶을 때(예: CSV)
     * - DTO의 contentType보다 overrideContentType이 우선.
     */
    public static ResponseEntity<Resource> toDownloadResponse(FileDownloadDto file, String overrideContentType) {
        Objects.requireNonNull(file, "file must not be null");
        String contentType = (overrideContentType != null && !overrideContentType.isBlank())
                ? overrideContentType
                : (file.contentType() != null ? file.contentType() : OCTET_STREAM.toString());

        ContentDisposition cd = ContentDisposition.attachment()
                .filename(file.fileName(), StandardCharsets.UTF_8)
                .build();

        Resource body = new ByteArrayResource(file.content());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .body(body);
    }
}