package com.nelani.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nelani.demo.dto.PaymentRequestDTO;
import com.nelani.demo.dto.PaymentResponseDTO;
import com.nelani.demo.model.PaymentProviderType;
import com.nelani.demo.model.PaymentSortField;
import com.nelani.demo.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@ActiveProfiles("test")
class PaymentControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private PaymentService paymentService;

        @Test
        void PaymentControllerTest_GetAllPayments_returnsPaymentResponseDTOPage() throws Exception {
                // Arrange
                final PaymentResponseDTO response = new PaymentResponseDTO(
                                "pay_123",
                                BigDecimal.valueOf(100L),
                                PaymentProviderType.PAYPAL,
                                "PENDING",
                                OffsetDateTime.now(),
                                OffsetDateTime.now().plusDays(1),
                                null

                );
                final Page<PaymentResponseDTO> resultsList = new PageImpl<>(List.of(response), PageRequest.of(0, 10),
                                1);

                // Mock
                when(paymentService.getAllPayments(any(PaymentSortField.class), any(Sort.Direction.class), anyInt(),
                                anyInt())).thenReturn(resultsList);

                // Act & Assert
                mockMvc.perform(get("/api/payments")
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void PaymentControllerTest_initializePayment_returnsPaymentResponse() throws Exception {
                // Arrange
                final PaymentRequestDTO request = new PaymentRequestDTO(
                                "ORDER-1",
                                BigDecimal.valueOf(100),
                                PaymentProviderType.PAYPAL);

                final PaymentResponseDTO response = new PaymentResponseDTO(
                                "pay_123",
                                BigDecimal.valueOf(100L),
                                PaymentProviderType.PAYPAL,
                                "PENDING",
                                OffsetDateTime.now(),
                                OffsetDateTime.now().plusDays(1),
                                null

                );

                // Mock
                when(paymentService.initializePayment(request)).thenReturn(response);

                // Act & Assert
                mockMvc.perform(post("/api/payments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.orderId").value("pay_123"))
                                .andExpect(jsonPath("$.provider").value("PAYPAL"))
                                .andExpect(jsonPath("$.status").value("PENDING"))
                                .andExpect(jsonPath("$.createdAt").exists())
                                .andExpect(jsonPath("$.expiresAt").exists())
                                .andExpect(jsonPath("$.completedAt").doesNotExist());
        }
}
