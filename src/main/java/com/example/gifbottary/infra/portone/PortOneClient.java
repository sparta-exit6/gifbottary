package com.example.gifbottary.infra.portone;

import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.example.gifbottary.common.exception.ErrorCode;
import com.example.gifbottary.common.exception.ServiceException;
import com.example.gifbottary.infra.portone.dto.PortOnePaymentResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortOneClient {

	private final RestClient portOneRestClient;

	public PortOnePaymentResponse getPayment(String portOnePaymentId) {
		HttpStatusCodeException lastStatusException = null;
		RestClientException lastClientException = null;

		for (int attempt = 1; attempt <= 3; attempt++) {
			try {
				return requestPayment(portOnePaymentId, attempt);
			} catch (HttpStatusCodeException exception) {
				lastStatusException = exception;

				if (exception.getStatusCode() != HttpStatus.NOT_FOUND || attempt == 3) {
					break;
				}

				sleepBeforeRetry();
			} catch (RestClientException exception) {
				lastClientException = exception;
				break;
			}
		}

		if (lastStatusException != null) {
			log.warn(
				"PortOne payment request failed. paymentId={}, status={}, body={}",
				portOnePaymentId,
				lastStatusException.getStatusCode(),
				lastStatusException.getResponseBodyAsString()
			);
			throw new ServiceException(ErrorCode.PAYMENT_NOT_FOUND, "PortOne 결제 정보를 찾을 수 없습니다.");
		}

		log.warn("PortOne payment request failed. paymentId={}", portOnePaymentId, lastClientException);
		throw new ServiceException(ErrorCode.PAYMENT_NOT_FOUND, "PortOne 결제 정보를 찾을 수 없습니다.");
	}

	private PortOnePaymentResponse requestPayment(String portOnePaymentId, int attempt) {
		log.info("Request PortOne payment. paymentId={}, attempt={}", portOnePaymentId, attempt);

		PortOnePaymentResponse response = portOneRestClient.get()
			.uri("/payments/{paymentId}", portOnePaymentId)
			.retrieve()
			.body(PortOnePaymentResponse.class);

		if (response == null) {
			throw new ServiceException(ErrorCode.PAYMENT_NOT_FOUND, "PortOne 결제 정보를 찾을 수 없습니다.");
		}

		return response;
	}

	private void sleepBeforeRetry() {
		try {
			Thread.sleep(700);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
		}
	}
}
