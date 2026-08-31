package com.example.data

enum class AuthType {
    NONE,
    GUEST,
    EMAIL
}

data class UserSession(
    val authType: AuthType = AuthType.NONE,
    val email: String = "",
    val userName: String = "Guest User",
    val totalQuotaGb: Double = 0.0,
    val usedQuotaGb: Double = 0.0,
    val lastDailyBonusTimeMs: Long = 0L,
    val consecutiveDaysClaimed: Int = 1,
    val totalSessionsCount: Int = 0,
    val isVipMember: Boolean = false,
    val generatedAdRevenueNgn: Double = 350.0 // Creator monetization pool in Nigerian Naira (NGN)
) {
    val remainingQuotaGb: Double
        get() = (totalQuotaGb - usedQuotaGb).coerceAtLeast(0.0)

    val usagePercentage: Float
        get() = if (totalQuotaGb > 0) ((usedQuotaGb / totalQuotaGb) * 100).toFloat().coerceIn(0f, 100f) else 0f
}

data class CreatorMonetizationInfo(
    val accountName: String = "Sabiu Abdullahi Muhammad",
    val accountNumber: String = "9169878194",
    val bankName: String = "OPay Digital Services Limited",
    val totalGeneratedEarningsNgn: Double = 14500.0,
    val pendingPayoutNgn: Double = 3200.0,
    val totalActiveUsersSupported: Int = 128
)
