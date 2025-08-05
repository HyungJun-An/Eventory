package com.eventory.expoAdmin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Builder
@AllArgsConstructor
public class ExpoResponseDto {
    private Long expoId;
    private Long expoAdminId;
    private String title;
    private String description;
    private Date startDate;
    private Date endDate;
    private String location;
    private Boolean visibility;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime displayStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime displayUpdateDate;
}
