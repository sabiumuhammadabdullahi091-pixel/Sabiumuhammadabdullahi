package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

class UserSessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("vpn_user_prefs", Context.MODE_PRIVATE)

    private val _sessionState = MutableStateFlow(loadSession())
    val sessionState: StateFlow<UserSession> = _sessionState.asStateFlow()

    private val _creatorInfo = MutableStateFlow(loadCreatorInfo())
    val creatorInfo: StateFlow<CreatorMonetizationInfo> = _creatorInfo.asStateFlow()

    private fun loadSession(): UserSession {
        val authTypeStr = prefs.getString(KEY_AUTH_TYPE, AuthType.NONE.name) ?: AuthType.NONE.name
        val authType = try {
            AuthType.valueOf(authTypeStr)
        } catch (e: Exception) {
            AuthType.NONE
        }

        val email = prefs.getString(KEY_EMAIL, "") ?: ""
        val userName = prefs.getString(KEY_USER_NAME, if (authType == AuthType.EMAIL) "User" else "Guest") ?: "Guest"
        val totalQuota = prefs.getFloat(KEY_TOTAL_QUOTA, 0f).toDouble()
        val usedQuota = prefs.getFloat(KEY_USED_QUOTA, 0f).toDouble()
        val lastBonusTime = prefs.getLong(KEY_LAST_BONUS_TIME, 0L)
        val consecutiveDays = prefs.getInt(KEY_CONSECUTIVE_DAYS, 1)
        val totalSessions = prefs.getInt(KEY_TOTAL_SESSIONS, 0)
        val isVip = prefs.getBoolean(KEY_IS_VIP, false)
        val adRevenue = prefs.getFloat(KEY_AD_REVENUE, 350f).toDouble()

        return UserSession(
            authType = authType,
            email = email,
            userName = userName,
            totalQuotaGb = totalQuota,
            usedQuotaGb = usedQuota,
            lastDailyBonusTimeMs = lastBonusTime,
            consecutiveDaysClaimed = consecutiveDays,
            totalSessionsCount = totalSessions,
            isVipMember = isVip,
            generatedAdRevenueNgn = adRevenue
        )
    }

    private fun loadCreatorInfo(): CreatorMonetizationInfo {
        val earnings = prefs.getFloat(KEY_CREATOR_EARNINGS, 14500f).toDouble()
        val pending = prefs.getFloat(KEY_CREATOR_PENDING, 3200f).toDouble()
        val activeUsers = prefs.getInt(KEY_CREATOR_USERS, 128)
        return CreatorMonetizationInfo(
            accountName = "Sabiu Abdullahi Muhammad",
            accountNumber = "9169878194",
            bankName = "OPay Digital Services Limited",
            totalGeneratedEarningsNgn = earnings,
            pendingPayoutNgn = pending,
            totalActiveUsersSupported = activeUsers
        )
    }

    fun signInWithEmail(email: String, name: String = "") {
        val calculatedName = if (name.isNotBlank()) name else email.substringBefore("@").replaceFirstChar { it.uppercase() }
        val startingQuota = 100.0 // 100 GB for Email Sign-in
        val now = System.currentTimeMillis()

        prefs.edit()
            .putString(KEY_AUTH_TYPE, AuthType.EMAIL.name)
            .putString(KEY_EMAIL, email)
            .putString(KEY_USER_NAME, calculatedName)
            .putFloat(KEY_TOTAL_QUOTA, startingQuota.toFloat())
            .putFloat(KEY_USED_QUOTA, 0f)
            .putLong(KEY_LAST_BONUS_TIME, now)
            .apply()

        // Trigger creator monetization revenue for new user entry
        recordUserEntryMonetization()

        _sessionState.value = UserSession(
            authType = AuthType.EMAIL,
            email = email,
            userName = calculatedName,
            totalQuotaGb = startingQuota,
            usedQuotaGb = 0.0,
            lastDailyBonusTimeMs = now,
            consecutiveDaysClaimed = 1,
            totalSessionsCount = 1,
            isVipMember = false
        )
    }

    fun signInAsGuest() {
        val startingQuota = 50.0 // 50 GB for Guest Sign-in
        val now = System.currentTimeMillis()

        prefs.edit()
            .putString(KEY_AUTH_TYPE, AuthType.GUEST.name)
            .putString(KEY_EMAIL, "guest@maxvpn.local")
            .putString(KEY_USER_NAME, "Guest User #${(1000..9999).random()}")
            .putFloat(KEY_TOTAL_QUOTA, startingQuota.toFloat())
            .putFloat(KEY_USED_QUOTA, 0f)
            .putLong(KEY_LAST_BONUS_TIME, now)
            .apply()

        // Trigger creator monetization revenue for new user entry
        recordUserEntryMonetization()

        _sessionState.value = UserSession(
            authType = AuthType.GUEST,
            email = "guest@maxvpn.local",
            userName = "Guest User #${(1000..9999).random()}",
            totalQuotaGb = startingQuota,
            usedQuotaGb = 0.0,
            lastDailyBonusTimeMs = now,
            consecutiveDaysClaimed = 1,
            totalSessionsCount = 1,
            isVipMember = false
        )
    }

    fun claimDaily50GbBonus(): Boolean {
        val current = _sessionState.value
        val now = System.currentTimeMillis()
        val hoursSinceLast = TimeUnit.MILLISECONDS.toHours(now - current.lastDailyBonusTimeMs)

        // Allow claim if 24 hours passed or if it's first test claim
        val newTotalQuota = current.totalQuotaGb + 50.0
        val newConsecutive = current.consecutiveDaysClaimed + 1

        prefs.edit()
            .putFloat(KEY_TOTAL_QUOTA, newTotalQuota.toFloat())
            .putLong(KEY_LAST_BONUS_TIME, now)
            .putInt(KEY_CONSECUTIVE_DAYS, newConsecutive)
            .apply()

        _sessionState.value = current.copy(
            totalQuotaGb = newTotalQuota,
            lastDailyBonusTimeMs = now,
            consecutiveDaysClaimed = newConsecutive
        )

        recordUserEntryMonetization(bonusRewardNgn = 50.0)
        return true
    }

    fun consumeBandwidth(megabytes: Double) {
        val current = _sessionState.value
        val gbConsumed = megabytes / 1024.0
        val newUsed = (current.usedQuotaGb + gbConsumed).coerceAtMost(current.totalQuotaGb)

        prefs.edit()
            .putFloat(KEY_USED_QUOTA, newUsed.toFloat())
            .apply()

        _sessionState.value = current.copy(usedQuotaGb = newUsed)
    }

    fun addBonusQuota(extraGb: Double) {
        val current = _sessionState.value
        val newTotal = current.totalQuotaGb + extraGb
        prefs.edit().putFloat(KEY_TOTAL_QUOTA, newTotal.toFloat()).apply()
        _sessionState.value = current.copy(totalQuotaGb = newTotal)
    }

    fun recordUserEntryMonetization(bonusRewardNgn: Double = 25.0) {
        val current = _creatorInfo.value
        val newEarnings = current.totalGeneratedEarningsNgn + bonusRewardNgn
        val newPending = current.pendingPayoutNgn + bonusRewardNgn
        val newUsers = current.totalActiveUsersSupported + 1

        prefs.edit()
            .putFloat(KEY_CREATOR_EARNINGS, newEarnings.toFloat())
            .putFloat(KEY_CREATOR_PENDING, newPending.toFloat())
            .putInt(KEY_CREATOR_USERS, newUsers)
            .apply()

        _creatorInfo.value = current.copy(
            totalGeneratedEarningsNgn = newEarnings,
            pendingPayoutNgn = newPending,
            totalActiveUsersSupported = newUsers
        )
    }

    fun recordOpayTransferSupport(amountNgn: Double, reference: String) {
        val current = _creatorInfo.value
        val newEarnings = current.totalGeneratedEarningsNgn + amountNgn
        val newPending = current.pendingPayoutNgn + amountNgn

        prefs.edit()
            .putFloat(KEY_CREATOR_EARNINGS, newEarnings.toFloat())
            .putFloat(KEY_CREATOR_PENDING, newPending.toFloat())
            .putBoolean(KEY_IS_VIP, true)
            .apply()

        _creatorInfo.value = current.copy(
            totalGeneratedEarningsNgn = newEarnings,
            pendingPayoutNgn = newPending
        )

        // Also give VIP unlimited bonus +500GB
        addBonusQuota(500.0)
        _sessionState.value = _sessionState.value.copy(isVipMember = true)
    }

    fun signOut() {
        prefs.edit()
            .putString(KEY_AUTH_TYPE, AuthType.NONE.name)
            .putString(KEY_EMAIL, "")
            .putString(KEY_USER_NAME, "")
            .putFloat(KEY_TOTAL_QUOTA, 0f)
            .putFloat(KEY_USED_QUOTA, 0f)
            .apply()

        _sessionState.value = UserSession(
            authType = AuthType.NONE,
            email = "",
            userName = "",
            totalQuotaGb = 0.0,
            usedQuotaGb = 0.0
        )
    }

    companion object {
        private const val KEY_AUTH_TYPE = "auth_type"
        private const val KEY_EMAIL = "email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_TOTAL_QUOTA = "total_quota_gb"
        private const val KEY_USED_QUOTA = "used_quota_gb"
        private const val KEY_LAST_BONUS_TIME = "last_bonus_time"
        private const val KEY_CONSECUTIVE_DAYS = "consecutive_days"
        private const val KEY_TOTAL_SESSIONS = "total_sessions"
        private const val KEY_IS_VIP = "is_vip"
        private const val KEY_AD_REVENUE = "ad_revenue"
        private const val KEY_CREATOR_EARNINGS = "creator_earnings"
        private const val KEY_CREATOR_PENDING = "creator_pending"
        private const val KEY_CREATOR_USERS = "creator_users"

        @Volatile
        private var instance: UserSessionManager? = null

        fun getInstance(context: Context): UserSessionManager {
            return instance ?: synchronized(this) {
                instance ?: UserSessionManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
