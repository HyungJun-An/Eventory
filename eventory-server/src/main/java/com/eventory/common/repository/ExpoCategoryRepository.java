package com.eventory.common.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eventory.common.entity.Expo;
import com.eventory.common.entity.ExpoCategory;
import com.eventory.common.entity.ExpoCategoryId;

@Repository
public interface ExpoCategoryRepository extends JpaRepository<ExpoCategory, ExpoCategoryId> {

	Optional<ExpoCategory> findByExpo(Expo expo);
	
	
}
