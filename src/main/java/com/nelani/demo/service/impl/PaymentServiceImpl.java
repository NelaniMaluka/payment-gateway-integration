package com.nelani.demo.service.impl;

import com.nelani.demo.dto.PaymentRequestDTO;
import com.nelani.demo.dto.PaymentResponseDTO;
import com.nelani.demo.model.Payment;
import com.nelani.demo.model.PaymentStatus;
import com.nelani.demo.provider.PaymentProvider;
import com.nelani.demo.repository.PaymentRepository;
import com.nelani.demo.service.PaymentProviderFactory;
import com.nelani.demo.service.PaymentService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
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
    public List<PaymentResponseDTO> getAllPayments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        var paymentsList = paymentRepository.findAll(pageable);

        return null;
    }

    @Override
    public PaymentResponseDTO initializePayment(PaymentRequestDTO request) {

        Payment payment;

        // Check if the payment request exists
        Optional<Payment> existingPayment = paymentRepository.findByOrderId(request.orderId());

        if (existingPayment.isPresent()) {
            payment = existingPayment.get();

            // If the request exists, update it or throw an error accordingly
            switch (payment.getStatus()) {

                case PENDING ->
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "An active payment already exists for this order.");

                case SUCCESS ->
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Order already paid.");

                case EXPIRED ->
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Order already expired.");

                case INITIATING, FAILED ->
                    payment.markInitiating();
            }

        } else {
            // Create new payment
            payment = new Payment(
                    request.orderId(),
                    request.amount(),
                    PaymentStatus.INITIATING,
                    request.provider(),
                    OffsetDateTime
                            .now().plusDays(1));
        }

        paymentRepository.save(payment); // Save the request

        PaymentProvider provider = factory.get(payment.getProvider());

        return provider.createPayment(payment);
    }
}
