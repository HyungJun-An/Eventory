package com.eventory.expoAdmin.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

// 예약자 명단
public class ReservationListResponseDto {

    @NotNull
    private Integer page;

    @NotNull
    private Integer size;

    @NotNull
    private Long totalElements;

    @NotNull
    private Integer totalPages;

    @NotNull
    private List<Item> content;

    public ReservationListResponseDto(Integer page, Integer size, Long totalElements, Integer totalPages, List<Item> content) {
        this.page = page; this.size = size; this.totalElements = totalElements; this.totalPages = totalPages; this.content = content;
    }
    public ReservationListResponseDto() {}

    public Integer getPage() { return page; }
    public Integer getSize() { return size; }
    public Long getTotalElements() { return totalElements; }
    public Integer getTotalPages() { return totalPages; }
    public List<Item> getContent() { return content; }

    public static class Item {
        @NotNull private Long reservationId;
        @NotBlank private String name;          // 예약자명
        @NotBlank private String phone;
        @NotBlank private String code;          // 예약코드
        @NotBlank private String ticketType;    // "유료" | "무료"
        @NotNull private LocalDateTime reservedAt;
        @NotBlank private String status;        // "입장 완료" | "미입장"
        private LocalDateTime lastCheckinAt;    // null 가능
        @NotNull private Integer totalTickets;
        @NotNull private Integer checkedInCount;

        public Item(Long reservationId, String name, String phone, String code, String ticketType,
                    LocalDateTime reservedAt, String status, LocalDateTime lastCheckinAt,
                    Integer totalTickets, Integer checkedInCount) {
            this.reservationId = reservationId;
            this.name = name;
            this.phone = phone;
            this.code = code;
            this.ticketType = ticketType;
            this.reservedAt = reservedAt;
            this.status = status;
            this.lastCheckinAt = lastCheckinAt;
            this.totalTickets = totalTickets;
            this.checkedInCount = checkedInCount;
        }
        public Item() {}

        public Long getReservationId() { return reservationId; }
        public String getName() { return name; }
        public String getPhone() { return phone; }
        public String getCode() { return code; }
        public String getTicketType() { return ticketType; }
        public LocalDateTime getReservedAt() { return reservedAt; }
        public String getStatus() { return status; }
        public LocalDateTime getLastCheckinAt() { return lastCheckinAt; }
        public Integer getTotalTickets() { return totalTickets; }
        public Integer getCheckedInCount() { return checkedInCount; }
    }
}
