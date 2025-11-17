package com.selimhorri.app.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.selimhorri.app.domain.Payment;
import com.selimhorri.app.domain.PaymentStatus;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.PaymentDto;

@DisplayName("PaymentMappingHelper Unit Tests")
class PaymentMappingHelperTest {
	
	private Payment testPayment;
	private PaymentDto testPaymentDto;
	
	@BeforeEach
	void setUp() {
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
				.orderDto(OrderDto.builder().orderId(1).build())
				.build();
	}
	
	@Test
	@DisplayName("Should map Payment to PaymentDto successfully")
	void testMapPaymentToDto_Success() {
		// When
		PaymentDto result = PaymentMappingHelper.map(testPayment);
		
		// Then
		assertNotNull(result);
		assertEquals(1, result.getPaymentId());
		assertEquals(1, result.getOrderDto().getOrderId());
		assertEquals(true, result.getIsPayed());
		assertEquals(PaymentStatus.COMPLETED, result.getPaymentStatus());
		assertNotNull(result.getOrderDto());
	}
	
	@Test
	@DisplayName("Should map PaymentDto to Payment successfully")
	void testMapDtoToPayment_Success() {
		// When
		Payment result = PaymentMappingHelper.map(testPaymentDto);
		
		// Then
		assertNotNull(result);
		assertEquals(1, result.getPaymentId());
		assertEquals(1, result.getOrderId());
		assertEquals(true, result.getIsPayed());
		assertEquals(PaymentStatus.COMPLETED, result.getPaymentStatus());
	}
	
	@Test
	@DisplayName("Should maintain bidirectional mapping consistency")
	void testBidirectionalMapping_Consistency() {
		// When - Payment to DTO and back
		PaymentDto mappedDto = PaymentMappingHelper.map(testPayment);
		Payment mappedBackPayment = PaymentMappingHelper.map(mappedDto);
		
		// Then
		assertEquals(testPayment.getPaymentId(), mappedBackPayment.getPaymentId());
		assertEquals(testPayment.getOrderId(), mappedBackPayment.getOrderId());
		assertEquals(testPayment.getIsPayed(), mappedBackPayment.getIsPayed());
		assertEquals(testPayment.getPaymentStatus(), mappedBackPayment.getPaymentStatus());
	}
	
	@Test
	@DisplayName("Should map Payment with different status correctly")
	void testMapPaymentWithDifferentStatus() {
		// Given
		Payment paymentWithDifferentStatus = Payment.builder()
				.paymentId(2)
				.orderId(2)
				.isPayed(false)
				.paymentStatus(PaymentStatus.NOT_STARTED)
				.build();
		
		// When
		PaymentDto result = PaymentMappingHelper.map(paymentWithDifferentStatus);
		
		// Then
		assertNotNull(result);
		assertEquals(2, result.getPaymentId());
		assertEquals(2, result.getOrderDto().getOrderId());
		assertEquals(false, result.getIsPayed());
		assertEquals(PaymentStatus.NOT_STARTED, result.getPaymentStatus());
	}
	
	@Test
	@DisplayName("Should map PaymentDto with different status correctly")
	void testMapDtoWithDifferentStatus() {
		// Given
		PaymentDto paymentDtoWithDifferentStatus = PaymentDto.builder()
				.paymentId(3)
				.isPayed(false)
				.paymentStatus(PaymentStatus.IN_PROGRESS)
				.orderDto(OrderDto.builder().orderId(3).build())
				.build();
		
		// When
		Payment result = PaymentMappingHelper.map(paymentDtoWithDifferentStatus);
		
		// Then
		assertNotNull(result);
		assertEquals(3, result.getPaymentId());
		assertEquals(3, result.getOrderId());
		assertEquals(false, result.getIsPayed());
		assertEquals(PaymentStatus.IN_PROGRESS, result.getPaymentStatus());
	}
	
}

