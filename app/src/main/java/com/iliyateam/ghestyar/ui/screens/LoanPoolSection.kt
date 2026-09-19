// ═══ ui/screens/LoanPoolSection.kt ═══
package com.iliyateam.ghestyar.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.iliyateam.ghestyar.MainViewModel
import com.iliyateam.ghestyar.data.LoanPool
import com.iliyateam.ghestyar.data.LoanPoolMember
import com.iliyateam.ghestyar.data.LoanPoolWithMembers
import com.iliyateam.ghestyar.ui.components.ConfettiBurst
import com.iliyateam.ghestyar.ui.components.VipGateContainer
import com.iliyateam.ghestyar.ui.components.bounceClick
import com.iliyateam.ghestyar.ui.theme.*
import com.iliyateam.ghestyar.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun LoanPoolSection(
    poolsWithMembers: List<LoanPoolWithMembers>,
    isPremium: Boolean,
    onOpenPremium: () -> Unit,
    vm: MainViewModel
) {
    val context = LocalContext.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingPool by remember { mutableStateOf<LoanPool?>(null) }
    var activeLotteryPool by remember { mutableStateOf<LoanPoolWithMembers?>(null) }
    var poolForAdvanceRound by remember { mutableStateOf<LoanPoolWithMembers?>(null) }

    VipGateContainer(
        isPremium = isPremium,
        featureTitle = "صندوق‌های وام و قرعه‌کشی خانوادگی",
        featureDescription = "مدیریت جامع اعضا، گردونه متحرک قرعه‌کشی ماهانه، ره‌گیری واریزی‌ها و صدور گزارش برای پیام‌رسان‌ها",
        onOpenPremium = onOpenPremium
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // سربرگ دکمه ایجاد صندوق
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("صندوق‌های وام خانوادگی و همکاران", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${poolsWithMembers.size.faDigits()} صندوق فعال ثبت شده", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Button(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldVip),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.bounceClick(minScale = 0.94f)
                ) {
                    Icon(Icons.Rounded.Add, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("صندوق جدید", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.Black)
                }
            }

            if (poolsWithMembers.isEmpty()) {
                EmptyPoolsView(onCreatePool = { showCreateDialog = true })
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(poolsWithMembers, key = { it.pool.id }) { item ->
                        RichPoolCardItem(
                            poolWithMembers = item,
                            onSpinLottery = { activeLotteryPool = item },
                            onEdit = { editingPool = item.pool },
                            onShareReport = { sharePoolReport(context, item) },
                            onToggleMember = { m -> vm.toggleMemberPaidThisMonth(m) },
                            onAdvanceRound = { poolForAdvanceRound = item },
                            onDelete = { vm.deleteLoanPool(item.pool) }
                        )
                    }
                }
            }
        }
    }

    // دیالوگ ایجاد صندوق جدید
    if (showCreateDialog) {
        AddLoanPoolDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { title, monthlyAmt, members, startDay, note ->
                vm.createLoanPool(title, monthlyAmt, members, startDay, note)
                showCreateDialog = false
            }
        )
    }

    // دیالوگ ویرایش صندوق
    editingPool?.let { poolToEdit ->
        EditLoanPoolDialog(
            pool = poolToEdit,
            onDismiss = { editingPool = null },
            onSave = { title, monthlyAmt, note ->
                vm.updateLoanPool(poolToEdit, title, monthlyAmt, note)
                editingPool = null
            }
        )
    }

    // دیالوگ گردونه قرعه‌کشی زنده
    activeLotteryPool?.let { poolData ->
        LotteryWheelDialog(
            poolData = poolData,
            onDismiss = { activeLotteryPool = null },
            onWinnerSelected = { winner ->
                vm.markPoolWinner(poolData.pool, winner, poolData.pool.currentRound)
                activeLotteryPool = null
            }
        )
    }

    // دیالوگ تایید ورود به دور بعد و تسویه ماه
    poolForAdvanceRound?.let { poolData ->
        AlertDialog(
            onDismissRequest = { poolForAdvanceRound = null },
            title = { Text("ورود به دور بعد (${(poolData.pool.currentRound + 1).faDigits()})") },
            text = {
                Text(
                    "آیا مطمئن هستید؟ با تایید این مرحله، دور ${poolData.pool.currentRound.faDigits()} پایان یافته و وضعیت واریزی تمام اعضا برای ماه جدید بازنشانی می‌شود.",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.advancePoolRoundAndResetPayments(poolData.pool, poolData.members)
                        poolForAdvanceRound = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Moss)
                ) {
                    Text("تایید و ورود به ماه بعد ✅", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { poolForAdvanceRound = null }) { Text("انصراف") }
            }
        )
    }
}

/**
 * کارت جامع و پیشرفته صندوق وام
 */
