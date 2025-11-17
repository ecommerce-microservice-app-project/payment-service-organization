package com.selimhorri.app.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.domain.Payment;
import com.selimhorri.app.domain.PaymentStatus;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.PaymentDto;
import com.selimhorri.app.exception.wrapper.PaymentNotFoundException;
import com.selimhorri.app.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImpl Unit Tests")
class PaymentServiceImplTest {
	
	@Mock
	private PaymentRepository paymentRepository;
	
	@Mock
	private RestTemplate restTemplate;
	
	@InjectMocks
	private PaymentServiceImpl paymentService;
	
	private Payment testPayment;
	private PaymentDto testPaymentDto;
	private OrderDto testOrderDto;
	
	@BeforeEach
	void setUp() {
		testOrderDto = OrderDto.builder()
				.orderId(1)
				.orderDesc("Test Order")
				.orderFee(99.99)
				.build();
		
		testPayment = Payment.builder()
				.paymentId(1)
				.orderId(1)
				.isPayed(true)
				.paymentStatus(PaymentStatus.COMPLETED)
				.build();
		
		testPaymentDto = PaymentDto.builder()
				.paymentId(1)
				.isPayed(true)
				.paymentStatus(PaymentStatus.COMPLETED)
				.orderDto(testOrderDto)
				.build();
	}
	
	@Test
	@DisplayName("Should find all payments successfully")
	void testFindAll_Success() {
		// Given
		List<Payment> payments = Arrays.asList(testPayment);
		when(paymentRepository.findAll()).thenReturn(payments);
		when(restTemplate.getForObject(
				eq(AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/1"),
				eq(OrderDto.class)))
				.thenReturn(testOrderDto);
		
		// When
		List<PaymentDto> result = paymentService.findAll();
		
		// Then
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(1, result.get(0).getPaymentId());
		assertNotNull(result.get(0).getOrderDto());
		assertEquals(1, result.get(0).getOrderDto().getOrderId());
		verify(paymentRepository, times(1)).findAll();
		verify(restTemplate, times(1)).getForObject(
				eq(AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/1"),
				eq(OrderDto.class));
	}
	
	@Test
	@DisplayName("Should return empty list when no payments exist")
	void testFindAll_EmptyList() {
		// Given
		when(paymentRepository.findAll()).thenReturn(Collections.emptyList());
		
		// When
		List<PaymentDto> result = paymentService.findAll();
		
		// Then
		assertNotNull(result);
		assertTrue(result.isEmpty());
		verify(paymentRepository, times(1)).findAll();
		verify(restTemplate, never()).getForObject(any(String.class), any(Class.class));
	}
	
	@Test
	@DisplayName("Should find payment by id successfully")
	void testFindById_Success() {
		// Given
		when(paymentRepository.findById(1)).thenReturn(Optional.of(testPayment));
		when(restTemplate.getForObject(
				eq(AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/1"),
				eq(OrderDto.class)))
				.thenReturn(testOrderDto);
		
		// When
		PaymentDto result = paymentService.findById(1);
		
		// Then
		assertNotNull(result);
		assertEquals(1, result.getPaymentId());
		assertEquals(1, result.getOrderDto().getOrderId());
		assertEquals(PaymentStatus.COMPLETED, result.getPaymentStatus());
		assertTrue(result.getIsPayed());
		verify(paymentRepository, times(1)).findById(1);
		verify(restTemplate, times(1)).getForObject(
				eq(AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/1"),
				eq(OrderDto.class));
	}
	
	@Test
	@DisplayName("Should throw PaymentNotFoundException when payment not found")
	void testFindById_NotFound() {
		// Given
		when(paymentRepository.findById(999)).thenReturn(Optional.empty());
		
		// When & Then
		PaymentNotFoundException exception = assertThrows(
				PaymentNotFoundException.class,
				() -> paymentService.findById(999)
		);
		
		assertTrue(exception.getMessage().contains("Payment with id: 999 not found"));
		verify(paymentRepository, times(1)).findById(999);
		verify(restTemplate, never()).getForObject(any(String.class), any(Class.class));
	}
	
	@Test
	@DisplayName("Should save payment successfully")
	void testSave_Success() {
		// Given
		PaymentDto newPaymentDto = PaymentDto.builder()
				.isPayed(false)
				.paymentStatus(PaymentStatus.NOT_STARTED)
				.orderDto(OrderDto.builder().orderId(2).build())
				.build();
		
		Payment savedPayment = Payment.builder()
				.paymentId(2)
				.orderId(2)
				.isPayed(false)
				.paymentStatus(PaymentStatus.NOT_STARTED)
				.build();
		
		when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
		
		// When
		PaymentDto result = paymentService.save(newPaymentDto);
		
		// Then
		assertNotNull(result);
		assertEquals(2, result.getOrderDto().getOrderId());
		assertEquals(PaymentStatus.NOT_STARTED, result.getPaymentStatus());
		assertEquals(false, result.getIsPayed());
		verify(paymentRepository, times(1)).save(any(Payment.class));
	}
	
	@Test
	@DisplayName("Should update payment successfully")
	void testUpdate_Success() {
		// Given
		PaymentDto updatedPaymentDto = PaymentDto.builder()
				.paymentId(1)
				.isPayed(true)
				.paymentStatus(PaymentStatus.COMPLETED)
				.orderDto(OrderDto.builder().orderId(1).build())
				.build();
		
		Payment updatedPayment = Payment.builder()
				.paymentId(1)
				.orderId(1)
				.isPayed(true)
				.paymentStatus(PaymentStatus.COMPLETED)
				.build();
		
		when(paymentRepository.save(any(Payment.class))).thenReturn(updatedPayment);
		
		// When
		PaymentDto result = paymentService.update(updatedPaymentDto);
		
		// Then
		assertNotNull(result);
		assertEquals(1, result.getPaymentId());
		assertEquals(PaymentStatus.COMPLETED, result.getPaymentStatus());
		verify(paymentRepository, times(1)).save(any(Payment.class));
	}
	
	@Test
	@DisplayName("Should delete payment by id successfully")
	void testDeleteById_Success() {
		// Given
		// No need to mock anything for delete
		
		// When
		paymentService.deleteById(1);
		
		// Then
		verify(paymentRepository, times(1)).deleteById(1);
	}
	
	@Test
	@DisplayName("Should handle multiple payments and return distinct list")
	void testFindAll_MultiplePayments() {
		// Given
		Payment payment2 = Payment.builder()
				.paymentId(2)
				.orderId(2)
				.isPayed(false)
				.paymentStatus(PaymentStatus.IN_PROGRESS)
				.build();
		
		List<Payment> payments = Arrays.asList(testPayment, payment2);
		when(paymentRepository.findAll()).thenReturn(payments);
		
		OrderDto orderDto2 = OrderDto.builder()
				.orderId(2)
				.orderDesc("Another Order")
				.build();
		
		when(restTemplate.getForObject(
				eq(AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/1"),
				eq(OrderDto.class)))
				.thenReturn(testOrderDto);
		when(restTemplate.getForObject(
				eq(AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/2"),
				eq(OrderDto.class)))
				.thenReturn(orderDto2);
		
		// When
		List<PaymentDto> result = paymentService.findAll();
		
		// Then
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(1, result.get(0).getPaymentId());
		assertEquals(2, result.get(1).getPaymentId());
		assertEquals(1, result.get(0).getOrderDto().getOrderId());
		assertEquals(2, result.get(1).getOrderDto().getOrderId());
		verify(paymentRepository, times(1)).findAll();
	}

}

