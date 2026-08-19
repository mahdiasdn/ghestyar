// ═══ ui/components/ReceiptCardDialog.kt ═══
package com.iliyateam.ghestyar.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.iliyateam.ghestyar.data.ChequeOrDebt
import com.iliyateam.ghestyar.data.Installment
import com.iliyateam.ghestyar.ui.theme.*
import com.iliyateam.ghestyar.util.faDigits
import com.iliyateam.ghestyar.util.formatJalali
import com.iliyateam.ghestyar.util.money
import java.time.LocalDate

@Composable
fun InstallmentReceiptCardDialog(
    item: Installment,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val todayJalali = LocalDate.now().formatJalali()
    val dueJalali = LocalDate.ofEpochDay(item.dueEpochDay).formatJalali()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // هدر دیالوگ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = CircleShape, color = Moss.copy(alpha = 0.14f), modifier = Modifier.size(36.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.ReceiptLong, null, tint = Moss, modifier = Modifier.size(20.dp))
                        }
                    }
                    Text("کارت رسید پرداخت", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.Close, "بستن", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // 🎨 کارت گرافیکی رسید پرداخت (طرح بانکی و مدرن)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF064E3B),
                                    Color(0xFF047857),
                                    Color(0xFF0F766E)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // بالای کارت: برند و نشان تایید
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("🌱", fontSize = 16.sp)
                                Text("قسط‌یار • رسید الکترونیک", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA7F3D0))
                            }
                            Surface(shape = RoundedCornerShape(50), color = Color(0x3310B981)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF6EE7B7), modifier = Modifier.size(12.dp))
                                    Text("تسویه شد", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6EE7B7))
                                }
                            }
                        }

                        // مبلغ بزرگ وسط کارت
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Text("مبلغ پرداخت شده", fontSize = 10.sp, color = Color(0xCCE6F7F2))
                            Text(
                                "${item.amount.money()} تومان",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        // جداکننده خط‌چین
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0x33FFFFFF))
                        )

                        // ردیف‌های اطلاعات رسید
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ReceiptRow(label = "عنوان قسط:", value = item.title)
                            ReceiptRow(label = "نوبت قسط:", value = "قسط ${item.paidSessions.faDigits()} از ${item.totalSessions.faDigits()}")
                            ReceiptRow(label = "تاریخ ثبت:", value = todayJalali)
                            ReceiptRow(label = "سررسید بعدی:", value = dueJalali)
                        }

                        // بارکد گرافیکی شکیل انتهای کارت
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("GH-PAY-${item.id.faDigits()}-${item.paidSessions.faDigits()}", fontSize = 9.sp, color = Color(0x88FFFFFF))
                            Text("تایید شده در سامانه هوشمند قسط‌یار", fontSize = 8.sp, color = Color(0xAAFFFFFF))
                        }
                    }
                }
            }

            // دکمه‌های عملیات
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        ReceiptShareHelper.shareInstallmentReceipt(context, item)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Moss),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .bounceClick(minScale = 0.94f)
                ) {
                    Icon(Icons.Rounded.Share, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("اشتراک‌گذاری کارت رسید", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("بستن", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ChequeReceiptCardDialog(
    item: ChequeOrDebt,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dueJalali = LocalDate.ofEpochDay(item.dueEpochDay).formatJalali()
    val isCheque = item.isCheque

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // هدر دیالوگ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = CircleShape, color = ChequeBlue.copy(alpha = 0.14f), modifier = Modifier.size(36.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(if (isCheque) "✍️" else "🤝", fontSize = 16.sp)
                        }
                    }
                    Text(if (isCheque) "کارت یادآوری چک صیادی" else "کارت یادآوری قرض مالی", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.Close, "بستن", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // 🎨 کارت گرافیکی صیادی (طرح لاجوردی سلطنتی)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF1E3A8A),
                                    Color(0xFF2563EB),
                                    Color(0xFF3B82F6)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // بالای کارت
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(if (isCheque) "🏛️" else "💼", fontSize = 16.sp)
                                Text(if (isCheque) "یادآوری چک صیادی" else "یادآوری تعهد مالی", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBFDBFE))
                            }
                            Surface(shape = RoundedCornerShape(50), color = Color(0x33FFFFFF)) {
                                Text(
                                    if (item.isReceivable) "طلب (دریافتی)" else "بدهی (پرداختی)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // مبلغ
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Text("مبلغ سند مالی", fontSize = 10.sp, color = Color(0xCCDBEAFE))
                            Text(
                                "${item.amount.money()} تومان",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        // جداکننده
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x33FFFFFF)))

                        // اطلاعات
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ReceiptRow(label = "طرف حساب:", value = item.personName)
                            ReceiptRow(label = "بابت:", value = item.title)
                            ReceiptRow(label = "موعد سررسید:", value = dueJalali)
                            if (item.chequeNumber.isNotBlank()) {
                                ReceiptRow(label = "شماره صیادی/چک:", value = item.chequeNumber)
                            }
                            if (item.bankName.isNotBlank()) {
                                ReceiptRow(label = "بانک صادرکننده:", value = item.bankName)
                            }
                        }

                        // بارکد انتهای کارت
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("SAYAD-${item.id.faDigits()}", fontSize = 9.sp, color = Color(0x88FFFFFF))
                            Text("ثبت و یادآوری توسط قسط‌یار", fontSize = 8.sp, color = Color(0xAAFFFFFF))
                        }
                    }
                }
            }

            // دکمه‌های عملیات
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        ReceiptShareHelper.shareChequeReminder(context, item)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ChequeBlue),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .bounceClick(minScale = 0.94f)
                ) {
                    Icon(Icons.Rounded.Share, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("ارسال و اشتراک‌گذاری", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("بستن", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 11.sp, color = Color(0xCCFFFFFF))
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
