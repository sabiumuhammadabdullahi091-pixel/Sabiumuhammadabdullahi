package com.example.ads

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.WithdrawalTransaction
import com.google.android.gms.ads.rewarded.RewardItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Model representing an earned AdMob / Google Mobile Ads reward event.
 */
data class AdRewardRecord(
    val id: String = UUID.randomUUID().toString(),
    val rewardAmountNgn: Double = 500.0,
    val adType: String = "AdMob Rewarded Video",
    val rewardItemType: String = "cash_naira",
    val rewardItemAmount: Int = 500,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "CREDITED (₦500.00)",
    val reference: String = "REW_ADMOB_${System.currentTimeMillis().toString().takeLast(8)}",
    val beneficiaryName: String = "Sabiu Abdullahi Muhammad",
    val opayAccount: String = "9169878194"
) {
    val formattedDate: String
        get() = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault()).format(Date(timestamp))
}

/**
 * RewardTracker listens for AdMob rewarded video events, updates a local counter
 * for 'ads watched', computes total earnings calculated as (adsWatched * 500) Naira,
 * and maintains reactive histories for both rewards and OPay transfers.
 */
class RewardTracker private constructor(private val context: Context) {

    companion object {
        private const val TAG = "RewardTracker"
        private const val PREFS_NAME = "admob_reward_tracker_prefs"
        private const val KEY_ADS_WATCHED = "key_ads_watched_counter"
        private const val KEY_WITHDRAWABLE_BALANCE = "key_withdrawable_balance"
        private const val KEY_REWARDS_HISTORY = "key_rewards_history_json"
        private const val KEY_TRANSFER_HISTORY = "key_transfer_history_json"
        const val REWARD_PER_AD_NGN = 500.0

        @Volatile
        private var INSTANCE: RewardTracker? = null

        fun getInstance(context: Context): RewardTracker {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RewardTracker(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // 1. Counter for ads watched
    private val _adsWatchedCount = MutableStateFlow(loadAdsWatchedCount())
    val adsWatchedCount: StateFlow<Int> = _adsWatchedCount.asStateFlow()

    // 2. Total earnings calculated strictly as (adsWatched * 500) Naira
    private val _totalEarningsNgn = MutableStateFlow(calculateTotalEarnings(loadAdsWatchedCount()))
    val totalEarningsNgn: StateFlow<Double> = _totalEarningsNgn.asStateFlow()

    // 3. Withdrawable balance (Earnings minus completed transfers)
    private val _withdrawableBalanceNgn = MutableStateFlow(loadWithdrawableBalance())
    val withdrawableBalanceNgn: StateFlow<Double> = _withdrawableBalanceNgn.asStateFlow()

    // 4. Rewards History
    private val _rewardsHistory = MutableStateFlow(loadRewardsHistory())
    val rewardsHistory: StateFlow<List<AdRewardRecord>> = _rewardsHistory.asStateFlow()

    // 5. Transfer History
    private val _transferHistory = MutableStateFlow(loadTransferHistory())
    val transferHistory: StateFlow<List<WithdrawalTransaction>> = _transferHistory.asStateFlow()

    // 6. Real-time Status / Event Message
    private val _lastEventMessage = MutableStateFlow("Reward Tracker Active (₦500 / ad)")
    val lastEventMessage: StateFlow<String> = _lastEventMessage.asStateFlow()

    init {
        // Recalculate total earnings on init
        val count = _adsWatchedCount.value
        _totalEarningsNgn.value = calculateTotalEarnings(count)
        Log.d(TAG, "Initialized RewardTracker: $count ads watched, ₦${_totalEarningsNgn.value} total earnings")
    }

    /**
     * Calculates total earnings strictly as (adsWatched * 500) Naira.
     */
    fun calculateTotalEarnings(adsWatched: Int): Double {
        return adsWatched * REWARD_PER_AD_NGN
    }

    /**
     * Callback for AdMob OnUserEarnedRewardListener.
     * When AdMob triggers onUserEarnedReward, this method updates the local counter,
     * recalculates total earnings, logs the reward in Rewards History, and credits the balance.
     */
    fun onUserEarnedReward(rewardItem: RewardItem? = null, adType: String = "AdMob Rewarded Video"): AdRewardRecord {
        val type = rewardItem?.type ?: "Naira Cash Reward"
        val amount = rewardItem?.amount ?: 500

        val newCount = _adsWatchedCount.value + 1
        val newTotalEarnings = calculateTotalEarnings(newCount)
        val newBalance = _withdrawableBalanceNgn.value + REWARD_PER_AD_NGN

        val record = AdRewardRecord(
            rewardAmountNgn = REWARD_PER_AD_NGN,
            adType = adType,
            rewardItemType = type,
            rewardItemAmount = amount,
            timestamp = System.currentTimeMillis(),
            status = "CREDITED (₦500.00)",
            beneficiaryName = "Sabiu Abdullahi Muhammad",
            opayAccount = "9169878194"
        )

        val updatedHistory = listOf(record) + _rewardsHistory.value

        // Update StateFlows
        _adsWatchedCount.value = newCount
        _totalEarningsNgn.value = newTotalEarnings
        _withdrawableBalanceNgn.value = newBalance
        _rewardsHistory.value = updatedHistory
        _lastEventMessage.value = "🎉 ₦500.00 Rewarded! Total: ₦${String.format(Locale.US, "%,.2f", newTotalEarnings)}"

        // Persist locally
        saveState(newCount, newBalance, updatedHistory)

        Log.d(TAG, "onUserEarnedReward: ad watched #$newCount recorded. Total Earnings: ₦$newTotalEarnings")
        return record
    }

    /**
     * Records a transfer / payout to the OPay account (Sabiu Abdullahi Muhammad / 9169878194)
     * and appends it to Transfer History.
     */
    fun processTransferToOpay(amount: Double): WithdrawalTransaction? {
        val currentBalance = _withdrawableBalanceNgn.value
        if (currentBalance < amount || amount <= 0) {
            Log.w(TAG, "processTransferToOpay failed: insufficient balance (have ₦$currentBalance, requested ₦$amount)")
            return null
        }

        val tx = WithdrawalTransaction(
            amountNgn = amount,
            bankName = "OPay",
            accountName = "Sabiu Abdullahi Muhammad",
            accountNumber = "9169878194",
            timestamp = System.currentTimeMillis(),
            status = "SUCCESS (CREDITED)"
        )

        val newBalance = currentBalance - amount
        val updatedTransfers = listOf(tx) + _transferHistory.value

        _withdrawableBalanceNgn.value = newBalance
        _transferHistory.value = updatedTransfers
        _lastEventMessage.value = "💸 ₦${String.format(Locale.US, "%,.2f", amount)} transferred to OPay (9169878194)"

        // Persist transfer history and balance
        saveTransferState(newBalance, updatedTransfers)
        return tx
    }

    // --- Local Persistence Helpers ---

    private fun loadAdsWatchedCount(): Int {
        return prefs.getInt(KEY_ADS_WATCHED, 7) // Default initial seed if new
    }

    private fun loadWithdrawableBalance(): Double {
        val initialCalculated = calculateTotalEarnings(loadAdsWatchedCount())
        return prefs.getFloat(KEY_WITHDRAWABLE_BALANCE, initialCalculated.toFloat()).toDouble()
    }

    private fun saveState(count: Int, balance: Double, history: List<AdRewardRecord>) {
        val jsonArray = JSONArray()
        history.take(50).forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("rewardAmountNgn", item.rewardAmountNgn)
                put("adType", item.adType)
                put("rewardItemType", item.rewardItemType)
                put("rewardItemAmount", item.rewardItemAmount)
                put("timestamp", item.timestamp)
                put("status", item.status)
                put("reference", item.reference)
                put("beneficiaryName", item.beneficiaryName)
                put("opayAccount", item.opayAccount)
            }
            jsonArray.put(obj)
        }

        prefs.edit()
            .putInt(KEY_ADS_WATCHED, count)
            .putFloat(KEY_WITHDRAWABLE_BALANCE, balance.toFloat())
            .putString(KEY_REWARDS_HISTORY, jsonArray.toString())
            .apply()
    }

    private fun saveTransferState(balance: Double, transfers: List<WithdrawalTransaction>) {
        val jsonArray = JSONArray()
        transfers.take(50).forEach { tx ->
            val obj = JSONObject().apply {
                put("id", tx.id)
                put("amountNgn", tx.amountNgn)
                put("bankName", tx.bankName)
                put("accountName", tx.accountName)
                put("accountNumber", tx.accountNumber)
                put("timestamp", tx.timestamp)
                put("status", tx.status)
                put("reference", tx.reference)
            }
            jsonArray.put(obj)
        }

        prefs.edit()
            .putFloat(KEY_WITHDRAWABLE_BALANCE, balance.toFloat())
            .putString(KEY_TRANSFER_HISTORY, jsonArray.toString())
            .apply()
    }

    private fun loadRewardsHistory(): List<AdRewardRecord> {
        val rawJson = prefs.getString(KEY_REWARDS_HISTORY, null)
        if (rawJson.isNullOrEmpty()) {
            // Seed initial sample rewards history based on default 7 ads watched
            val now = System.currentTimeMillis()
            return listOf(
                AdRewardRecord(
                    rewardAmountNgn = 500.0,
                    adType = "AdMob Rewarded Video",
                    timestamp = now - 1800000L,
                    reference = "REW_ADMOB_98781901"
                ),
                AdRewardRecord(
                    rewardAmountNgn = 500.0,
                    adType = "Google Interstitial Sponsor",
                    timestamp = now - 7200000L,
                    reference = "REW_ADMOB_98781902"
                ),
                AdRewardRecord(
                    rewardAmountNgn = 500.0,
                    adType = "AdMob Rewarded Video",
                    timestamp = now - 18000000L,
                    reference = "REW_ADMOB_98781903"
                ),
                AdRewardRecord(
                    rewardAmountNgn = 500.0,
                    adType = "AdMob Rewarded Video",
                    timestamp = now - 36000000L,
                    reference = "REW_ADMOB_98781904"
                ),
                AdRewardRecord(
                    rewardAmountNgn = 500.0,
                    adType = "Google Interstitial Sponsor",
                    timestamp = now - 86400000L,
                    reference = "REW_ADMOB_98781905"
                ),
                AdRewardRecord(
                    rewardAmountNgn = 500.0,
                    adType = "AdMob Rewarded Video",
                    timestamp = now - (86400000L * 2),
                    reference = "REW_ADMOB_98781906"
                ),
                AdRewardRecord(
                    rewardAmountNgn = 500.0,
                    adType = "AdMob Rewarded Video",
                    timestamp = now - (86400000L * 3),
                    reference = "REW_ADMOB_98781907"
                )
            )
        }

        return try {
            val list = mutableListOf<AdRewardRecord>()
            val array = JSONArray(rawJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    AdRewardRecord(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        rewardAmountNgn = obj.optDouble("rewardAmountNgn", 500.0),
                        adType = obj.optString("adType", "AdMob Rewarded Video"),
                        rewardItemType = obj.optString("rewardItemType", "cash_naira"),
                        rewardItemAmount = obj.optInt("rewardItemAmount", 500),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        status = obj.optString("status", "CREDITED (₦500.00)"),
                        reference = obj.optString("reference", "REW_ADMOB"),
                        beneficiaryName = obj.optString("beneficiaryName", "Sabiu Abdullahi Muhammad"),
                        opayAccount = obj.optString("opayAccount", "9169878194")
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse rewards history JSON", e)
            emptyList()
        }
    }

    private fun loadTransferHistory(): List<WithdrawalTransaction> {
        val rawJson = prefs.getString(KEY_TRANSFER_HISTORY, null)
        if (rawJson.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            return listOf(
                WithdrawalTransaction(
                    amountNgn = 1000.0,
                    bankName = "OPay",
                    accountName = "Sabiu Abdullahi Muhammad",
                    accountNumber = "9169878194",
                    timestamp = now - 86400000L,
                    status = "SUCCESS (CREDITED)",
                    reference = "OPAY_TX_98781941"
                ),
                WithdrawalTransaction(
                    amountNgn = 2500.0,
                    bankName = "OPay",
                    accountName = "Sabiu Abdullahi Muhammad",
                    accountNumber = "9169878194",
                    timestamp = now - (86400000L * 3),
                    status = "SUCCESS (CREDITED)",
                    reference = "OPAY_TX_98781942"
                )
            )
        }

        return try {
            val list = mutableListOf<WithdrawalTransaction>()
            val array = JSONArray(rawJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    WithdrawalTransaction(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        amountNgn = obj.optDouble("amountNgn", 0.0),
                        bankName = obj.optString("bankName", "OPay"),
                        accountName = obj.optString("accountName", "Sabiu Abdullahi Muhammad"),
                        accountNumber = obj.optString("accountNumber", "9169878194"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        status = obj.optString("status", "SUCCESS (CREDITED)"),
                        reference = obj.optString("reference", "OPAY_TX")
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse transfer history JSON", e)
            emptyList()
        }
    }
}
