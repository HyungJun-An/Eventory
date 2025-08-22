package com.eventory.common.repository;

import com.eventory.common.entity.CheckInLog;
import com.eventory.systemAdmin.dto.ChartResponseDto;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckInLogRepository extends JpaRepository<CheckInLog, Long> {
    // 입장한 티켓 수
    @Query("""
    SELECT COUNT(cl)
    FROM checkInLog cl
    WHERE cl.ticket.qrCode.reservation.expo.expoId = :expoId
    """)
    Long countCheckedInByExpoId(@Param("expoId") Long expoId);

    // 일별
    @Query(value = "SELECT DATE(c.time) AS date, COUNT(*) AS uv " +
            "FROM checkin_log c " +
            "GROUP BY DATE(c.time)",
            nativeQuery = true)
    List<ChartResponseDto> countDailyCheckIn();


	// 주별
    @Query(value = "SELECT YEARWEEK(c.time, 1) AS date, COUNT(*) AS uv " +
            "FROM checkin_log c " +
            "GROUP BY YEARWEEK(c.time, 1) " +
            "ORDER BY date",
            nativeQuery = true)
    List<ChartResponseDto> countWeeklyCheckIn();


	// 월별
	@Query(value = "SELECT DATE_FORMAT(c.time, '%Y-%m') AS date, COUNT(*) AS uv " +
            "FROM checkin_log c " +
            "GROUP BY DATE_FORMAT(c.time, '%Y-%m') " +
            "ORDER BY date",
            nativeQuery = true)
	List<ChartResponseDto> countMonthlyCheckIn();
}
