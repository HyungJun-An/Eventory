package com.eventory.systemAdmin.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eventory.auth.security.CustomUserPrincipal;
import com.eventory.expoAdmin.dto.ManagerRequestDto;
import com.eventory.systemAdmin.dto.AdminCredentialDto;
import com.eventory.systemAdmin.dto.ExpoApprovalResponseDto;
import com.eventory.systemAdmin.dto.ExpoStatusRequestDto;
import com.eventory.systemAdmin.dto.SysChartResponseDto;
import com.eventory.systemAdmin.dto.SysExpoAdminResponseDto;
import com.eventory.systemAdmin.dto.SysExpoDetailResponseDto;
import com.eventory.systemAdmin.dto.SysExpoResponseDto;
import com.eventory.systemAdmin.dto.SysMeResponseDto;
import com.eventory.systemAdmin.dto.SysStatResponseDto;
import com.eventory.systemAdmin.service.SystemAdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** 시스템관리자 API — SecurityConfig 에서 ROLE_SYSTEM_ADMIN 만 접근 가능 */
@RestController
@RequestMapping("/api/sys")
@RequiredArgsConstructor
public class systemAdminController {

	private final SystemAdminService systemAdminService;

	@GetMapping("/me")
	public ResponseEntity<SysMeResponseDto> findMe(@AuthenticationPrincipal CustomUserPrincipal principal){
		return ResponseEntity.ok(systemAdminService.findMe(principal.getId()));
	}

	@GetMapping("/expos")
	public ResponseEntity<Page<SysExpoResponseDto>> findAllSysExpoPages(@RequestParam(required = false) String status,
																	   @RequestParam(required = false) String title,
																	   @RequestParam(defaultValue = "0") int page,
																	   @RequestParam(defaultValue = "10") int size){
		return ResponseEntity.ok(systemAdminService.findAllSysExpoPages(status, title, page, size));
	}

	@GetMapping("/expos/{expoId}")
	public ResponseEntity<SysExpoDetailResponseDto> findExpoDetail(@PathVariable Long expoId){
		return ResponseEntity.ok(systemAdminService.findExpoDetail(expoId));
	}

	/** 승인·반려 — 첫 승인으로 관리자 계정이 발급되면 응답에 credential(아이디·임시 비밀번호)이 한 번 포함된다 */
	@PutMapping("/expos/{expoId}/status")
	public ResponseEntity<ExpoApprovalResponseDto> updateExpoStatus(@PathVariable Long expoId,
												   @RequestBody ExpoStatusRequestDto requestDto){
		return ResponseEntity.ok(systemAdminService.updateExpoStatus(expoId, requestDto));
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
	public ResponseEntity<Void> updateExpoAdmin(@PathVariable Long adminId,
											 @RequestBody @Valid ManagerRequestDto requestDto){
		systemAdminService.updateExpoAdmin(adminId, requestDto);
		return ResponseEntity.noContent().build();
	}

	/** 임시 비밀번호 재발급 — 새 비밀번호는 응답으로 한 번만 제공 */
	@PostMapping("/admins/{adminId}/password-reset")
	public ResponseEntity<AdminCredentialDto> resetExpoAdminPassword(@PathVariable Long adminId){
		return ResponseEntity.ok(systemAdminService.resetExpoAdminPassword(adminId));
	}

	@DeleteMapping("/admins/{adminId}")
	public ResponseEntity<Void> deleteExpoAdmin(@PathVariable Long adminId){
		systemAdminService.deleteExpoAdmin(adminId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/stats")
	public ResponseEntity<SysStatResponseDto> findSysStat(){
		return ResponseEntity.ok(systemAdminService.findSysStat());
	}

	@GetMapping("/chart")
	public ResponseEntity<SysChartResponseDto> findChart(@RequestParam String period){
		return ResponseEntity.ok(systemAdminService.findChart(period));
	}
}
