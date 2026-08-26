// ═══ ui/screens/AboutScreen.kt ═══
package com.iliyateam.ghestyar.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.iliyateam.ghestyar.ui.components.AppLogo
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iliyateam.ghestyar.R
import com.iliyateam.ghestyar.ui.components.bounceClick
import com.iliyateam.ghestyar.ui.theme.GoldVip
import com.iliyateam.ghestyar.ui.theme.Moss
import com.iliyateam.ghestyar.util.faDigits

@Composable
fun AboutScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    var showRateConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // نوار بالا با statusBarsPadding
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "بازگشت")
            }
            Text("درباره قسط‌یار", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(10.dp))

            // لوگوی بزرگ برنامه
            AppLogo(
                modifier = Modifier
                    .size(92.dp)
                    .bounceClick(minScale = 0.94f)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("قسط‌یار", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("دستیار جامع مدیریت اقساط و آرامش مالی", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Text(
                        "نسخه ۱.۰",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp)
                    )
                }
            }

            // کارت معرفی سازنده
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = GoldVip.copy(alpha = 0.15f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👨‍💻", fontSize = 24.sp)
                        }
                    }

                    Column(Modifier.weight(1f)) {
                        Text("توسعه‌دهنده", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("MightyMahdi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Moss)
                        Text("طراحی شده با عشق برای آرامش مالی شما ❤️", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // کلیدهای عملیات مایکت
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column {
                    AboutLinkRow(
                        title = "بررسی به‌روزرسانی در مایکت 🔄",
                        subtitle = "بررسی و نصب مستقیم نسخه جدید درون برنامه",
                        icon = Icons.Rounded.Update,
                        onClick = { checkMyketUpdate(context) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                    AboutLinkRow(
                        title = "صفحه قسط‌یار در مایکت 📲",
                        subtitle = "مشاهده صفحه رسمی برنامه در استور مایکت",
                        icon = Icons.AutoMirrored.Rounded.OpenInNew,
                        onClick = { openMyketDetails(context) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                    AboutLinkRow(
                        title = "ثبت نظر و ۵ ستاره در مایکت ⭐",
                        subtitle = "حمایت شما به ما انرژی فوق‌العاده‌ای می‌دهد",
                        icon = Icons.Rounded.RateReview,
                        onClick = { showRateConfirmDialog = true }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                    AboutLinkRow(
                        title = "سایر برنامه‌های توسعه‌دهنده در مایکت 🚀",
                        subtitle = "مشاهده سایر برنامه‌ها و بازی‌های منتشر شده در مایکت",
                        icon = Icons.Rounded.Apps,
                        onClick = { openMyketDeveloper(context) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                    AboutLinkRow(
                        title = "معرفی قسط‌یار به دوستان",
                        subtitle = "اشتراک‌گذاری لینک دانلود برنامه با دیگران",
                        icon = Icons.Rounded.Share,
                        onClick = { shareApp(context) }
                    )
                }
            }

            // متن قوانین و حریم خصوصی
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("حفظ حریم خصوصی و امنیت داده‌ها 🛡️", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "تمام اطلاعات مالی، اقساط، چک‌ها و تراکنش‌های شما به صورت ۱۰۰٪ آفلاین و درون گوشی خودتان ذخیره می‌شود و هیچ اطلاعاتی به هیچ سروری ارسال نمی‌گردد.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        // دکمه ارگونومیک پایین صفحه با navigationBarsPadding
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Moss)
                ) {
                    Text("بازگشت به برنامه", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }

    // دیالوگ تایید ثبت نظر در مایکت (طبق توصیه مستندات فنی مایکت)
    if (showRateConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRateConfirmDialog = false },
            icon = {
                Text("⭐⭐⭐⭐⭐", fontSize = 24.sp)
            },
            title = {
                Text("ثبت نظر و امتیاز در مایکت", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Text(
                    "آیا از کار با قسط‌یار رضایت دارید؟\nبا ثبت نظر و امتیاز ۵ ستاره در مایکت، انرژی زیادی به ما برای افزودن امکانات جذاب‌تر می‌دهید. ❤️",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRateConfirmDialog = false
                        openMyketComment(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Moss)
                ) {
                    Text("ثبت نظر در مایکت 🌟", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRateConfirmDialog = false }) {
                    Text("بعداً")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun AboutLinkRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(minScale = 0.98f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Moss, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Rounded.ChevronLeft, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

// ─── توابع رسمی اینتنت‌های مایکت (طبق مستندات فنی رسمی مایکت) ───
/**
 * باز کردن صفحه ثبت نظر در مایکت: myket://comment?id=[package_name]
 */
private fun openMyketComment(context: Context) {
    val packageName = context.packageName
    val url = "myket://comment?id=$packageName"
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://myket.ir/app/$packageName"))
            context.startActivity(webIntent)
        } catch (e2: Exception) {
            Toast.makeText(context, "مایکت یا مرورگر یافت نشد", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * نمایش سایر برنامه‌های توسعه‌دهنده در مایکت: myket://developer/[PACKAGE_NAME]
 */
private fun openMyketDeveloper(context: Context) {
    val packageName = context.packageName
    val url = "myket://developer/$packageName"
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://myket.ir/developer/$packageName"))
            context.startActivity(webIntent)
        } catch (e2: Exception) {
            Toast.makeText(context, "مایکت یا مرورگر یافت نشد", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * بررسی و به‌روزرسانی درون‌برنامه‌ای مایکت: myket://check-update?id=[APP_PACKAGE_NAME]
 */
private fun checkMyketUpdate(context: Context) {
    val packageName = context.packageName
    val url = "myket://check-update?id=$packageName"
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        // در صورت عدم شناسایی مایکت، هدایت به صفحه برنامه
        openMyketDetails(context)
    }
}

/**
 * مشاهده صفحه جزئیات برنامه در مایکت: myket://details?id=[package_name]
 */
private fun openMyketDetails(context: Context) {
    val packageName = context.packageName
    val url = "myket://details?id=$packageName"
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://myket.ir/app/$packageName"))
            context.startActivity(webIntent)
        } catch (e2: Exception) {
            Toast.makeText(context, "مایکت یا مرورگر یافت نشد", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun shareApp(context: Context) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            "قسط‌یار؛ دستیار هوشمند مدیریت اقساط، چک‌ها و دخل‌وخرج.\nهمین حالا از مایکت دانلود کن:\nhttps://myket.ir/app/${context.packageName}"
        )
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "اشتراک‌گذاری قسط‌یار"))
}
