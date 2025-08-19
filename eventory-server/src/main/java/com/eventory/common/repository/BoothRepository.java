package com.eventory.common.repository;

import com.eventory.common.entity.Booth;
import com.eventory.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoothRepository extends JpaRepository<Booth, Long> {
    List<Booth> findAllByExpo_ExpoId(Long expoId);

    List<Booth> findAllByUser(User user);
}
