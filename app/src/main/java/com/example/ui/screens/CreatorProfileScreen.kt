package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CreatorManager
import com.example.data.CreatorStats
import com.example.data.CreatorWallet
import com.example.data.DataSaverMetrics
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun CreatorProfileScreen(
    stats: CreatorStats,
    wallet: CreatorWallet,
    dataSaver: DataSaverMetrics,
    creatorManager: CreatorManager,
    onToggleUltraSaver: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // State for viewing & editing OPay Account Details
    var isEditingOpay by remember { mutableStateOf(false) }
    var accountNameInput by remember(wallet.opayAccountName) { mutableStateOf(wallet.opayAccountName) }
    var accountNumberInput by remember(wallet.opayAccountNumber) { mutableStateOf(wallet.opayAccountNumber) }
    var bankNameInput by remember(wallet.opayBankName) { mutableStateOf(wallet.opayBankName) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied $label: $text", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CREATOR SETTINGS & PROFILE",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 1.sp
        )
        Text(
            text = "OPay Account Details, Verified Identity & Payout Settings",
            fontSize = 12.sp,
            color = TextSecondaryDark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. OPay Account Details Card (View & Edit)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("opay_account_details_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00B074))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
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
                            Text(
                                text = "OPAY ACCOUNT DETAILS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00B074),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Direct Payout Destination",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            if (isEditingOpay) {
                                // Save
                                creatorManager.updateOpayAccountDetails(
                                    accountName = accountNameInput.trim().ifEmpty { "Sabiu Abdullahi Muhammad" },
                                    accountNumber = accountNumberInput.trim().ifEmpty { "9169878194" },
                                    bankName = bankNameInput.trim().ifEmpty { "OPay" }
                                )
                                isEditingOpay = false
                                saveSuccessMessage = "Saved OPay details successfully!"
                            } else {
                                isEditingOpay = true
                                saveSuccessMessage = null
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isEditingOpay) CyberGreen else CyberCyan
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isEditingOpay) CyberGreen else CyberCyan
                        ),
                        modifier = Modifier.testTag("edit_save_opay_button")
                    ) {
                        Icon(
                            imageVector = if (isEditingOpay) Icons.Default.Save else Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isEditingOpay) "Save" else "Edit",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isEditingOpay) {
                    // Edit Mode Form
                    Column {
                        // Account Name
                        Text(
                            text = "Account Holder Full Name",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondaryDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = accountNameInput,
                            onValueChange = { accountNameInput = it },
                            placeholder = { Text("Sabiu Abdullahi Muhammad", color = TextMutedDark, fontSize = 13.sp) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberGreen,
                                unfocusedBorderColor = CyberCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = CyberSurfaceVariant,
                                unfocusedContainerColor = CyberSurfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("opay_account_name_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Account Number
                        Text(
                            text = "OPay Account / Phone Number",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondaryDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = accountNumberInput,
                            onValueChange = { accountNumberInput = it },
                            placeholder = { Text("9169878194", color = TextMutedDark, fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(18.dp))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberGreen,
                                unfocusedBorderColor = CyberCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = CyberSurfaceVariant,
                                unfocusedContainerColor = CyberSurfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("opay_account_number_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Bank Name
                        Text(
                            text = "Bank / FinTech Provider",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondaryDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = bankNameInput,
                            onValueChange = { bankNameInput = it },
                            placeholder = { Text("OPay", color = TextMutedDark, fontSize = 13.sp) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color(0xFF00B074), modifier = Modifier.size(18.dp))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberGreen,
                                unfocusedBorderColor = CyberCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = CyberSurfaceVariant,
                                unfocusedContainerColor = CyberSurfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("opay_bank_name_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                creatorManager.updateOpayAccountDetails(
                                    accountName = accountNameInput.trim().ifEmpty { "Sabiu Abdullahi Muhammad" },
                                    accountNumber = accountNumberInput.trim().ifEmpty { "9169878194" },
                                    bankName = bankNameInput.trim().ifEmpty { "OPay" }
                                )
                                isEditingOpay = false
                                saveSuccessMessage = "Saved OPay details successfully!"
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B074), contentColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("save_opay_details_btn")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SAVE & UPDATE OPAY DETAILS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // View Mode Display
                    Column {
                        // Account Name Row
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyberSurfaceVariant.copy(alpha = 0.6f))
                                .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "BENEFICIARY ACCOUNT NAME", fontSize = 10.sp, color = TextMutedDark, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = wallet.opayAccountName,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Verified",
                                            tint = CyberCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Name",
                                    tint = CyberCyan,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { copyToClipboard(wallet.opayAccountName, "Account Name") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Account Number Row
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyberSurfaceVariant.copy(alpha = 0.6f))
                                .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "OPAY ACCOUNT / PHONE NUMBER", fontSize = 10.sp, color = TextMutedDark, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = wallet.opayAccountNumber,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CyberGreen,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Number",
                                    tint = CyberGreen,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { copyToClipboard(wallet.opayAccountNumber, "Account Number") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Bank Provider Row
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyberSurfaceVariant.copy(alpha = 0.6f))
                                .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "BANK / SETTLEMENT INSTITUTION", fontSize = 10.sp, color = TextMutedDark, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${wallet.opayBankName} (Instant Nigerian Payout Gateway)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00B074)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF00B074).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "INSTANT NGN",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF00B074)
                                    )
                                }
                            }
                        }
                    }
                }

                if (saveSuccessMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = saveSuccessMessage!!,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberGreen,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Creator Identity & Level Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(CyberCyan.copy(alpha = 0.2f))
                            .border(1.5.dp, CyberCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = wallet.opayAccountName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = CyberCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "OPay Account: ${wallet.opayAccountNumber}",
                            fontSize = 12.sp,
                            color = CyberGreen
                        )
                        Text(
                            text = "Level: ${stats.creatorLevel}",
                            fontSize = 11.sp,
                            color = CyberAmber
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Multi-Platform Sync Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CONNECTED REVENUE PLATFORMS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                PlatformSyncRow(name = "Google Creator / AdSense Hub", status = "SYNCED & ACTIVE", color = Color(0xFFEA4335))
                Spacer(modifier = Modifier.height(8.dp))
                PlatformSyncRow(name = "Telegram Stars Channel", status = "SYNCED & ACTIVE", color = Color(0xFF2AABEE))
                Spacer(modifier = Modifier.height(8.dp))
                PlatformSyncRow(name = "Facebook Stars & Reels", status = "SYNCED & ACTIVE", color = Color(0xFF1877F2))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Data Saver Engine Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DATA SAVER & WEB ENGINE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberGreen,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Ultra Data-Saving Engine", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text(text = "Compress web search assets up to 82%", fontSize = 11.sp, color = TextMutedDark)
                    }
                    Switch(
                        checked = dataSaver.isUltraDataSaverActive,
                        onCheckedChange = onToggleUltraSaver,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberGreen,
                            checkedTrackColor = CyberGreen.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextMutedDark,
                            uncheckedTrackColor = CyberSurfaceVariant
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Information Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Digital Creator's Rewarding App • Registered to Sabiu Abdullahi Muhammad",
                fontSize = 11.sp,
                color = TextMutedDark
            )
        }
    }
}

@Composable
fun PlatformSyncRow(name: String, status: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = name, fontSize = 12.sp, color = Color.White)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberGreen)
        }
    }
}
