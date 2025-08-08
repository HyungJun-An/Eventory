package com.eventory.systemAdmin.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eventory.systemAdmin.dto.SysExpoResponseDto;
import com.eventory.systemAdmin.service.SystemAdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sys")
@RequiredArgsConstructor
public class systemAdminController {

	private final SystemAdminService systemAdminService;
	
	@GetMapping("/expos")
	public ResponseEntity<Page<SysExpoResponseDto>> getAllSysExpoPages(@RequestParam(required = false) String status,
																	   @RequestParam(required = false) String title,
																	   @RequestParam(defaultValue = "0") int page,
																	   @RequestParam(defaultValue = "10") int size){
		return ResponseEntity.ok(systemAdminService.getAllSysExpoPages(status, title, page, size));
	}
}
