package com.devtrack.billing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** The three fields Razorpay Checkout hands the browser on success. */
public record VerifyPaymentRequest(
    @NotBlank @Size(max = 100) String razorpayOrderId,
    @NotBlank @Size(max = 100) String razorpayPaymentId,
    @NotBlank @Size(max = 200) String razorpaySignature) {}
