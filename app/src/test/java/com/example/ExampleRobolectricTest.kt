package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Digital Creator Rewards", appName)
  }

  @Test
  fun `test RewardTracker formula and ad event tracking`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val tracker = com.example.ads.RewardTracker.getInstance(context)

    val initialCount = tracker.adsWatchedCount.value
    val initialEarnings = tracker.totalEarningsNgn.value
    assertEquals(initialCount * 500.0, initialEarnings, 0.01)

    val record = tracker.onUserEarnedReward(adType = "AdMob Rewarded Video")
    val newCount = tracker.adsWatchedCount.value
    val newEarnings = tracker.totalEarningsNgn.value

    assertEquals(initialCount + 1, newCount)
    assertEquals(newCount * 500.0, newEarnings, 0.01)
    assertEquals(500.0, record.rewardAmountNgn, 0.01)
    assertEquals("Sabiu Abdullahi Muhammad", record.beneficiaryName)
    assertEquals("9169878194", record.opayAccount)
    assertEquals(record.id, tracker.rewardsHistory.value.first().id)
  }

  @Test
  fun `test RewardTracker OPay transfer history`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val tracker = com.example.ads.RewardTracker.getInstance(context)

    // Ensure sufficient balance by recording 4 ads (₦2,000)
    tracker.onUserEarnedReward()
    tracker.onUserEarnedReward()
    tracker.onUserEarnedReward()
    tracker.onUserEarnedReward()

    val balanceBefore = tracker.withdrawableBalanceNgn.value
    val transferAmount = 1000.0
    val tx = tracker.processTransferToOpay(transferAmount)

    org.junit.Assert.assertNotNull(tx)
    assertEquals(1000.0, tx!!.amountNgn, 0.01)
    assertEquals("OPay", tx.bankName)
    assertEquals("Sabiu Abdullahi Muhammad", tx.accountName)
    assertEquals("9169878194", tx.accountNumber)
    assertEquals("SUCCESS (CREDITED)", tx.status)
    assertEquals(balanceBefore - transferAmount, tracker.withdrawableBalanceNgn.value, 0.01)
    assertEquals(tx.id, tracker.transferHistory.value.first().id)
  }

  @Test
  fun `test update and retrieve OPay account details in CreatorManager`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val manager = com.example.data.CreatorManager.getInstance(context)

    assertEquals("Sabiu Abdullahi Muhammad", manager.wallet.value.opayAccountName)
    assertEquals("9169878194", manager.wallet.value.opayAccountNumber)
    assertEquals("OPay", manager.wallet.value.opayBankName)

    manager.updateOpayAccountDetails("Sabiu Abdullahi Muhammad", "9169878194", "OPay")
    assertEquals("Sabiu Abdullahi Muhammad", manager.wallet.value.opayAccountName)
    assertEquals("9169878194", manager.wallet.value.opayAccountNumber)
  }
}
