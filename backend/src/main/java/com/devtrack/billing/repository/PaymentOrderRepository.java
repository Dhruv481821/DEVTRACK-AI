package com.devtrack.billing.repository;

import com.devtrack.billing.entity.PaymentOrder;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, UUID> {

  /**
   * Row lock (SELECT ... FOR UPDATE): the browser verify call and the Razorpay webhook can arrive
   * at the same moment for one order; the lock serialises them so fulfilment happens once.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select o from PaymentOrder o where o.razorpayOrderId = :razorpayOrderId")
  Optional<PaymentOrder> findByRazorpayOrderIdForUpdate(
      @Param("razorpayOrderId") String razorpayOrderId);
}
