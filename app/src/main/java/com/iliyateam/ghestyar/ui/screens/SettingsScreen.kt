// ═══ ui/screens/SettingsScreen.kt ═══
package com.iliyateam.ghestyar.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iliyateam.ghestyar.AppFontScale
import com.iliyateam.ghestyar.AppThemeMode
import com.iliyateam.ghestyar.MainViewModel
import com.iliyateam.ghestyar.VipColorTheme
import com.iliyateam.ghestyar.data.UserProfile
import com.iliyateam.ghestyar.ui.components.PinLockDialog
import com.iliyateam.ghestyar.ui.components.bounceClick
import com.iliyateam.ghestyar.ui.theme.*
import com.iliyateam.ghestyar.util.faDigits
import com.iliyateam.ghestyar.widget.GhestYarWidgetProvider

@Composable
fun SettingsScreen(
    vm: MainViewModel,
    isPremium: Boolean,
    onOpenCalculator: () -> Unit,
    onOpenPremium: () -> Unit,
    onOpenAbout: () -> Unit,
    onExportExcel: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {
    val themeMode by vm.themeMode.collectAsStateWithLifecycle()
    val vipColorTheme by vm.vipColorTheme.collectAsStateWithLifecycle()
    val fontScale by vm.fontScale.collectAsStateWithLifecycle()
    val isPrivacyMode by vm.isPrivacyMode.collectAsStateWithLifecycle()
    val isPinLockEnabled by vm.isPinLockEnabled.collectAsStateWithLifecycle()
    val notifHour by vm.notificationHour.collectAsStateWithLifecycle()

    val allProfiles by vm.allProfiles.collectAsStateWithLifecycle()
    val activeProfile by vm.activeProfile.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var showSetPinDialog by remember { mutableStateOf(false) }
    var showAddProfileDialog by remember { mutableStateOf(false) }
    var showWidgetGuideDialog by remember { mutableStateOf(false) }
    var profileToDelete by remember { mutableStateOf<UserProfile?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .padding(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // هدر تنظیمات با statusBarsPadding
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Moss,
                modifier = Modifier.size(44.dp),
                shadowElevation = 3.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Settings, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("تنظیمات و شخصی‌سازی", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("مدیریت پروفایل‌ها، تم، امنیت و فایل‌ها", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // 👥 بخش ۱: مدیریت حساب‌ها و پروفایل‌های چندگانه و کاملاً ایزوله
        SettingsSection(title = "مدیریت حساب‌ها و فضاهای مالی چندگانه") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "حساب‌های تعریف‌شده (${allProfiles.size.faDigits()} حساب)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            if (!isPremium && allProfiles.size >= 1) {
                                onOpenPremium()
                            } else {
                                showAddProfileDialog = true
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isPremium && allProfiles.size >= 1) GoldVip.copy(alpha = 0.18f) else Moss.copy(alpha = 0.14f)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.bounceClick(minScale = 0.94f)
                    ) {
                        if (!isPremium && allProfiles.size >= 1) {
                            Icon(Icons.Rounded.Star, null, tint = GoldVip, modifier = Modifier.size(16.dp))
                        } else {
                            Icon(Icons.Rounded.Add, null, tint = Moss, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (!isPremium && allProfiles.size >= 1) "حساب جدید (⭐ VIP)" else "افزودن حساب جدید",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isPremium && allProfiles.size >= 1) GoldVip else Moss
                        )
                    }
                }

                // لیست پروفایل‌ها
                allProfiles.forEach { profile ->
                    val isSelected = activeProfile.id == profile.id
                    Card(
                        onClick = { vm.selectProfile(profile.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Moss.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        border = if (isSelected) BorderStroke(1.5.dp, Moss) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick(minScale = 0.98f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) Moss else MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(profile.emoji, fontSize = 18.sp)
                                }
                            }

                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        profile.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (profile.isDefault) {
                                        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
                                            Text("پیش‌فرض", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                                Text(
                                    if (isSelected) "حساب فعال (تمام اطلاعات تفکیک‌شده است) ✅" else "لمس کنید برای سوییچ به این حساب",
                                    fontSize = 10.sp,
                                    color = if (isSelected) Moss else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (!profile.isDefault && profile.id != 1L) {
                                IconButton(
                                    onClick = { profileToDelete = profile },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Outlined.Delete, "حذف حساب", tint = Coral, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // بخش ۲: ظاهر و تم
        SettingsSection(title = "ظاهر و تم برنامه") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOptionChip(
                    title = "روشن",
                    icon = Icons.Rounded.LightMode,
                    isSelected = themeMode == AppThemeMode.LIGHT,
                    onClick = { vm.setTheme(AppThemeMode.LIGHT) },
                    modifier = Modifier.weight(1f)
                )
                ThemeOptionChip(
                    title = "تاریک",
                    icon = Icons.Rounded.DarkMode,
                    isSelected = themeMode == AppThemeMode.DARK,
                    onClick = { vm.setTheme(AppThemeMode.DARK) },
                    modifier = Modifier.weight(1f)
                )
                ThemeOptionChip(
                    title = "سیستم",
                    icon = Icons.Rounded.BrightnessAuto,
                    isSelected = themeMode == AppThemeMode.SYSTEM,
                    onClick = { vm.setTheme(AppThemeMode.SYSTEM) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // بخش ۳: تم‌های اشرافی لوکس VIP
        SettingsSection(title = "تم‌های رنگی اشرافی (⭐ VIP)") {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "پالت رنگی و استایل اشرافی اپلیکیشن را بر اساس سلیقه خود تغییر دهید:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(VipColorTheme.entries) { themeOption ->
                        val isSelected = vipColorTheme == themeOption
                        Surface(
                            onClick = {
                                if (isPremium || themeOption == VipColorTheme.TEAL_MOSS) {
                                    vm.setVipTheme(themeOption)
                                } else {
                                    onOpenPremium()
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) GoldVip.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) GoldVip else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.bounceClick(minScale = 0.94f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(android.graphics.Color.parseColor(themeOption.hexColor)),
                                    modifier = Modifier.size(22.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                                ) {}
                                Text(
                                    themeOption.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) GoldVip else MaterialTheme.colorScheme.onSurface
                                )
                                if (!isPremium && themeOption != VipColorTheme.TEAL_MOSS) {
                                    Icon(Icons.Rounded.Lock, null, tint = GoldVip, modifier = Modifier.size(13.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // بخش ۴: اندازه فونت و متون
        SettingsSection(title = "اندازه قلم و متون") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AppFontScale.entries.forEach { scaleOption ->
                    val isSelected = fontScale == scaleOption
                    Surface(
                        onClick = { vm.setFontScale(scaleOption) },
                        modifier = Modifier
                            .weight(1f)
                            .bounceClick(minScale = 0.94f),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Moss else MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                scaleOption.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // بخش ۴: امنیت، حریم خصوصی و اعلان‌ها
        SettingsSection(title = "امنیت و یادآوری‌ها") {
            SettingsToggleRow(
                title = "قفل امنیتی با رمز عبور (PIN)",
                subtitle = if (isPinLockEnabled) "رمز عبور ۴ رقمی فعال است 🔒" else "قفل برنامه هنگام ورود غیرفعال است",
                icon = Icons.Rounded.Lock,
                checked = isPinLockEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        showSetPinDialog = true
                    } else {
                        vm.setPinLock(false, "")
                    }
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

            SettingsToggleRow(
                title = "حالت حریم خصوصی (مخفی‌سازی مبالغ)",
                subtitle = "پوشاندن مبالغ با •••••• برای حفظ محرمانگی",
                icon = Icons.Rounded.VisibilityOff,
                checked = isPrivacyMode,
                onCheckedChange = { vm.setPrivacyMode(it) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Notifications, null, tint = Moss, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("ساعت ارسال اعلان‌های یادآوری", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                        Text("اعلان‌ها با نام حساب مربوطه سر موعد ارسال می‌شوند", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf(8, 9, 10, 12, 18, 20)) { hour ->
                        val isSelected = notifHour == hour
                        FilterChip(
                            selected = isSelected,
                            onClick = { vm.setNotificationHour(hour) },
                            label = { Text("ساعت ${hour.faDigits()}:۰۰", fontSize = 11.sp) },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Moss,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // بخش ۵: ابزارهای مالی و محاسباتی
        SettingsSection(title = "ابزارهای هوشمند مالی و ویجت") {
            SettingsActionRow(
                title = "ویجت هوشمند صفحه اصلی گوشی 📱",
                subtitle = "مشاهده زنده سررسید اقساط و روزشمار روی صفحه اصلی",
                icon = Icons.Rounded.Widgets,
                locked = false,
                onClick = { showWidgetGuideDialog = true }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

            SettingsActionRow(
                title = "ماشین‌حساب جامع اقساط و سود وام 🧮",
                subtitle = "محاسبه وام‌های ۴٪، ۱۸٪، ۲۳٪ و نرخ‌های مصوب بانکی",
                icon = Icons.Rounded.Calculate,
                locked = false,
                onClick = onOpenCalculator
            )
        }

        // بخش ۶: مدیریت داده‌ها و فایل‌ها
        SettingsSection(title = "مدیریت داده‌ها و پشتیبان") {
            SettingsActionRow(
                title = "خروجی و گزارش‌گیری رسمی (PDF و اکسل)",
                subtitle = "تهیه گزارش تفکیک‌شده PDF مصور و اکسل با فیلتر بازه زمانی",
                icon = Icons.Rounded.PictureAsPdf,
                locked = !isPremium,
                onClick = { if (isPremium) onExportExcel() else onOpenPremium() }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

            SettingsActionRow(
                title = "تهیه فایل پشتیبان JSON",
                subtitle = "ذخیره فایل پشتیبان از تمام اقساط و دخل‌وخرج",
                icon = Icons.Rounded.CloudUpload,
                locked = !isPremium,
                onClick = { if (isPremium) onBackup() else onOpenPremium() }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

            SettingsActionRow(
                title = "بازیابی اطلاعات از فایل پشتیبان",
                subtitle = "بازگردانی فایل بک‌آپ گرفته شده قبلی",
                icon = Icons.Rounded.Restore,
                locked = !isPremium,
                onClick = { if (isPremium) onRestore() else onOpenPremium() }
            )
        }

        // بخش ۷: اشتراک طلایی VIP
        Card(
            onClick = onOpenPremium,
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = GoldVip.copy(alpha = 0.14f)),
            border = BorderStroke(1.dp, GoldVip.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .bounceClick(minScale = 0.98f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = GoldVip,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Star, null, tint = Color.Black, modifier = Modifier.size(24.dp))
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text("ارتقا به نسخه طلایی (⭐ VIP)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GoldVip)
                    Text("گزارش‌گیری PDF و اکسل، هوش مالی و امکانات نامحدود", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Rounded.ChevronLeft, null, tint = GoldVip)
            }
        }

        // بخش ۸: درباره برنامه و به‌روزرسانی
        SettingsSection(title = "درباره و به‌روزرسانی") {
            SettingsActionRow(
                title = "بررسی به‌روزرسانی در مایکت 🔄",
                subtitle = "بررسی و دریافت آخرین نسخه قسط‌یار",
                icon = Icons.Rounded.Update,
                locked = false,
                onClick = {
                    val packageName = context.packageName
                    val url = "myket://check-update?id=$packageName"
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        try {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://myket.ir/app/$packageName"))
                            context.startActivity(webIntent)
                        } catch (_: Exception) {}
                    }
                }
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(horizontal = 14.dp)
            )

            SettingsActionRow(
                title = "درباره قسط‌یار",
                subtitle = "نسخه ۱.۰",
                icon = Icons.Rounded.Info,
                locked = false,
                onClick = onOpenAbout
            )
        }
    }

    // دیالوگ تنظیم پین کد
    if (showSetPinDialog) {
        PinLockDialog(
            isSettingMode = true,
            onPinSet = { newPin ->
                vm.setPinLock(true, newPin)
                showSetPinDialog = false
            },
            onDismiss = {
                showSetPinDialog = false
            }
        )
    }

    // دیالوگ راهنما و افزودن ویجت صفحه اصلی
    if (showWidgetGuideDialog) {
        WidgetGuideDialog(
            onDismiss = { showWidgetGuideDialog = false }
        )
    }

    // دیالوگ افزودن پروفایل جدید
    if (showAddProfileDialog) {
        AddProfileDialog(
            onDismiss = { showAddProfileDialog = false },
            onSave = { name, emoji, colorIndex ->
                vm.addProfile(name, emoji, colorIndex)
                showAddProfileDialog = false
            }
        )
    }

    // دیالوگ تایید حذف پروفایل
    profileToDelete?.let { profile ->
        com.iliyateam.ghestyar.ui.components.ConfirmDeleteDialog(
            title = "حذف حساب کاربری «${profile.name}»",
            message = "آیا مطمئنید؟ با حذف این حساب، کلیه اقساط، چک‌ها، قلک‌ها و دخل‌وخرج ثبت‌شده در این حساب حذف خواهند شد.",
            onConfirm = {
                vm.deleteProfile(profile)
                profileToDelete = null
            },
            onDismiss = { profileToDelete = null }
        )
    }
}

@Composable
fun AddProfileDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, emoji: String, colorIndex: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("💼") }
    var selectedColorIndex by remember { mutableIntStateOf(0) }

    val emojis = listOf("💼", "👨‍👩‍👧", "👤", "🏢", "🛍️", "💳", "🚗", "💎")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("➕", fontSize = 18.sp)
                Text("افزودن حساب و فضای مالی جدید", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("برای این حساب یک نام و آیکون مشخص کنید تا محیطی کاملاً مستقل داشته باشید:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام حساب (مثلاً شرکت کارآفرین)") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("انتخاب آیکون حساب:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(emojis) { emoji ->
                            val isSelected = selectedEmoji == emoji
                            Surface(
                                onClick = { selectedEmoji = emoji },
                                shape = CircleShape,
                                color = if (isSelected) Moss else MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(emoji, fontSize = 20.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), selectedEmoji, selectedColorIndex)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Moss)
            ) {
                Text("ایجاد و فعال‌سازی حساب", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(6.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun ThemeOptionChip(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.bounceClick(minScale = 0.94f),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Moss else MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Moss, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = Moss)
        )
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    locked: Boolean,
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
            Icon(icon, null, tint = Moss, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    if (locked) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = GoldVip.copy(alpha = 0.16f),
                            border = BorderStroke(0.7.dp, GoldVip.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(Icons.Rounded.Star, null, tint = GoldVip, modifier = Modifier.size(11.dp))
                                Text("VIP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GoldVip)
                            }
                        }
                    }
                }
                Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Rounded.ChevronLeft, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

/**
 * دیالوگ پیش‌نمایش زنده و راهنمای افزودن ویجت به صفحه اصلی
 */
@Composable
private fun WidgetGuideDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("📱", fontSize = 20.sp)
                Text("ویجت هوشمند صفحه اصلی", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "با قرار دادن ویجت قسط‌یار روی صفحه اصلی گوشی، همیشه از نزدیک‌ترین سررسید اقساط و تعهدات ماهانه باخبر باشید.",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // شبیه‌سازی زنده ویجت
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0F2628),
                    border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("قسط‌یار • سررسید بعدی", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA7F3D0))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0x33F59E0B)
                            ) {
                                Text("۳ روز مانده", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Text("وام خرید مسکن", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("۳٬۵۰۰٬۰۰۰ تومان", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))

                        HorizontalDivider(color = Color(0x22FFFFFF))

                        Text("تعهد این ماه: ۷٬۲۰۰٬۰۰۰ تومان (۲ قسط فعال)", fontSize = 10.sp, color = Color(0xFF9CA3AF))
                    }
                }

                // راهنمای گام به گام
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("نحوه فعال‌سازی روی گوشی:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("۱. انگشت خود را روی فضای خالی صفحه اصلی گوشی نگه دارید.", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("۲. گزینه «ویجت‌ها / Widgets» را انتخاب کنید.", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("۳. «قسط‌یار» را پیدا کرده و به صفحه بکشید.", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
                        val myProvider = android.content.ComponentName(context, GhestYarWidgetProvider::class.java)
                        if (appWidgetManager.isRequestPinAppWidgetSupported) {
                            appWidgetManager.requestPinAppWidget(myProvider, null, null)
                        } else {
                            android.widget.Toast.makeText(context, "لطفاً از منوی ویجت‌های لانچر گوشی اقدام کنید", android.widget.Toast.LENGTH_LONG).show()
                        }
                    } else {
                        android.widget.Toast.makeText(context, "لطفاً از منوی ویجت‌های لانچر گوشی اقدام کنید", android.widget.Toast.LENGTH_LONG).show()
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Moss)
            ) {
                Text("افزودن خودکار به صفحه اصلی 📱", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("متوجه شدم") }
        }
    )
}
