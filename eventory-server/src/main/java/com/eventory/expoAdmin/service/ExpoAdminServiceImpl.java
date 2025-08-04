package com.eventory.expoAdmin.service;

import com.eventory.expoAdmin.repository.ExpoAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExpoAdminServiceImpl implements ExpoAdminService {

    private final ExpoAdminRepository expoAdminRepository;
}
