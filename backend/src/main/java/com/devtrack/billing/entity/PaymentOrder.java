package com.devtrack.billing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/** One Razorpay order per checkout attempt. Becomes PAID exactly once (see BillingService). */
@Entity
@Table(name = "payment_order")
@Getter
@Setter
@NoArgsConstructor
public class PaymentOrder {

  @Id @GeneratedValue @UuidGenerator private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "razorpay_order_id", nullable = false)
  private String razorpayOrderId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BillingPlan plan;

  @Column(name = "amount_paise", nullable = false)
  private int amountPaise;

  @Column(nullable = false)
  private String currency;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status = PaymentStatus.CREATED;

  @Column(name = "razorpay_payment_id")
  private String razorpayPaymentId;

  @Column(name = "paid_at")
  private Instant paidAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PaymentOrder other)) return false;
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