@Composable
private fun RichPoolCardItem(
    poolWithMembers: LoanPoolWithMembers,
    onSpinLottery: () -> Unit,
    onEdit: () -> Unit,
    onShareReport: () -> Unit,
    onToggleMember: (LoanPoolMember) -> Unit,
    onAdvanceRound: () -> Unit,
    onDelete: () -> Unit
) {
    val pool = poolWithMembers.pool
    val members = poolWithMembers.members
    val paidMembers = members.filter { it.paidThisMonth }
    val unpaidMembers = members.filter { !it.paidThisMonth }
    val wonMembers = members.filter { it.hasWon }
    val remainingEligible = members.filter { !it.hasWon }
    val isAllPaid = unpaidMembers.isEmpty() && members.isNotEmpty()
    var expandedMembers by remember { mutableStateOf(true) }
    var showConfirmDelete by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val totalCollected = paidMembers.size * pool.monthlyAmount

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(minScale = 0.99f),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.2.dp, GoldVip.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ۱. سربرگ کارت با عنوان، دور فعلی و دکمه‌های ویرایش و حذف
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = GoldVip.copy(alpha = 0.16f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👥", fontSize = 22.sp)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            pool.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "سهم ماهانه: ${pool.monthlyAmount.money()} تومان",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GoldVip.copy(alpha = 0.18f),
                        border = BorderStroke(0.8.dp, GoldVip.copy(alpha = 0.4f))
                    ) {
                        Text(
                            "ماه ${pool.currentRound.faDigits()} از ${pool.totalMembers.faDigits()}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = GoldVip,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Outlined.Edit, "ویرایش صندوق", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }

                    IconButton(onClick = { showConfirmDelete = true }, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Outlined.Delete, "حذف صندوق", tint = Coral.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            // ۲. جعبه شاخص‌های مالی کلیدی صندوق
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("مبلغ وام برنده این دور:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${pool.winnerPayout.money()} تومان", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = GoldVip)
                    }

                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(MaterialTheme.colorScheme.outlineVariant))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("جمع‌آوری شده این ماه:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${totalCollected.money()} تومان", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = if (isAllPaid) Color(0xFF10B981) else Moss)
                    }

                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(MaterialTheme.colorScheme.outlineVariant))

                    Column(horizontalAlignment = Alignment.End) {
                        Text("وضعیت پرداخت:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${paidMembers.size.faDigits()} از ${members.size.faDigits()} نفر", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = if (isAllPaid) Color(0xFF10B981) else Coral)
                    }
                }
            }

            // ۳. دکمه‌های اقدام: گردونه قرعه کشی، ارسال گزارش، و پایان ماه
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSpinLottery,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(46.dp)
                        .bounceClick(minScale = 0.95f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldVip)
                ) {
                    Text("🎰 گردونه قرعه‌کشی", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color.Black)
                }

                FilledTonalButton(
                    onClick = onShareReport,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .bounceClick(minScale = 0.95f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Share, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("گزارش ماه", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onAdvanceRound,
                    modifier = Modifier
                        .height(46.dp)
                        .bounceClick(minScale = 0.95f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("پایان ماه ⏭️", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }

            // ۴. لیست برندگان ادوار گذشته (در صورت وجود)
            if (wonMembers.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("🏆 برندگان دوره‌های قبل:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = GoldVip)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(wonMembers.sortedBy { it.wonMonth }) { won: LoanPoolMember ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GoldVip.copy(alpha = 0.12f),
                                border = BorderStroke(0.7.dp, GoldVip.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    "دور ${won.wonMonth.faDigits()}: ${won.name}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = GoldVip,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ۵. لیست اعضا با امکان تغییر وضعیت پرداخت و یادآوری
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedMembers = !expandedMembers },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("لیست اعضا و وضعیت واریز ماه جاری:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Icon(if (expandedMembers) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                AnimatedVisibility(visible = expandedMembers) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        members.forEachIndexed { index, member ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (member.paidThisMonth) Color(0xFF10B981).copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = if (member.paidThisMonth) BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (member.paidThisMonth) Color(0xFF10B981).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    if (member.hasWon) "🏆" else "${(index + 1).faDigits()}",
                                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                                )
                                            }
                                        }

                                        Column {
                                            Text(member.name, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                                            if (member.hasWon) {
                                                Text("برنده دور ${member.wonMonth.faDigits()}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = GoldVip)
                                            } else if (member.phone.isNotBlank()) {
                                                Text(member.phone, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (!member.paidThisMonth && member.phone.isNotBlank()) {
                                            IconButton(
                                                onClick = {
                                                    val sms = Intent(Intent.ACTION_VIEW).apply {
                                                        data = Uri.parse("sms:${member.phone}")
                                                        putExtra("sms_body", "سلام ${member.name} عزیز، موعد واریز سهم ماهانه صندوق «${pool.title}» به مبلغ ${pool.monthlyAmount.money()} تومان فرارسیده است. با تشکر.")
                                                    }
                                                    context.startActivity(sms)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.AutoMirrored.Rounded.Send, "یادآوری پیامکی", tint = GoldVip, modifier = Modifier.size(15.dp))
                                            }
                                        }

                                        FilterChip(
                                            selected = member.paidThisMonth,
                                            onClick = { onToggleMember(member) },
                                            label = {
                                                Text(
                                                    if (member.paidThisMonth) "پرداخت شد ✅" else "در انتظار ⏳",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                            },
                                            shape = RoundedCornerShape(50),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF10B981),
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showConfirmDelete) {
        com.iliyateam.ghestyar.ui.components.ConfirmDeleteDialog(
            title = "حذف صندوق «${pool.title}»",
            message = "آیا از حذف این صندوق وام خانوادگی با ${members.size.faDigits()} عضو اطمینان دارید؟ تمامی سوابق قرعه‌کشی و واریزی‌ها حذف خواهند شد.",
            onConfirm = {
                showConfirmDelete = false
                onDelete()
            },
            onDismiss = { showConfirmDelete = false }
        )
    }
}

/**
 * نمای حالت بدون صندوق
 */
@Composable
private fun EmptyPoolsView(onCreatePool: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("🎰", fontSize = 52.sp)
            Text("صندوق‌های وام و قرعه‌کشی خانوادگی", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "صندوق‌های قرعه‌کشی دوره‌ای با دوستان، فامیل یا همکاران را ثبت کنید تا واریزی‌ها و قرعه‌کشی گردونه شانس هر ماه به صورت هوشمند و شفاف انجام شود.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onCreatePool,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldVip),
                modifier = Modifier.bounceClick(minScale = 0.94f)
            ) {
                Icon(Icons.Rounded.Add, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("ایجاد اولین صندوق قرعه‌کشی ✨", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

/**
 * دیالوگ گردونه قرعه‌کشی شانس متحرک
 */
@Composable
private fun LotteryWheelDialog(
    poolData: LoanPoolWithMembers,
    onDismiss: () -> Unit,
    onWinnerSelected: (LoanPoolMember) -> Unit
) {
    val eligibleMembers = remember(poolData) { poolData.members.filter { !it.hasWon } }
    var isSpinning by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var selectedWinner by remember { mutableStateOf<LoanPoolMember?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = { if (!isSpinning) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "گردونه شانس «${poolData.pool.title}» 🎰",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldVip
                )

                if (eligibleMembers.isEmpty()) {
                    Text("تمامی اعضای این صندوق قبلاً برنده شده‌اند! دوره با موفقیت به پایان رسیده است.", style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
                } else {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().rotate(rotationAngle)) {
                            val sliceAngle = 360f / eligibleMembers.size
                            val radius = size.minDimension / 2
                            val colors = listOf(
                                Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFF3B82F6),
                                Color(0xFFEC4899), Color(0xFF8B5CF6), Color(0xFF14B8A6),
                                Color(0xFFF97316), Color(0xFF06B6D4)
                            )

                            eligibleMembers.forEachIndexed { i, _ ->
                                drawArc(
                                    color = colors[i % colors.size],
                                    startAngle = i * sliceAngle,
                                    sweepAngle = sliceAngle,
                                    useCenter = true,
                                    size = Size(radius * 2, radius * 2),
                                    topLeft = Offset(center.x - radius, center.y - radius)
                                )
                            }
                        }

                        // نشانگر مرکزی قرعه کشی
                        Surface(shape = CircleShape, color = Color.White, shadowElevation = 6.dp, modifier = Modifier.size(54.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🎯", fontSize = 22.sp)
                            }
                        }
                    }

                    selectedWinner?.let { winner ->
                        ConfettiBurst(active = true, color = GoldVip, modifier = Modifier.size(120.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = GoldVip.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, GoldVip),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("برنده خوش‌شانس این دور: 🎉", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(winner.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = GoldVip)
                                Text("مبلغ وام: ${poolData.pool.winnerPayout.money()} تومان", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (selectedWinner == null) {
                            Button(
                                onClick = {
                                    if (!isSpinning) {
                                        isSpinning = true
                                        coroutineScope.launch {
                                            val spins = Random.nextInt(5, 9)
                                            val winnerIndex = Random.nextInt(eligibleMembers.size)
                                            val slice = 360f / eligibleMembers.size
                                            val targetAngle = (360f * spins) + (winnerIndex * slice) + (slice / 2)

                                            val anim = Animatable(rotationAngle)
                                            anim.animateTo(
                                                targetValue = targetAngle,
                                                animationSpec = tween(
                                                    durationMillis = 3500,
                                                    easing = FastOutSlowInEasing
                                                )
                                            )
                                            rotationAngle = targetAngle % 360f
                                            selectedWinner = eligibleMembers[winnerIndex]
                                            isSpinning = false
                                        }
                                    }
                                },
                                enabled = !isSpinning,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldVip)
                            ) {
                                Text(if (isSpinning) "در حال چرخش... 🎲" else "چرخش گردونه شانس 🎰", fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        } else {
                            Button(
                                onClick = { onWinnerSelected(selectedWinner!!) },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Text("ثبت برنده و تایید 🏆", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * دیالوگ ایجاد صندوق وام جدید
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddLoanPoolDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, monthlyAmount: Long, memberNames: List<String>, startEpochDay: Long, note: String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var monthlyDigits by rememberSaveable { mutableStateOf("1000000") }
    var memberNamesText by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }

    val monthly = monthlyDigits.toLongOrNull() ?: 0L
    val parsedMembers = remember(memberNamesText) {
        memberNamesText.lines().map { it.trim() }.filter { it.isNotBlank() }
    }
    val isValid = title.isNotBlank() && monthly > 0 && parsedMembers.size >= 2

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("ایجاد صندوق وام و قرعه‌کشی جدید", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("نام صندوق (مثلاً صندوق فامیلی مهربانی)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = if (monthlyDigits.isEmpty()) "" else monthly.money(),
                onValueChange = { v -> monthlyDigits = v.cleanNumericDigits(12) },
                label = { Text("مبلغ سهم ماهانه هر عضو (تومان)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = memberNamesText,
                onValueChange = { memberNamesText = it },
                label = { Text("نام اعضا (هر نام در یک خط)") },
                placeholder = { Text("مثال:\nعلی رضایی\nمریم حسینی\nمهدی احمدی\nرضا کمالی") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                minLines = 4,
                maxLines = 7
            )

            if (parsedMembers.size >= 2) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GoldVip.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("تعداد اعضا: ${parsedMembers.size.faDigits()} نفر", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = GoldVip)
                        Text("مبلغ وام برنده هر ماه: ${(parsedMembers.size * monthly).money()} تومان", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            Button(
                onClick = {
                    onSave(title, monthly, parsedMembers, System.currentTimeMillis() / (1000 * 60 * 60 * 24), note)
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldVip)
            ) {
                Text("ایجاد و راه‌اندازی صندوق 👥", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditLoanPoolDialog(
    pool: LoanPool,
    onDismiss: () -> Unit,
    onSave: (title: String, monthlyAmount: Long, note: String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(pool.title) }
    var monthlyDigits by rememberSaveable { mutableStateOf(pool.monthlyAmount.toString()) }
    var note by rememberSaveable { mutableStateOf(pool.note) }

    val monthly = monthlyDigits.toLongOrNull() ?: 0L
    val isValid = title.isNotBlank() && monthly > 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("ویرایش مشخصات صندوق وام", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("نام صندوق") },
                placeholder = { Text("مثال: صندوق فامیلی، همکاران شرکت") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = if (monthlyDigits.isEmpty()) "" else monthly.money(),
                onValueChange = { v -> monthlyDigits = v.cleanNumericDigits(12) },
                label = { Text("سهم واریزی ماهانه هر نفر (تومان)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("توضیحات و قوانین (اختیاری)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                minLines = 2
            )

            Button(
                onClick = { onSave(title, monthly, note) },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldVip)
            ) {
                Text("ذخیره تغییرات صندوق ✅", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

private fun sharePoolReport(context: Context, poolData: LoanPoolWithMembers) {
    val pool = poolData.pool
    val members = poolData.members
    val paid = members.filter { it.paidThisMonth }
    val unpaid = members.filter { !it.paidThisMonth }

    val sb = StringBuilder()
    sb.appendLine("📊 گزارش ماهانه «${pool.title}»")
    sb.appendLine("🗓️ دوره: ماه ${pool.currentRound.faDigits()} از ${pool.totalMembers.faDigits()}")
    sb.appendLine("💰 مبلغ وام برنده: ${pool.winnerPayout.money()} تومان")
    sb.appendLine("───────────────")
    sb.appendLine("✅ واریز شده‌ها (${paid.size.faDigits()} نفر):")
    paid.forEach { sb.appendLine("• ${it.name}") }
    sb.appendLine("───────────────")
    if (unpaid.isNotEmpty()) {
        sb.appendLine("⏳ در انتظار واریز (${unpaid.size.faDigits()} نفر):")
        unpaid.forEach { sb.appendLine("• ${it.name}") }
        sb.appendLine("───────────────")
    }
    sb.appendLine("سامانه مدیریت مالی قسط‌یار")

    val intent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(intent, "ارسال گزارش صندوق"))
}
