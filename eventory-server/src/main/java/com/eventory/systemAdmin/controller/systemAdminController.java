package com.eventory.systemAdmin.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eventory.expoAdmin.dto.ManagerRequestDto;
import com.eventory.systemAdmin.dto.ExpoStatusRequestDto;
import com.eventory.systemAdmin.dto.SysExpoAdminResponseDto;
import com.eventory.systemAdmin.dto.SysExpoResponseDto;
import com.eventory.systemAdmin.service.SystemAdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sys")
@RequiredArgsConstructor
public class systemAdminController {

	private final SystemAdminService systemAdminService;
	
	@GetMapping("/expos")
	public ResponseEntity<Page<SysExpoResponseDto>> findAllSysExpoPages(@RequestParam(required = false) String status,
																	   @RequestParam(required = false) String title,
																	   @RequestParam(defaultValue = "0") int page,
																	   @RequestParam(defaultValue = "10") int size){
		return ResponseEntity.ok(systemAdminService.findAllSysExpoPages(status, title, page, size));
	}
	
	@PutMapping("/expos/{expoId}/status")
	public ResponseEntity<?> updateExpoStatus(@PathVariable Long expoId,
												   @RequestBody ExpoStatusRequestDto requestDto){
		systemAdminService.updateExpoStatus(expoId, requestDto);
		return ResponseEntity.noContent().build();
	}
	
	@GetMapping("/admins")
	public ResponseEntity<Page<SysExpoAdminResponseDto>> findAllExpoAdminPages(@RequestParam(required = false) String keyword,
																		 @RequestParam(defaultValue = "0") int page,
																		 @RequestParam(defaultValue = "10") int size){
		return ResponseEntity.ok(systemAdminService.findAllExpoAdminPages(keyword, page, size));
	}
	
	@GetMapping("/admins/{adminId}/expos")
	public ResponseEntity<Page<SysExpoResponseDto>> findExpoByExpoAdminPages(@PathVariable Long adminId,
																			 @RequestParam(defaultValue = "0") int page,
			 																 @RequestParam(defaultValue = "10") int size){
		return ResponseEntity.ok(systemAdminService.findExpoByExpoAdminPages(adminId, page, size));
	}
	
	@GetMapping("/admins/{adminId}")
	public ResponseEntity<SysExpoAdminResponseDto> findExpoAdmin(@PathVariable Long adminId){
		return ResponseEntity.ok(systemAdminService.findExpoAdmin(adminId));
	}
	
	@PutMapping("/admins/{adminId}")
	public ResponseEntity<?> updateExpoAdmin(@PathVariable Long adminId,
											 @RequestBody ManagerRequestDto requestDto){
		systemAdminService.updateExpoAdmin(adminId, requestDto);
		return ResponseEntity.noContent().build();
	}
	
	@DeleteMapping("/admins/{adminId}")
	public ResponseEntity<?> deleteExpoAdmin(@PathVariable Long adminId){
		systemAdminService.deleteExpoAdmin(adminId);
		return ResponseEntity.noContent().build();
	}
}
