package com.smartpark.service;

import com.smartpark.dto.PaymentRequest;
import com.smartpark.dto.PaymentResponse;
import com.smartpark.model.Booking;
import com.smartpark.model.Payment;
import com.smartpark.repository.BookingRepository;
import com.smartpark.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final LoggingService loggingService;

    @Value("${stripe.api.key:sk_test_placeholder}")
    private String stripeApiKey;

    public PaymentService(PaymentRepository paymentRepository, BookingRepository bookingRepository,
                          BookingService bookingService, LoggingService loggingService) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
        this.loggingService = loggingService;
    }

    /**
     * Create a payment intent. In production, this would call Stripe API.
     * For demo purposes, we simulate the payment intent creation.
     */
    @Transactional
    public PaymentResponse createPaymentIntent(Long userId, PaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Booking does not belong to you");
        }

        // Generate a simulated payment reference
        String paymentRef = "pi_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        String clientSecret = paymentRef + "_secret_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        Payment payment = Payment.builder()
                .booking(booking)
                .paymentRef(paymentRef)
                .gateway("STRIPE")
                .amount(booking.getTotalAmount())
                .currency("INR")
                .status(Payment.PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);

        loggingService.info("PaymentService", "Payment intent created", userId,
                Map.of("paymentRef", paymentRef, "amount", booking.getTotalAmount()));

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .paymentRef(paymentRef)
                .clientSecret(clientSecret)
                .amount(booking.getTotalAmount())
                .currency("INR")
                .status("PENDING")
                .gateway("STRIPE")
                .build();
    }

    /**
     * Confirm payment. In production, this would verify with Stripe webhook.
     * For demo, we auto-confirm and activate the booking.
     */
    @Transactional
    public PaymentResponse confirmPayment(Long userId, PaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Booking does not belong to you");
        }

        Payment payment = paymentRepository.findByBookingId(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for this booking"));

        // Mark payment as successful
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setGatewayResponse("{\"status\":\"succeeded\",\"simulated\":true}");
        paymentRepository.save(payment);

        // Activate the booking
        bookingService.confirmBooking(booking.getId(), userId);

        loggingService.info("PaymentService", "Payment confirmed", userId,
                Map.of("paymentRef", payment.getPaymentRef(), "bookingRef", booking.getBookingRef()));

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .paymentRef(payment.getPaymentRef())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status("SUCCESS")
                .gateway("STRIPE")
                .build();
    }
}
