package com.devtrack.billing.entity;

import java.time.Period;

/**
 * The sellable Pro passes. Prices live here, server-side only — the client sends a plan name and
 * never an amount. Amounts are in paise (₹1 = 100 paise), as Razorpay requires.
 */
public enum BillingPlan {
  PRO_MONTHLY("Pro — 1 month", 19_900, Period.ofMonths(1)),
  PRO_YEARLY("Pro — 12 months", 199_900, Period.ofYears(1));

  private final String label;
  private final int amountPaise;
  private final Period period;

  BillingPlan(String label, int amountPaise, Period period) {
    this.label = label;
    this.amountPaise = amountPaise;
    this.period = period;
  }

  public String label() {
    return label;
  }

  public int amountPaise() {
    return amountPaise;
  }

  public Period period() {
    return period;
  }
}
