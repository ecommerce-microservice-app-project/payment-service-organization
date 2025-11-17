package com.selimhorri.app.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.domain.Payment;
import com.selimhorri.app.domain.PaymentStatus;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.PaymentDto;
import com.selimhorri.app.repository.PaymentRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Pruebas de Integración para PaymentService
 * Estas pruebas usan la base de datos real (H2 en memoria)
 * y prueban la integración completa entre capas
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Payment Service Integration Tests")
class PaymentServiceIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private RestTemplate restTemplate;

	@BeforeEach
	void setUp() {
		// Clean database before each test
		paymentRepository.deleteAll();

		// Mock RestTemplate responses for external service calls
		OrderDto mockOrderDto = OrderDto.builder()
				.orderId(1)
				.orderDesc("Test Order")
				.orderFee(99.99)
				.build();

		when(restTemplate.getForObject(
				any(String.class),
				eq(OrderDto.class)))
				.thenReturn(mockOrderDto);
	}

	@Test
	@DisplayName("Should create payment successfully via REST API")
	void testCreatePayment_Success() throws Exception {
		// Given
		PaymentDto paymentDto = PaymentDto.builder()
				.isPayed(true)
				.paymentStatus(PaymentStatus.COMPLETED)
				.orderDto(OrderDto.builder().orderId(1).build())
				.build();

		// When & Then
		mockMvc.perform(post("/api/payments")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(paymentDto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.order.orderId").value(1))
				.andExpect(jsonPath("$.isPayed").value(true))
				.andExpect(jsonPath("$.paymentStatus").value("COMPLETED"));

		// Verify it was saved in database
		assertTrue(paymentRepository.count() > 0);
	}

	@Test
	@DisplayName("Should retrieve payment by id via REST API")
	void testGetPaymentById_Success() throws Exception {
		// Given
		Payment savedPayment = createPaymentInDatabase();

		// When & Then
		mockMvc.perform(get("/api/payments/{paymentId}", savedPayment.getPaymentId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paymentId").value(savedPayment.getPaymentId()))
				.andExpect(jsonPath("$.order.orderId").value(savedPayment.getOrderId()))
				.andExpect(jsonPath("$.order").exists())
				.andExpect(jsonPath("$.order.orderId").exists());
	}

	@Test
	@DisplayName("Should retrieve all payments via REST API")
	void testGetAllPayments_Success() throws Exception {
		// Given - Create multiple payments
		Payment payment1 = Payment.builder()
				.orderId(1)
				.isPayed(true)
				.paymentStatus(PaymentStatus.COMPLETED)
				.build();
		paymentRepository.save(payment1);

		Payment payment2 = Payment.builder()
				.orderId(2)
				.isPayed(false)
				.paymentStatus(PaymentStatus.IN_PROGRESS)
				.build();
		paymentRepository.save(payment2);

		// When & Then
		mockMvc.perform(get("/api/payments"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.collection").isArray())
				.andExpect(jsonPath("$.collection.length()").value(2));
	}

	@Test
	@DisplayName("Should update payment successfully via REST API")
	void testUpdatePayment_Success() throws Exception {
		// Given
		Payment savedPayment = createPaymentInDatabase();

		PaymentDto updatedPaymentDto = PaymentDto.builder()
				.paymentId(savedPayment.getPaymentId())
				.isPayed(false)
				.paymentStatus(PaymentStatus.IN_PROGRESS)
				.orderDto(OrderDto.builder().orderId(savedPayment.getOrderId()).build())
				.build();

		// When & Then
		mockMvc.perform(put("/api/payments")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updatedPaymentDto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paymentId").value(savedPayment.getPaymentId()))
				.andExpect(jsonPath("$.isPayed").value(false))
				.andExpect(jsonPath("$.paymentStatus").value("IN_PROGRESS"));
	}

	@Test
	@DisplayName("Should delete payment successfully via REST API")
	void testDeletePayment_Success() throws Exception {
		// Given
		Payment savedPayment = createPaymentInDatabase();

		// When & Then
		mockMvc.perform(delete("/api/payments/{paymentId}", savedPayment.getPaymentId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(true));

		// Verify it was deleted from database
		assertTrue(paymentRepository.findById(savedPayment.getPaymentId()).isEmpty());
	}

	@Test
	@DisplayName("Should return 400 error when payment not found")
	void testGetPaymentById_NotFound() throws Exception {
		// When & Then
		mockMvc.perform(get("/api/payments/{paymentId}", 999))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Should persist payment with correct data")
	void testPaymentPersistence() throws Exception {
		// Given
		PaymentDto paymentDto = PaymentDto.builder()
				.isPayed(true)
				.paymentStatus(PaymentStatus.COMPLETED)
				.orderDto(OrderDto.builder().orderId(5).build())
				.build();

		// When
		String response = mockMvc.perform(post("/api/payments")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(paymentDto)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		PaymentDto result = objectMapper.readValue(response, PaymentDto.class);

		// Then - Verify payment in database
		Payment dbPayment = paymentRepository.findById(result.getPaymentId()).orElseThrow();
		assertNotNull(dbPayment);
		assertEquals(5, dbPayment.getOrderId());
		assertEquals(true, dbPayment.getIsPayed());
		assertEquals(PaymentStatus.COMPLETED, dbPayment.getPaymentStatus());
	}

	@Test
	@DisplayName("Should retrieve payments with order information")
	void testGetPaymentsWithOrder() throws Exception {
		// Given
		createPaymentInDatabase();

		// When & Then
		mockMvc.perform(get("/api/payments"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.collection[0].order").exists())
				.andExpect(jsonPath("$.collection[0].order.orderId").exists());
	}

	@Test
	@DisplayName("Should handle multiple payments for same order")
	void testMultiplePaymentsForSameOrder() throws Exception {
		// Given
		Payment payment1 = Payment.builder()
				.orderId(1)
				.isPayed(true)
				.paymentStatus(PaymentStatus.COMPLETED)
				.build();

		Payment payment2 = Payment.builder()
				.orderId(1)
				.isPayed(false)
				.paymentStatus(PaymentStatus.NOT_STARTED)
				.build();

		paymentRepository.save(payment1);
		paymentRepository.save(payment2);

		// When & Then
		mockMvc.perform(get("/api/payments"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.collection.length()").value(2))
				.andExpect(jsonPath("$.collection[0].order.orderId").value(1))
				.andExpect(jsonPath("$.collection[1].order.orderId").value(1));
	}

	/**
	 * Helper method to create a payment in the database
	 */
	private Payment createPaymentInDatabase() {
		Payment payment = Payment.builder()
				.orderId(1)
				.isPayed(true)
				.paymentStatus(PaymentStatus.COMPLETED)
				.build();

		return paymentRepository.save(payment);
	}

}

