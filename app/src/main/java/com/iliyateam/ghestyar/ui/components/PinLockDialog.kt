// ═══ ui/components/PinLockDialog.kt ═══
package com.iliyateam.ghestyar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.iliyateam.ghestyar.ui.theme.Coral
import com.iliyateam.ghestyar.ui.theme.Moss
import com.iliyateam.ghestyar.util.faDigits

@Composable
fun PinLockDialog(
    correctPin: String = "",
    onSuccess: () -> Unit = {},
    isSettingMode: Boolean = false,
    onDismiss: () -> Unit = {},
    onPinSet: (String) -> Unit = {}
) {
    var inputPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val haptic = LocalHapticFeedback.current

    fun onNumberClick(num: String) {
        if (inputPin.length < 4) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            val newPin = inputPin + num
            inputPin = newPin
            errorMessage = null

            if (newPin.length == 4) {
                if (isSettingMode) {
                    onPinSet(newPin)
                    onSuccess()
                } else {
                    if (newPin == correctPin) {
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        onSuccess()
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.Reject)
                        errorMessage = "رمز اشتباه است!"
                        inputPin = ""
                    }
                }
            }
        }
    }

    fun onDeleteClick() {
        if (inputPin.isNotEmpty()) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            inputPin = inputPin.dropLast(1)
            errorMessage = null
        }
    }

    Dialog(
        onDismissRequest = { if (isSettingMode) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = isSettingMode,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Moss.copy(alpha = 0.14f),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Lock, null, tint = Moss, modifier = Modifier.size(36.dp))
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (isSettingMode) "تعیین رمز عبور جدید" else "قسط‌یار قفل است",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (isSettingMode) "یک رمز ۴ رقمی دلخواه وارد کنید" else "رمز ۴ رقمی خود را وارد کنید",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // ۴ دایره پین کد
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < inputPin.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) Moss else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }

                errorMessage?.let { msg ->
                    Text(msg, color = Coral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(4.dp))

                // کیپد عددی
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val rows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("", "0", "del")
                    )

                    rows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEach { item ->
                                when (item) {
                                    "" -> Spacer(Modifier.size(68.dp))
                                    "del" -> {
                                        Surface(
                                            onClick = ::onDeleteClick,
                                            shape = CircleShape,
                                            color = Color.Transparent,
                                            modifier = Modifier.size(68.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.AutoMirrored.Rounded.Backspace,
                                                    contentDescription = "حذف",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                    else -> {
                                        Surface(
                                            onClick = { onNumberClick(item) },
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(68.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    item.faDigits(),
                                                    fontSize = 24.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isSettingMode) {
                    TextButton(onClick = onDismiss) {
                        Text("انصراف", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
