package com.eventory.expoAdmin.dto;

import jakarta.validation.constraints.*;

// 예약자 명단
public class ReservationListRequestDto {

    // ALL | CHECKED_IN | NOT_CHECKED_IN
    @NotBlank(message = "status는 필수입니다.")
    @Pattern(regexp = "ALL|CHECKED_IN|NOT_CHECKED_IN", message = "status는 ALL, CHECKED_IN, NOT_CHECKED_IN만 허용됩니다.")
    private String status = "ALL";

    @Size(max = 100, message = "search는 최대 100자까지 허용됩니다.")
    private String search;

    @NotNull @Min(value = 0, message = "page는 0 이상이어야 합니다.")
    private Integer page = 0;

    @NotNull @Min(1) @Max(100)
    private Integer size = 20;

    // createdAt,desc만 허용
    @NotBlank
    @Pattern(regexp = "createdAt,desc", message = "sort는 'createdAt,desc'만 허용됩니다.")
    private String sort = "createdAt,desc";

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSearch() { return (search != null && search.isBlank()) ? null : search; }
    public void setSearch(String search) { this.search = search; }
    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = (sort == null ? "createdAt,desc" : sort); }
}
