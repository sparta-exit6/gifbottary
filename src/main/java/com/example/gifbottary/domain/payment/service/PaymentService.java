package com.example.gifbottary.domain.payment.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gifbottary.common.exception.ServiceException;
import com.example.gifbottary.domain.payment.dto.request.PaymentCreateRequest;
import com.example.gifbottary.domain.payment.dto.response.PaymentCreateResponse;
import com.example.gifbottary.domain.payment.entity.Payment;
import com.example.gifbottary.domain.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {


}
