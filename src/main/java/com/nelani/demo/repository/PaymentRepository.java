package com.nelani.demo.repository;

import com.nelani.demo.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Override
    Page<Payment> findAll(Pageable pageable);

    Optional<Payment> findByOrderId(String orderId);
}
