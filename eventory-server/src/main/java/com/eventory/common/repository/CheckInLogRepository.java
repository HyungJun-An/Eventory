package com.eventory.common.repository;

import com.eventory.common.entity.CheckInLog;
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

}
