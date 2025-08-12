package com.eventory.expoUser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpoMainPageResponseDto {

    private Long expoId; // 박람회 ID
    private String expoName; // 박람회 이름 (title)
    private String thumbnailUrl;// 썸네일 이미지 (imageUrl)
    private String location; // 장소
    private String startDate; // 시작일 (yyyy-MM-dd)
    private String endDate; // 종료일 (yyyy-MM-dd)
    private List<String> categories; // 카테고리 이름들
}
