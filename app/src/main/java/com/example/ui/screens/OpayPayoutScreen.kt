package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CreatorWallet
import com.example.data.WithdrawalTransaction
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextSecondaryDark
import java.util.Locale

@Composable
fun OpayPayoutScreen(
    wallet: CreatorWallet,
    onWithdrawAmount: (Double) -> WithdrawalTransaction?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedAmount by remember { mutableDoubleStateOf(1000.0) }
    var customAmountInput by remember { mutableStateOf("") }
    var latestReceipt by remember { mutableStateOf<WithdrawalTransaction?>(null) }
    var payoutToast by remember { mutableStateOf<String?>(null) }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied $label: $text", Toast.LENGTH_SHORT).show()
    }

    fun shareReceipt(tx: WithdrawalTransaction) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "💰 OPAY CREATOR SETTLEMENT RECEIPT\n" +
                        "Amount: ₦${String.format(Locale.US, "%,.2f", tx.amountNgn)}\n" +
                        "Bank: ${tx.bankName}\n" +
                        "Account Name: ${tx.accountName}\n" +
                        "Account No: ${tx.accountNumber}\n" +
                        "Status: ${tx.status}\n" +
                        "Reference: ${tx.reference}\n" +
                        "Date: ${tx.formattedDate}"
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share OPay Payout Receipt"))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "DIRECT OPAY SETTLEMENT HUB",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 1.sp
        )
        Text(
            text = "Automated payout to verified creator account in Nigeria",
            fontSize = 12.sp,
            color = TextSecondaryDark
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Official OPay Account Badge Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF00B074).copy(alpha = 0.25f),
                            CyberSurfaceDark
                        )
                    )
                )
                .border(1.5.dp, Color(0xFF00B074), RoundedCornerShape(20.dp))
                .padding(18.dp)
                .testTag("opay_verified_account_card")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00B074).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = Color(0xFF00B074),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "OPAY VERIFIED ACCOUNT",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00B074),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = Color(0xFF00B074),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = "Instant Settlement Engine",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF00B074).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "PRIMARY PAYOUT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00B074)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Account Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = TextSecondaryDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Account Holder: ",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                    Text(
                        text = wallet.opayAccountName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Account Number & Copy
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFF00B074),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Account No: ",
                            fontSize = 12.sp,
                            color = TextSecondaryDark
                        )
                        Text(
                            text = wallet.opayAccountNumber,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyberGreen
                        )
                    }

                    Button(
                        onClick = { copyToClipboard(wallet.opayAccountNumber, "OPay Account") },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00B074),
                            contentColor = Color.White
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Copy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Balance & Fast Cashout Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WITHDRAWABLE BALANCE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberAmber,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = String.format(Locale.US, "₦%,.2f", wallet.balanceNgn),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyberGreen
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Total: ₦${String.format(Locale.US, "%,.0f", wallet.totalEarnedNgn)}",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Select Cashout Amount:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Amount selector buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(500.0, 1000.0, 2000.0, 5000.0).forEach { amount ->
                        val isSelected = selectedAmount == amount && customAmountInput.isEmpty()
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF00B074).copy(alpha = 0.25f) else CyberSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFF00B074) else CyberCardBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    selectedAmount = amount
                                    customAmountInput = ""
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = String.format(Locale.US, "₦%,.0f", amount),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CyberGreen else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Instant Transfer Action Button
                val withdrawAmount = if (customAmountInput.isNotBlank()) {
                    customAmountInput.toDoubleOrNull() ?: selectedAmount
                } else {
                    selectedAmount
                }

                val canWithdraw = wallet.balanceNgn >= withdrawAmount && withdrawAmount > 0

                Button(
                    onClick = {
                        val tx = onWithdrawAmount(withdrawAmount)
                        if (tx != null) {
                            latestReceipt = tx
                            payoutToast = "Instant Transfer of ₦${String.format(Locale.US, "%,.2f", withdrawAmount)} sent to Sabiu Abdullahi Muhammad (9169878194)!"
                        } else {
                            payoutToast = "Insufficient balance. Watch more sponsor ads to earn ₦500 each!"
                        }
                    },
                    enabled = canWithdraw,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00B074),
                        contentColor = Color.White,
                        disabledContainerColor = CyberSurfaceVariant,
                        disabledContentColor = TextMutedDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_opay_withdrawal_button")
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WITHDRAW ₦${String.format(Locale.US, "%,.0f", withdrawAmount)} TO OPAY",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }

                if (payoutToast != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = payoutToast!!,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canWithdraw) CyberGreen else CyberRed,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Digital Payment Receipt Card (Generated upon withdrawal)
        AnimatedVisibility(visible = latestReceipt != null) {
            latestReceipt?.let { tx ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("opay_transfer_receipt_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00B074))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = CyberGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TRANSFER SUCCESSFUL",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CyberGreen,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Text(
                                text = tx.formattedDate,
                                fontSize = 10.sp,
                                color = TextSecondaryDark
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = String.format(Locale.US, "₦%,.2f", tx.amountNgn),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(text = "Beneficiary: ${tx.accountName} (${tx.accountNumber})", fontSize = 12.sp, color = TextSecondaryDark)
                        Text(text = "Bank: ${tx.bankName} • Ref: ${tx.reference}", fontSize = 11.sp, color = CyberCyan)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { shareReceipt(tx) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Share Receipt", fontSize = 11.sp, color = Color.White)
                            }

                            Button(
                                onClick = { copyToClipboard(tx.reference, "Transaction Ref") },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B074)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Copy Ref", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Payout History Ledger
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RECENT OPAY WITHDRAWALS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "${wallet.recentTransactions.size} Records",
                        fontSize = 11.sp,
                        color = TextMutedDark
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                wallet.recentTransactions.take(5).forEach { tx ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Direct OPay Payout",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${tx.reference} • ${tx.formattedDate}",
                                fontSize = 10.sp,
                                color = TextMutedDark
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = String.format(Locale.US, "-₦%,.2f", tx.amountNgn),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberGreen
                            )
                            Text(
                                text = tx.status,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberGreen
                            )
                        }
                    }
                }
            }
        }
    }
}
