package com.example.gifbottary.infra.portone.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gifbottary.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PortOneConfigController {

	private final PortOneProperties portOneProperties;

	@GetMapping("/portone")
	public ResponseEntity<ApiResponse<PortOneConfigResponse>> getConfig() {
		return ResponseEntity.ok(ApiResponse.ok(new PortOneConfigResponse(
			portOneProperties.getStoreId(),
			portOneProperties.getChannelKey()
		)));
	}
}