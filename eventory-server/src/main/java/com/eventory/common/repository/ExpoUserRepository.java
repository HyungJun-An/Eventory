package com.eventory.common.repository;

import com.eventory.common.entity.Expo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpoUserRepository extends JpaRepository<Expo, Long> {

  // 중간 테이블 fetch join으로 카테고리까지 로딩
  @Query("""
      select distinct e
      from expo e
      left join fetch e.expoCategories ec
      left join fetch ec.category c
      where e.visibility = true
        and e.endDate >= :today
      order by e.startDate asc
      """)
  List<Expo> findAllVisibleAndNotEnded(@Param("today") LocalDate today);
}
