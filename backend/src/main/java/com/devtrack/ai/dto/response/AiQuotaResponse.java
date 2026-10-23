package com.devtrack.ai.dto.response;

import com.devtrack.billing.entity.Tier;

/** The caller's AI allowance, so the UI can explain Free vs Pro without hardcoding numbers. */
public record AiQuotaResponse(Tier tier, int dailyLimit, int freeDailyLimit, int proDailyLimit) {}
