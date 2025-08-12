package com.eventory.expoUser.repository;

import com.eventory.common.entity.Expo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpoUserRepository extends JpaRepository<Expo, Long> {

    @Query(" SELECT e FROM expo e LEFT JOIN FETCH e.title " +
            "WHERE e.visibility IS TRUE " +
            "AND e.endDate >= :today " +
            "ORDER BY e.startDate ASC")
    List<Expo> findAllVisibleAndNotEnded(@Param("today") LocalDate today);
}