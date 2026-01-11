package com.nelani.demo.service.impl;

import com.nelani.demo.dto.PaymentRequestDTO;
import com.nelani.demo.dto.PaymentResponseDTO;
import com.nelani.demo.mapper.PaymentMapper;
import com.nelani.demo.model.Payment;
import com.nelani.demo.model.PaymentSortField;
import com.nelani.demo.model.PaymentStatus;
import com.nelani.demo.provider.PaymentProvider;
import com.nelani.demo.repository.PaymentRepository;
import com.nelani.demo.service.PaymentProviderFactory;
import com.nelani.demo.service.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProviderFactory factory;

    public PaymentServiceImpl(PaymentRepository paymentRepository, PaymentProviderFactory factory) {
        this.paymentRepository = paymentRepository;
        this.factory = factory;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponseDTO> getAllPayments(PaymentSortField field, Sort.Direction direction, int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(direction, field.fieldName()));

        var paymentsList = paymentRepository.findAll(pageable);

        return paymentsList.map(payment -> PaymentMapper.toResponseDTO(payment, null, null));
    }

    @Override
    @Transactional
    public PaymentResponseDTO initializePayment(PaymentRequestDTO request) {
        Payment payment;

        // Check if the payment request exists
        Optional<Payment> existingPayment = paymentRepository.findByOrderId(request.orderId());

        if (existingPayment.isPresent()) {
            payment = existingPayment.get();
            payment.expireIfNeeded();
            paymentRepository.save(payment);

            // If the request exists, update it or throw an error accordingly
            if (payment.canBeReinitialized()) {
                payment.markInitiating();
            } else if (payment.getStatus() == PaymentStatus.SUCCESS) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Order already paid.");
            } else if (payment.getStatus() == PaymentStatus.PENDING) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Resume payment.");
            }

        } else {
            // Create new payment
            payment = new Payment(
                    request.orderId(),
                    request.amount(),
                    PaymentStatus.INITIATING,
                    request.provider());

            paymentRepository.save(payment); // Save the request
        }

        // Call the payment provider and create the payment
        final PaymentProvider provider = factory.get(payment.getProvider());
        PaymentResponseDTO responseDTO = provider.createPayment(payment);

        // Update the existing Payment and save it to the DB
        payment.setProviderReference(responseDTO.getClientId());
        payment.markPending(responseDTO.getProvider());
        paymentRepository.save(payment);

        return responseDTO;
    }

    @Override
    @Transactional
    public PaymentResponseDTO resumePayment(String orderId) {
        // Get the payment by orderId
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Payment not found."));

        // Check if the payment is expired
        payment.expireIfNeeded();
        paymentRepository.save(payment);

        if (payment.isExpired()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment session expired.");
        }

        // Only allow the payment if its in Pending State
        if (!payment.canBeResumed()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Payment cannot be resumed. Current status: " + payment.getStatus());
        }

        // Resume the payment
        PaymentProvider provider = factory.get(payment.getProvider());
        if (!provider.supportsResume()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment provider does not support resume");
        }

        return provider.resumePayment(payment);
    }

}
