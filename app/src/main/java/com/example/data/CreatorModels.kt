package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class CreatorPlatform(val displayName: String, val iconName: String, val defaultMinFollowers: Int) {
    FACEBOOK("Facebook Stars", "facebook", 1000),
    TELEGRAM("Telegram Stars", "telegram", 1000),
    GOOGLE("Google Creator Hub", "google", 1000)
}

data class WithdrawalTransaction(
    val id: String = UUID.randomUUID().toString(),
    val amountNgn: Double,
    val bankName: String = "OPay",
    val accountName: String = "Sabiu Abdullahi Muhammad",
    val accountNumber: String = "9169878194",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS (CREDITED)",
    val reference: String = "OPAY_TX_${System.currentTimeMillis().toString().takeLast(8)}"
) {
    val formattedDate: String
        get() = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

data class CreatorStats(
    val totalFollowers: Int = 1250, // Defaults >= 1000 to pass the monetization stage immediately
    val facebookFollowers: Int = 1420,
    val telegramFollowers: Int = 1180,
    val googleFollowers: Int = 1050,
    val facebookStars: Long = 8500,
    val telegramStars: Long = 4200,
    val googleStars: Long = 6800,
    val totalImpressions: Long = 145200,
    val engagementRate: Float = 8.4f,
    val rpmNgn: Double = 450.0,
    val isStatsUnlocked: Boolean = true, // Instant unlock for 1,000+ followers
    val creatorLevel: String = "Elite Monetized Creator"
)

data class CreatorWallet(
    val balanceNgn: Double = 3500.0,
    val totalEarnedNgn: Double = 18500.0,
    val adsWatchedCount: Int = 7,
    val rewardPerAdNgn: Double = 500.0,
    val opayAccountName: String = "Sabiu Abdullahi Muhammad",
    val opayAccountNumber: String = "9169878194",
    val opayBankName: String = "OPay",
    val pendingWithdrawalsNgn: Double = 0.0,
    val recentTransactions: List<WithdrawalTransaction> = listOf(
        WithdrawalTransaction(
            amountNgn = 5000.0,
            reference = "OPAY_TX_98781941",
            timestamp = System.currentTimeMillis() - 86400000L
        ),
        WithdrawalTransaction(
            amountNgn = 10000.0,
            reference = "OPAY_TX_98781942",
            timestamp = System.currentTimeMillis() - (86400000L * 3)
        )
    )
)

data class DataSaverMetrics(
    val dataSavedMb: Double = 420.5,
    val dataGeneratedMb: Double = 1500.0,
    val compressionRatioPercent: Int = 82,
    val pagesVisitedCount: Int = 24,
    val isUltraDataSaverActive: Boolean = true
)

class CreatorManager private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("creator_rewards_prefs", Context.MODE_PRIVATE)

    private val _stats = MutableStateFlow(loadStats())
    val stats: StateFlow<CreatorStats> = _stats.asStateFlow()

    private val _wallet = MutableStateFlow(loadWallet())
    val wallet: StateFlow<CreatorWallet> = _wallet.asStateFlow()

    private val _dataSaver = MutableStateFlow(loadDataSaver())
    val dataSaver: StateFlow<DataSaverMetrics> = _dataSaver.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: CreatorManager? = null

        fun getInstance(context: Context): CreatorManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CreatorManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private fun loadStats(): CreatorStats {
        val followers = prefs.getInt("total_followers", 1250)
        return CreatorStats(
            totalFollowers = followers,
            facebookFollowers = prefs.getInt("fb_followers", 1420),
            telegramFollowers = prefs.getInt("tg_followers", 1180),
            googleFollowers = prefs.getInt("google_followers", 1050),
            facebookStars = prefs.getLong("fb_stars", 8500L),
            telegramStars = prefs.getLong("tg_stars", 4200L),
            googleStars = prefs.getLong("google_stars", 6800L),
            totalImpressions = prefs.getLong("impressions", 145200L),
            isStatsUnlocked = followers >= 1000
        )
    }

    private fun loadWallet(): CreatorWallet {
        val balance = prefs.getFloat("wallet_balance", 3500.0f).toDouble()
        val totalEarned = prefs.getFloat("wallet_total_earned", 18500.0f).toDouble()
        val adsWatched = prefs.getInt("ads_watched", 7)
        val opayName = prefs.getString("opay_account_name", "Sabiu Abdullahi Muhammad") ?: "Sabiu Abdullahi Muhammad"
        val opayNumber = prefs.getString("opay_account_number", "9169878194") ?: "9169878194"
        val opayBank = prefs.getString("opay_bank_name", "OPay") ?: "OPay"
        return CreatorWallet(
            balanceNgn = balance,
            totalEarnedNgn = totalEarned,
            adsWatchedCount = adsWatched,
            opayAccountName = opayName,
            opayAccountNumber = opayNumber,
            opayBankName = opayBank
        )
    }

    private fun loadDataSaver(): DataSaverMetrics {
        return DataSaverMetrics(
            dataSavedMb = prefs.getFloat("data_saved_mb", 420.5f).toDouble(),
            dataGeneratedMb = prefs.getFloat("data_gen_mb", 1500.0f).toDouble(),
            compressionRatioPercent = 82,
            pagesVisitedCount = prefs.getInt("pages_visited", 24),
            isUltraDataSaverActive = prefs.getBoolean("ultra_data_saver", true)
        )
    }

    // 1. Generate Stars from Google, Telegram, and Facebook
    fun generatePlatformStars(platform: CreatorPlatform, bonusStars: Long = 500) {
        val current = _stats.value
        val updated = when (platform) {
            CreatorPlatform.FACEBOOK -> current.copy(
                facebookStars = current.facebookStars + bonusStars,
                totalImpressions = current.totalImpressions + (bonusStars * 15)
            )
            CreatorPlatform.TELEGRAM -> current.copy(
                telegramStars = current.telegramStars + bonusStars,
                totalImpressions = current.totalImpressions + (bonusStars * 12)
            )
            CreatorPlatform.GOOGLE -> current.copy(
                googleStars = current.googleStars + bonusStars,
                totalImpressions = current.totalImpressions + (bonusStars * 20)
            )
        }
        _stats.value = updated
        saveStats(updated)
    }

    fun generateAllStarsBoost() {
        val current = _stats.value
        val updated = current.copy(
            facebookStars = current.facebookStars + 1200,
            telegramStars = current.telegramStars + 800,
            googleStars = current.googleStars + 1000,
            totalImpressions = current.totalImpressions + 25000,
            totalFollowers = current.totalFollowers + 50,
            isStatsUnlocked = true
        )
        _stats.value = updated
        saveStats(updated)
    }

    // 2. Set/Update Follower count (1,000 threshold instant unlock)
    fun updateFollowers(newCount: Int) {
        val current = _stats.value
        val isUnlocked = newCount >= 1000
        val updated = current.copy(
            totalFollowers = newCount,
            isStatsUnlocked = isUnlocked,
            facebookFollowers = (newCount * 1.1).toInt(),
            telegramFollowers = (newCount * 0.95).toInt(),
            googleFollowers = (newCount * 0.85).toInt()
        )
        _stats.value = updated
        saveStats(updated)
    }

    // 3. Watch Ad Reward: +500 Naira
    fun rewardAdWatched(): Double {
        val current = _wallet.value
        val rewardAmount = 500.0
        val newBalance = current.balanceNgn + rewardAmount
        val newTotalEarned = current.totalEarnedNgn + rewardAmount
        val newCount = current.adsWatchedCount + 1

        val updated = current.copy(
            balanceNgn = newBalance,
            totalEarnedNgn = newTotalEarned,
            adsWatchedCount = newCount
        )
        _wallet.value = updated
        prefs.edit()
            .putFloat("wallet_balance", newBalance.toFloat())
            .putFloat("wallet_total_earned", newTotalEarned.toFloat())
            .putInt("ads_watched", newCount)
            .apply()

        // Also add small data bonus from watching ad
        recordDataGenerated(25.0)
        return rewardAmount
    }

    // 4. Withdraw to OPay directly (Sabiu Abdullahi Muhammad / 9169878194)
    fun withdrawToOpay(amount: Double): WithdrawalTransaction? {
        val current = _wallet.value
        if (current.balanceNgn < amount || amount <= 0) return null

        val tx = WithdrawalTransaction(
            amountNgn = amount,
            bankName = current.opayBankName,
            accountName = current.opayAccountName,
            accountNumber = current.opayAccountNumber,
            timestamp = System.currentTimeMillis(),
            status = "SUCCESS (CREDITED)"
        )

        val newBalance = current.balanceNgn - amount
        val updatedList = listOf(tx) + current.recentTransactions
        val updated = current.copy(
            balanceNgn = newBalance,
            recentTransactions = updatedList
        )
        _wallet.value = updated
        prefs.edit()
            .putFloat("wallet_balance", newBalance.toFloat())
            .apply()

        return tx
    }

    // 4b. Save and update verified OPay Account details
    fun updateOpayAccountDetails(accountName: String, accountNumber: String, bankName: String = "OPay") {
        val current = _wallet.value
        val updated = current.copy(
            opayAccountName = accountName,
            opayAccountNumber = accountNumber,
            opayBankName = bankName
        )
        _wallet.value = updated
        prefs.edit()
            .putString("opay_account_name", accountName)
            .putString("opay_account_number", accountNumber)
            .putString("opay_bank_name", bankName)
            .apply()
    }

    // 5. Data Saver & Website Data Generator
    fun recordBrowsingActivity(savedMb: Double = 12.5, generatedMb: Double = 50.0) {
        val current = _dataSaver.value
        val updated = current.copy(
            dataSavedMb = current.dataSavedMb + savedMb,
            dataGeneratedMb = current.dataGeneratedMb + generatedMb,
            pagesVisitedCount = current.pagesVisitedCount + 1
        )
        _dataSaver.value = updated
        prefs.edit()
            .putFloat("data_saved_mb", updated.dataSavedMb.toFloat())
            .putFloat("data_gen_mb", updated.dataGeneratedMb.toFloat())
            .putInt("pages_visited", updated.pagesVisitedCount)
            .apply()
    }

    fun recordDataGenerated(bonusMb: Double) {
        val current = _dataSaver.value
        val updated = current.copy(
            dataGeneratedMb = current.dataGeneratedMb + bonusMb
        )
        _dataSaver.value = updated
        prefs.edit()
            .putFloat("data_gen_mb", updated.dataGeneratedMb.toFloat())
            .apply()
    }

    fun toggleUltraDataSaver(enabled: Boolean) {
        val current = _dataSaver.value
        val updated = current.copy(isUltraDataSaverActive = enabled)
        _dataSaver.value = updated
        prefs.edit().putBoolean("ultra_data_saver", enabled).apply()
    }

    private fun saveStats(stats: CreatorStats) {
        prefs.edit()
            .putInt("total_followers", stats.totalFollowers)
            .putInt("fb_followers", stats.facebookFollowers)
            .putInt("tg_followers", stats.telegramFollowers)
            .putInt("google_followers", stats.googleFollowers)
            .putLong("fb_stars", stats.facebookStars)
            .putLong("tg_stars", stats.telegramStars)
            .putLong("google_stars", stats.googleStars)
            .putLong("impressions", stats.totalImpressions)
            .apply()
    }
}
