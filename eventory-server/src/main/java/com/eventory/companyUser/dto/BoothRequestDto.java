package com.eventory.companyUser.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BoothRequestDto {

    @NotNull
    private Long expoId;

    @NotBlank
    private String title;

    @NotBlank
    private String imageUrl;

    @NotBlank
    private String location;

    @NotBlank
    private String managerName;

    @NotBlank
    private String department;

    @NotBlank
    private String phone;

    @NotBlank
    private String email;
}
