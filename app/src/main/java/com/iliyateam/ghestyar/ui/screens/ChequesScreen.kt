// ═══ ui/screens/ChequesScreen.kt ═══
package com.iliyateam.ghestyar.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iliyateam.ghestyar.MainViewModel
import com.iliyateam.ghestyar.data.ChequeOrDebt
import com.iliyateam.ghestyar.ui.components.StaggeredItemEntrance
import com.iliyateam.ghestyar.ui.components.bounceClick
import com.iliyateam.ghestyar.ui.theme.*
import com.iliyateam.ghestyar.util.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChequesScreen(
    vm: MainViewModel,
    isPremium: Boolean,
    onOpenPremium: () -> Unit
) {
    val pendingCheques by vm.pendingChequesAndDebts.collectAsStateWithLifecycle()
    val clearedCheques by vm.clearedChequesAndDebts.collectAsStateWithLifecycle()
    val isPrivacyMode by vm.isPrivacyMode.collectAsStateWithLifecycle()

    var showAddChequeDialog by remember { mutableStateOf(false) }
    var editingCheque by remember { mutableStateOf<ChequeOrDebt?>(null) }
    var selectedChequeForReceipt by remember { mutableStateOf<ChequeOrDebt?>(null) }
    var chequeToDelete by remember { mutableStateOf<ChequeOrDebt?>(null) }
    var filterType by remember { mutableIntStateOf(0) } // 0: همه, 1: چک‌های صیادی, 2: طلب و بدهی

    val filteredPending = remember(pendingCheques, filterType) {
        when (filterType) {
            1 -> pendingCheques.filter { it.isCheque }
            2 -> pendingCheques.filter { !it.isCheque }
            else -> pendingCheques
        }
    }

    val totalPayable = remember(pendingCheques) {
        pendingCheques.filter { !it.isReceivable }.sumOf { it.amount }
    }
    val totalReceivable = remember(pendingCheques) {
        pendingCheques.filter { it.isReceivable }.sumOf { it.amount }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // نوار بالای صفحه
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Moss,
                    modifier = Modifier.size(38.dp),
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✍️", fontSize = 18.sp)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("چک‌ها و مطالبات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("مدیریت هوشمند چک‌های صیادی و طلب‌ها", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Button(
                onClick = { showAddChequeDialog = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Moss),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("ثبت جدید", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // کارت‌های خلاصه وضعیت Bento
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .bounceClick(minScale = 0.96f),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, Coral.copy(alpha = 0.3f))
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("مجموع بدهی‌ها", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        if (isPrivacyMode) "••••••" else "${totalPayable.money()} ت",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Coral
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .bounceClick(minScale = 0.96f),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, ChequeBlue.copy(alpha = 0.3f))
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("مجموع مطالبات", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        if (isPrivacyMode) "••••••" else "${totalReceivable.money()} ت",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ChequeBlue
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // فیلترهای نوع (همه، چک، قرض)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("همه موارد", "چک‌های صیادی ✍️", "قرض و طلب 🤝").forEachIndexed { idx, label ->
                val selected = filterType == idx
                FilterChip(
                    selected = selected,
                    onClick = { filterType = idx },
                    label = { Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (idx == 1) ChequeBlue else Moss,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (filteredPending.isEmpty() && clearedCheques.isEmpty()) {
            EmptyChequeStateView(
                emoji = if (filterType == 1) "✍️" else "🤝",
                title = if (filterType == 1) "هیچ چک صیادی ثبت نشده است" else "هیچ چک یا طلب و بدهی ثبت نشده",
                actionText = "ثبت اولین مورد",
                onAction = { showAddChequeDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredPending.isNotEmpty()) {
                    item {
                        Text(
                            "در انتظار سررسید (${filteredPending.size.faDigits()})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    itemsIndexed(filteredPending, key = { _, item -> item.id }) { index, item ->
                        StaggeredItemEntrance(index = index) {
                            ChequeRowItem(
                                item = item,
                                isPrivacy = isPrivacyMode,
                                onToggle = { vm.toggleChequeCleared(item) },
                                onEdit = { editingCheque = item },
                                onShowReceipt = { selectedChequeForReceipt = item },
                                onDelete = { chequeToDelete = item }
                            )
                        }
                    }
                }

                if (clearedCheques.isNotEmpty()) {
                    item {
                        Text(
                            "تسویه‌شده‌ها (${clearedCheques.size.faDigits()})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 10.dp, start = 4.dp)
                        )
                    }
                    itemsIndexed(clearedCheques, key = { _, item -> item.id }) { index, item ->
                        StaggeredItemEntrance(index = index + pendingCheques.size) {
                            ChequeRowItem(
                                item = item,
                                isPrivacy = isPrivacyMode,
                                onToggle = { vm.toggleChequeCleared(item) },
                                onEdit = { editingCheque = item },
                                onShowReceipt = { selectedChequeForReceipt = item },
                                onDelete = { chequeToDelete = item }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddChequeDialog) {
        AddOrEditChequeDialog(
            initialItem = null,
            onDismiss = { showAddChequeDialog = false },
            onSave = { title, person, amount, isCheque, isReceivable, due, chNum, bank ->
                vm.addChequeOrDebt(title, person, amount, isCheque, isReceivable, due, chNum, bank)
                showAddChequeDialog = false
            }
        )
    }

    editingCheque?.let { oldCheque ->
        AddOrEditChequeDialog(
            initialItem = oldCheque,
            onDismiss = { editingCheque = null },
            onSave = { title, person, amount, isCheque, isReceivable, due, chNum, bank ->
                vm.updateChequeOrDebt(oldCheque, title, person, amount, isCheque, isReceivable, due, chNum, bank, oldCheque.note)
                editingCheque = null
            }
        )
    }

    selectedChequeForReceipt?.let { cheque ->
        com.iliyateam.ghestyar.ui.components.ChequeReceiptCardDialog(
            item = cheque,
            onDismiss = { selectedChequeForReceipt = null }
        )
    }

    chequeToDelete?.let { cheque ->
        com.iliyateam.ghestyar.ui.components.ConfirmDeleteDialog(
            title = "حذف «${cheque.title}»",
            message = "آیا از حذف این ${if (cheque.isCheque) "چک صیادی" else "قرض/طلب"} به مبلغ ${cheque.amount.money()} تومان اطمینان دارید؟",
            onConfirm = {
                vm.deleteChequeOrDebt(cheque)
                chequeToDelete = null
            },
            onDismiss = { chequeToDelete = null }
        )
    }
}

@Composable
private fun ChequeRowItem(
    item: ChequeOrDebt,
    isPrivacy: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onShowReceipt: () -> Unit,
    onDelete: () -> Unit
) {
    val due = LocalDate.ofEpochDay(item.dueEpochDay)

    Card(
        onClick = onEdit,
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(minScale = 0.98f),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, ChequeBlue.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Checkbox(
                checked = item.isCleared,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = ChequeBlue)
            )

            // اطلاعات چک و طلب کاملاً امن در برابر شکست عمودی متن
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(if (item.isCheque) "✍️" else "🤝", fontSize = 12.sp)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (item.isReceivable) ChequeBlue.copy(alpha = 0.14f) else MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = if (item.isReceivable) "طلبکاریم" else "بدهکاریم",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isReceivable) ChequeBlue else MaterialTheme.colorScheme.onErrorContainer,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "طرف: ${item.personName} • موعد: ${due.formatJalali()}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = if (isPrivacy) "••••••" else "${item.amount.money()} ت",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (item.isReceivable) ChequeBlue else Coral
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onShowReceipt,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Outlined.Share, "کارت تصویری یادآوری", tint = ChequeBlue, modifier = Modifier.size(16.dp))
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Outlined.Edit, "ویرایش", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Outlined.Delete, "حذف", tint = Coral.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyChequeStateView(
    emoji: String,
    title: String,
    actionText: String,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(emoji, fontSize = 48.sp)
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "چک‌های صیادی و طلب‌ها یا بدهی‌های شخصی‌ات را اینجا ثبت و رهگیری کن.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Moss),
                modifier = Modifier.bounceClick(minScale = 0.94f)
            ) {
                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(Modifier.width(6.dp))
                Text(actionText, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddOrEditChequeDialog(
    initialItem: ChequeOrDebt? = null,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        personName: String,
        amount: Long,
        isCheque: Boolean,
        isReceivable: Boolean,
        due: JalaliDate,
        chequeNumber: String,
        bankName: String
    ) -> Unit
) {
    var isCheque by rememberSaveable { mutableStateOf(initialItem?.isCheque ?: true) }
    var isReceivable by rememberSaveable { mutableStateOf(initialItem?.isReceivable ?: false) }
    var title by rememberSaveable { mutableStateOf(initialItem?.title ?: "") }
    var personName by rememberSaveable { mutableStateOf(initialItem?.personName ?: "") }
    var amountDigits by rememberSaveable { mutableStateOf(initialItem?.amount?.toString() ?: "") }
    var chequeNumber by rememberSaveable { mutableStateOf(initialItem?.chequeNumber ?: "") }
    var bankName by rememberSaveable { mutableStateOf(initialItem?.bankName ?: "") }
    var due by rememberSaveable {
        mutableStateOf(
            initialItem?.dueEpochDay?.let { LocalDate.ofEpochDay(it).toJalali() }
                ?: JalaliDate.today().plusMonths(1)
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }

    val amount = amountDigits.toLongOrNull() ?: 0L
    val isValid = title.isNotBlank() && personName.isNotBlank() && amount > 0

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (initialItem != null) "ویرایش چک یا بدهی / طلب" else "ثبت چک یا بدهی / طلب",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // نوع مورد (چک / قرض)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(3.dp)
            ) {
                Surface(
                    onClick = { isCheque = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isCheque) Moss else Color.Transparent
                ) {
                    Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text("چک صیادی ✍️", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isCheque) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }
                Surface(
                    onClick = { isCheque = false },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = if (!isCheque) Moss else Color.Transparent
                ) {
                    Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text("قرض شخصی 🤝", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (!isCheque) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // طلب یا بدهی
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !isReceivable,
                    onClick = { isReceivable = false },
                    label = { Text("بدهکاریم (پرداختی)", fontSize = 11.sp) },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = isReceivable,
                    onClick = { isReceivable = true },
                    label = { Text("طلبکاریم (دریافتی)", fontSize = 11.sp) },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("بابت چه چیزی؟") },
                placeholder = { Text("مثلاً خرید لپ‌تاپ یا قرض رفاقتی") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = personName,
                onValueChange = { personName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("نام طرف حساب (شخص / شرکت)") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = if (amountDigits.isEmpty()) "" else amount.money(),
                onValueChange = { v -> amountDigits = v.cleanNumericDigits(12) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("مبلغ (تومان)") },
                trailingIcon = {
                    if (amountDigits.isNotEmpty()) {
                        IconButton(onClick = { amountDigits = "" }) {
                            Icon(Icons.Rounded.Close, "پاک کردن", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            if (amount > 0L) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = (if (isReceivable) Moss else Coral).copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, (if (isReceivable) Moss else Coral).copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("✍️", fontSize = 12.sp)
                        Text(
                            "معادل: ${amount.toPersianWords("تومان")}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isReceivable) Moss else Coral
                        )
                    }
                }
            }

            Card(
                onClick = { showDatePicker = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تاریخ سررسید:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(due.toLocalDate().formatJalali(), fontWeight = FontWeight.Bold, color = Moss)
                }
            }

            Button(
                onClick = { onSave(title, personName, amount, isCheque, isReceivable, due, chequeNumber, bankName) },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Moss)
            ) {
                Text(
                    if (initialItem != null) "ذخیره تغییرات ✅" else "ثبت در سیستم ✅",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    if (showDatePicker) {
        JalaliDatePickerSheet(
            initial = due,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                due = it
                showDatePicker = false
            }
        )
    }
}
