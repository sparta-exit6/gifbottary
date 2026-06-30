package com.example.gifbottary.infra.portone;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.example.gifbottary.common.exception.ErrorCode;
import com.example.gifbottary.common.exception.ServiceException;
import com.example.gifbottary.infra.portone.dto.PortOnePaymentResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortOneClient {

	private final RestClient portOneRestClient;

	public PortOnePaymentResponse getPayment(String portOnePaymentId) {
		try {
			PortOnePaymentResponse response = portOneRestClient.get()
				.uri("/payments/{paymentId}", portOnePaymentId)
				.retrieve()
				.body(PortOnePaymentResponse.class);

			if (response == null) {
				throw new ServiceException(ErrorCode.PAYMENT_NOT_FOUND);
			}

			return response;
		} catch (RestClientException exception) {
			throw new ServiceException(ErrorCode.PAYMENT_NOT_FOUND);
		}
	}
}
