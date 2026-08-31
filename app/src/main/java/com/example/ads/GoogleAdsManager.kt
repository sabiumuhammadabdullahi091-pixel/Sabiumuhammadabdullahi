package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.OnUserEarnedRewardListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GoogleAdsManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "GoogleAdsManager"

        // Official Google Mobile Ads Sample Ad Unit IDs for Test & Development
        const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
        const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

        @Volatile
        private var INSTANCE: GoogleAdsManager? = null

        fun getInstance(context: Context): GoogleAdsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GoogleAdsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null

    private val _isRewardedAdLoaded = MutableStateFlow(false)
    val isRewardedAdLoaded: StateFlow<Boolean> = _isRewardedAdLoaded.asStateFlow()

    private val _isInterstitialAdLoaded = MutableStateFlow(false)
    val isInterstitialAdLoaded: StateFlow<Boolean> = _isInterstitialAdLoaded.asStateFlow()

    private val _isLoadingAd = MutableStateFlow(false)
    val isLoadingAd: StateFlow<Boolean> = _isLoadingAd.asStateFlow()

    private val _adStatusMessage = MutableStateFlow("Google Mobile Ads SDK Ready")
    val adStatusMessage: StateFlow<String> = _adStatusMessage.asStateFlow()

    init {
        try {
            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "Google Mobile Ads initialized: $initializationStatus")
                _adStatusMessage.value = "Google Mobile Ads SDK Initialized"
                loadRewardedAd()
                loadInterstitialAd()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MobileAds", e)
            _adStatusMessage.value = "MobileAds init: ${e.message}"
        }
    }

    /**
     * Preloads a Rewarded Video Ad
     */
    fun loadRewardedAd(onLoaded: (() -> Unit)? = null) {
        _isLoadingAd.value = true
        _adStatusMessage.value = "Loading Google Rewarded Video Ad..."

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            TEST_REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Rewarded Ad failed to load: ${adError.message}")
                    rewardedAd = null
                    _isRewardedAdLoaded.value = false
                    _isLoadingAd.value = false
                    _adStatusMessage.value = "Rewarded Ad Load: ${adError.message}"
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded Ad loaded successfully")
                    rewardedAd = ad
                    _isRewardedAdLoaded.value = true
                    _isLoadingAd.value = false
                    _adStatusMessage.value = "Google Rewarded Ad Ready (+₦500 reward)"
                    setupRewardedAdCallbacks(ad)
                    onLoaded?.invoke()
                }
            }
        )
    }

    private fun setupRewardedAdCallbacks(ad: RewardedAd) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                Log.d(TAG, "Rewarded Ad clicked")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded Ad dismissed")
                rewardedAd = null
                _isRewardedAdLoaded.value = false
                _adStatusMessage.value = "Ad closed. Ready for next reward."
                // Preload the next ad for continuous user engagement
                loadRewardedAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Rewarded Ad failed to show: ${adError.message}")
                rewardedAd = null
                _isRewardedAdLoaded.value = false
                _adStatusMessage.value = "Failed to show Ad: ${adError.message}"
                loadRewardedAd()
            }

            override fun onAdImpression() {
                Log.d(TAG, "Rewarded Ad recorded impression")
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded Ad is showing full screen")
                _adStatusMessage.value = "Playing Google Rewarded Ad..."
            }
        }
    }

    /**
     * Displays the Rewarded Video Ad with AdMob OnUserEarnedRewardListener and RewardTracker.
     */
    fun showRewardedAdWithTracker(
        activity: Activity,
        rewardTracker: RewardTracker,
        onAdClosedOrFailed: () -> Unit = {}
    ) {
        if (rewardedAd != null) {
            val listener = OnUserEarnedRewardListener { rewardItem: RewardItem ->
                Log.d(TAG, "AdMob onUserEarnedReward triggered: ${rewardItem.amount} ${rewardItem.type}")
                rewardTracker.onUserEarnedReward(rewardItem, "AdMob Rewarded Video")
            }
            rewardedAd?.show(activity, listener)
        } else {
            Log.w(TAG, "Rewarded ad is not ready yet. Reloading...")
            _adStatusMessage.value = "Ad preparing... Please try again in a moment."
            loadRewardedAd {
                rewardedAd?.show(activity) { rewardItem ->
                    rewardTracker.onUserEarnedReward(rewardItem, "AdMob Rewarded Video")
                }
            }
            onAdClosedOrFailed()
        }
    }

    /**
     * Displays the Rewarded Video Ad. If successfully completed, onRewardEarned is invoked.
     */
    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: (rewardAmount: Int, rewardType: String) -> Unit,
        onAdClosedOrFailed: () -> Unit
    ) {
        if (rewardedAd != null) {
            rewardedAd?.show(activity) { rewardItem ->
                val amount = rewardItem.amount
                val type = rewardItem.type
                Log.d(TAG, "User earned reward: $amount $type")
                _adStatusMessage.value = "🎉 ₦500.00 Reward Earned!"
                onRewardEarned(amount, type)
            }
        } else {
            Log.w(TAG, "Rewarded ad is not ready yet. Reloading...")
            _adStatusMessage.value = "Ad preparing... Please try again in a moment."
            loadRewardedAd {
                rewardedAd?.show(activity) { rewardItem ->
                    onRewardEarned(rewardItem.amount, rewardItem.type)
                }
            }
            onAdClosedOrFailed()
        }
    }

    /**
     * Preloads an Interstitial Ad
     */
    fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            TEST_INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Interstitial Ad failed to load: ${adError.message}")
                    interstitialAd = null
                    _isInterstitialAdLoaded.value = false
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial Ad loaded")
                    interstitialAd = ad
                    _isInterstitialAdLoaded.value = true
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            interstitialAd = null
                            _isInterstitialAdLoaded.value = false
                            loadInterstitialAd()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            interstitialAd = null
                            _isInterstitialAdLoaded.value = false
                            loadInterstitialAd()
                        }
                    }
                }
            }
        )
    }

    /**
     * Shows Interstitial Ad
     */
    fun showInterstitialAd(activity: Activity, onClosed: () -> Unit = {}) {
        if (interstitialAd != null) {
            interstitialAd?.show(activity)
            onClosed()
        } else {
            loadInterstitialAd()
            onClosed()
        }
    }
}
