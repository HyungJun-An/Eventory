package com.eventory.auth.dto;

import lombok.*;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeResponse {
    private Long id;
    private String loginId;
    private String name;
    private String email;
    private String phone;
    private String role;
    private long tokenRemainingMs;
    private Instant serverTime;
    private List<String> authorities;
}