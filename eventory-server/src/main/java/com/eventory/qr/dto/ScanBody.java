package com.eventory.qr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ScanBody {
    @NotBlank
    private String token;
}
