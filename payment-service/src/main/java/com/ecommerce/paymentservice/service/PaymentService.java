package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.dto.PaymentRequest;
import com.ecommerce.paymentservice.dto.PaymentResponse;
import com.ecommerce.paymentservice.dto.RefundRequest;
import com.ecommerce.paymentservice.exception.PaymentNotFoundException;
import com.ecommerce.paymentservice.exception.PaymentProcessingException;
import com.ecommerce.paymentservice.model.Payment;
import com.ecommerce.paymentservice.model.PaymentStatus;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Mock payment processor.
 * Simulates 90% success rate; real integrations would call Stripe/Razorpay here.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private static final Random RANDOM = new Random();

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for order {} amount {}", request.getOrderId(), request.getAmount());

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .build();

        // Mock: simulate 90% success rate
        boolean success = RANDOM.nextInt(10) != 0;

        if (success) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setMessage("Payment processed successfully via " + request.getPaymentMethod());
            log.info("Payment SUCCESS for order {} txn {}", request.getOrderId(), payment.getTransactionId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setMessage("Payment declined by bank. Please try again.");
            log.warn("Payment FAILED for order {}", request.getOrderId());
        }

        return toDto(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse processRefund(RefundRequest request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + request.getPaymentId()));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new PaymentProcessingException(
                "Cannot refund payment in status: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setMessage("Refunded. Reason: " + request.getReason());
        log.info("Refund processed for payment {}", payment.getId());
        return toDto(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        return toDto(paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + id)));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    private PaymentResponse toDto(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId()).transactionId(p.getTransactionId())
                .orderId(p.getOrderId()).userId(p.getUserId())
                .amount(p.getAmount()).paymentMethod(p.getPaymentMethod())
                .status(p.getStatus()).message(p.getMessage())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
