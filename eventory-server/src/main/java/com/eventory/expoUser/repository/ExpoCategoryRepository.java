package com.eventory.expoUser.repository;

import com.eventory.common.entity.ExpoCategory;
import com.eventory.common.entity.ExpoCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// ID 제네릭 수정
@Repository
public interface ExpoCategoryRepository extends JpaRepository<ExpoCategory, ExpoCategoryId> {
}
