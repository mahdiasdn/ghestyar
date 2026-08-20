// ═══ util/Exporter.kt ═══
package com.iliyateam.ghestyar.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.iliyateam.ghestyar.data.ChequeOrDebt
import com.iliyateam.ghestyar.data.Installment
import com.iliyateam.ghestyar.data.InstallmentCategories
import com.iliyateam.ghestyar.data.SavingsGoal
import com.iliyateam.ghestyar.data.Transaction
import com.iliyateam.ghestyar.data.UserProfile
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.time.LocalDate
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import com.iliyateam.ghestyar.data.LoanPool
import com.iliyateam.ghestyar.data.LoanPoolMember

data class FullBackupData(
    val version: Int = 4,
    val timestamp: Long = System.currentTimeMillis(),
    val installments: List<Installment> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val chequesAndDebts: List<ChequeOrDebt> = emptyList(),
    val userProfiles: List<UserProfile> = emptyList(),
    val loanPools: List<LoanPool> = emptyList(),
    val loanPoolMembers: List<LoanPoolMember> = emptyList()
)

enum class SubscriptionTier(
    val id: String,
    val title: String,
    val priceFormatted: String,
    val durationText: String,
    val discountBadge: String? = null
) {
    MONTHLY("monthly", "اشتراک ۱ ماهه", "۴۹٬۰۰۰", "ماهانه"),
    QUARTERLY("quarterly", "اشتراک ۳ ماهه", "۱۱۹٬۰۰۰", "۳ ماهه", "۲۰٪ تخفیف"),
    YEARLY("yearly", "اشتراک ۱ ساله (پیشنهادی)", "۲۹۹٬۰۰۰", "سالانه", "۵۰٪ تخفیف ویژه 🔥"),
    LIFETIME("lifetime", "اشتراک مادام‌العمر VIP", "۵۹۰٬۰۰۰", "یک‌بار برای همیشه", "ارزش استثنایی")
}

object Premium {
    const val MAX_FREE_ACTIVE_INSTALLMENTS = 4

    private const val PREF_NAME = "ghestyar_pref"
    private const val KEY_PREMIUM = "is_premium"
    private const val KEY_PLAN_ID = "premium_plan_id"
    private const val KEY_UNLOCKED_AT = "premium_unlocked_at"

    fun isPremium(ctx: Context): Boolean =
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_PREMIUM, false)

    fun setPremium(ctx: Context, enabled: Boolean, plan: SubscriptionTier = SubscriptionTier.YEARLY) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_PREMIUM, enabled)
            .putString(KEY_PLAN_ID, if (enabled) plan.id else "")
            .putLong(KEY_UNLOCKED_AT, if (enabled) System.currentTimeMillis() else 0L)
            .apply()
    }

    fun getActivePlan(ctx: Context): SubscriptionTier? {
        if (!isPremium(ctx)) return null
        val id = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_PLAN_ID, "")
        return SubscriptionTier.entries.firstOrNull { it.id == id } ?: SubscriptionTier.YEARLY
    }

    fun canAddMoreInstallments(ctx: Context, currentActiveCount: Int): Boolean {
        return isPremium(ctx) || currentActiveCount < MAX_FREE_ACTIVE_INSTALLMENTS
    }
}

object Exporter {

    /**
     * خروجی رسمی مایکروسافت اکسل (.xlsx واقعی با استاندارد OpenXML Zip، استایل، رنگ، فرمت مالی و جمع خودکار)
     */
    fun excelXlsx(ctx: Context, uri: Uri, items: List<Installment>, titleScope: String = "همه اقساط") {
        ctx.contentResolver.openOutputStream(uri)?.use { out ->
            writeXlsxZip(out, items, titleScope)
        }
    }

    private fun writeXlsxZip(outStream: OutputStream, items: List<Installment>, titleScope: String) {
        val zip = ZipOutputStream(outStream)

        fun addEntry(name: String, content: String) {
            zip.putNextEntry(ZipEntry(name))
            zip.write(content.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }

        // 1. [Content_Types].xml
        addEntry("[Content_Types].xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>""")

        // 2. _rels/.rels
        addEntry("_rels/.rels", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>""")

        // 3. xl/_rels/workbook.xml.rels
        addEntry("xl/_rels/workbook.xml.rels", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>""")

        // 4. xl/workbook.xml
        addEntry("xl/workbook.xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="گزارش اقساط" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>""")

        // 5. xl/styles.xml
        addEntry("xl/styles.xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <numFmts count="1">
    <numFmt numFmtId="164" formatCode="#,##0"/>
  </numFmts>
  <fonts count="4">
    <font><name val="Tahoma"/><sz val="10"/><color rgb="FF000000"/></font>
    <font><name val="Tahoma"/><sz val="13"/><b/><color rgb="FFFFFFFF"/></font>
    <font><name val="Tahoma"/><sz val="10"/><b/><color rgb="FF1E293B"/></font>
    <font><name val="Tahoma"/><sz val="9.5"/><color rgb="FF334155"/></font>
  </fonts>
  <fills count="6">
    <fill><patternFill patternType="none"/></fill>
    <fill><patternFill patternType="gray125"/></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FF1F7A54"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFEEF6F2"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFE6F4ED"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFFEF3C7"/></patternFill></fill>
  </fills>
  <borders count="2">
    <border><left/><right/><top/><bottom/></border>
    <border>
      <left style="thin"><color rgb="FFE2E8F0"/></left>
      <right style="thin"><color rgb="FFE2E8F0"/></right>
      <top style="thin"><color rgb="FFE2E8F0"/></top>
      <bottom style="thin"><color rgb="FFE2E8F0"/></bottom>
    </border>
  </borders>
  <cellXfs count="8">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
    <xf numFmtId="0" fontId="1" fillId="2" borderId="0" applyFont="1" applyFill="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
    <xf numFmtId="0" fontId="2" fillId="3" borderId="1" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
    <xf numFmtId="0" fontId="3" fillId="0" borderId="1" applyFont="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
    <xf numFmtId="0" fontId="3" fillId="0" borderId="1" applyFont="1" applyBorder="1" applyAlignment="1"><alignment horizontal="right" vertical="center"/></xf>
    <xf numFmtId="164" fontId="2" fillId="0" borderId="1" applyFont="1" applyNumberFormat="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
    <xf numFmtId="0" fontId="2" fillId="4" borderId="1" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
    <xf numFmtId="0" fontId="2" fillId="5" borderId="1" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
  </cellXfs>
</styleSheet>""")

        // 6. xl/worksheets/sheet1.xml
        val sheetXml = buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            appendLine("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
            appendLine("""<sheetViews><sheetView tabSelected="1" workbookViewId="0" rightToLeft="1"/></sheetViews>""")
            appendLine("""<cols>""")
            appendLine("""<col min="1" max="1" width="6" customWidth="1"/>""")
            appendLine("""<col min="2" max="2" width="28" customWidth="1"/>""")
            appendLine("""<col min="3" max="3" width="18" customWidth="1"/>""")
            appendLine("""<col min="4" max="4" width="14" customWidth="1"/>""")
            appendLine("""<col min="5" max="5" width="20" customWidth="1"/>""")
            appendLine("""<col min="6" max="6" width="12" customWidth="1"/>""")
            appendLine("""<col min="7" max="7" width="12" customWidth="1"/>""")
            appendLine("""<col min="8" max="8" width="22" customWidth="1"/>""")
            appendLine("""<col min="9" max="9" width="22" customWidth="1"/>""")
            appendLine("""<col min="10" max="10" width="14" customWidth="1"/>""")
            appendLine("""<col min="11" max="11" width="14" customWidth="1"/>""")
            appendLine("""<col min="12" max="12" width="14" customWidth="1"/>""")
            appendLine("""<col min="13" max="13" width="26" customWidth="1"/>""")
            appendLine("""</cols>""")
            appendLine("""<sheetData>""")

            val totalAll = items.sumOf { it.totalAmount }
            val paidAll = items.sumOf { it.paidAmount }
            val remAll = items.sumOf { it.remainingAmount }
            val progressPct = if (totalAll > 0) ((paidAll.toDouble() / totalAll) * 100).toInt() else 0

            // Row 1: Banner Title
            appendLine("""<row r="1" ht="30" customHeight="1">""")
            appendLine("""<c r="A1" s="1" t="inlineStr"><is><t>قسط‌یار • گزارش جامع اقساط و تعهدات مالی ($titleScope)</t></is></c>""")
            appendLine("""</row>""")

            // Row 2: Subtitle
            appendLine("""<row r="2" ht="20" customHeight="1">""")
            appendLine("""<c r="A2" t="inlineStr"><is><t>تاریخ صدور: ${LocalDate.now().formatJalaliWithWeekday()} | تعداد کل اقساط: ${items.size} فقره | کل تعهدات: ${totalAll.money()} ت | مانده: ${remAll.money()} ت | پیشرفت: $progressPct%</t></is></c>""")
            appendLine("""</row>""")

            // Row 4: Header
            appendLine("""<row r="4" ht="24" customHeight="1">""")
            val headers = listOf("ردیف", "عنوان وام / قسط", "مقصد / بانک", "دسته‌بندی", "مبلغ هر قسط (تومان)", "تعداد کل", "پرداخت‌شده", "مبلغ کل وام (تومان)", "مانده بدهی (تومان)", "سررسید فعلی", "وضعیت", "آخرین پرداخت", "یادداشت")
            val cols = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M")
            headers.forEachIndexed { i, h ->
                appendLine("""<c r="${cols[i]}4" s="2" t="inlineStr"><is><t>$h</t></is></c>""")
            }
            appendLine("""</row>""")

            // Rows: Data
            items.forEachIndexed { idx, item ->
                val r = idx + 5
                val cleanTitle = item.title.replace("<", "&lt;").replace(">", "&gt;").replace("&", "&amp;")
                val cleanDest = (if (item.destination.isBlank()) "—" else item.destination).replace("<", "&lt;").replace(">", "&gt;").replace("&", "&amp;")
                val catTitle = InstallmentCategories.get(item.category).title
                val due = LocalDate.ofEpochDay(item.dueEpochDay).formatJalali()
                val paidDate = item.paidAtEpochDay?.let { LocalDate.ofEpochDay(it).formatJalali() } ?: "—"
                val cleanNote = (if (item.note.isBlank()) "—" else item.note).replace("<", "&lt;").replace(">", "&gt;").replace("&", "&amp;")
                val statusStyle = if (item.isPaid) "6" else "7"
                val statusText = if (item.isPaid) "تسویه‌شده" else "در جریان"

                appendLine("""<row r="$r" ht="22" customHeight="1">""")
                appendLine("""<c r="A$r" s="3" t="n"><v>${idx + 1}</v></c>""")
                appendLine("""<c r="B$r" s="4" t="inlineStr"><is><t>$cleanTitle</t></is></c>""")
                appendLine("""<c r="C$r" s="3" t="inlineStr"><is><t>$cleanDest</t></is></c>""")
                appendLine("""<c r="D$r" s="3" t="inlineStr"><is><t>$catTitle</t></is></c>""")
                appendLine("""<c r="E$r" s="5" t="n"><v>${item.amount}</v></c>""")
                appendLine("""<c r="F$r" s="3" t="n"><v>${item.totalSessions}</v></c>""")
                appendLine("""<c r="G$r" s="3" t="n"><v>${item.paidSessions}</v></c>""")
                appendLine("""<c r="H$r" s="5" t="n"><v>${item.totalAmount}</v></c>""")
                appendLine("""<c r="I$r" s="5" t="n"><v>${item.remainingAmount}</v></c>""")
                appendLine("""<c r="J$r" s="3" t="inlineStr"><is><t>$due</t></is></c>""")
                appendLine("""<c r="K$r" s="$statusStyle" t="inlineStr"><is><t>$statusText</t></is></c>""")
                appendLine("""<c r="L$r" s="3" t="inlineStr"><is><t>$paidDate</t></is></c>""")
                appendLine("""<c r="M$r" s="4" t="inlineStr"><is><t>$cleanNote</t></is></c>""")
                appendLine("""</row>""")
            }

            // Totals Row
            val totRow = items.size + 5
            appendLine("""<row r="$totRow" ht="24" customHeight="1">""")
            appendLine("""<c r="A$totRow" s="2" t="inlineStr"><is><t>جمع</t></is></c>""")
            appendLine("""<c r="B$totRow" s="2" t="inlineStr"><is><t>مجموع کل اقساط (${items.size} فقره)</t></is></c>""")
            appendLine("""<c r="C$totRow" s="2" t="inlineStr"><is><t>—</t></is></c>""")
            appendLine("""<c r="D$totRow" s="2" t="inlineStr"><is><t>—</t></is></c>""")
            appendLine("""<c r="E$totRow" s="2" t="inlineStr"><is><t>—</t></is></c>""")
            appendLine("""<c r="F$totRow" s="2" t="inlineStr"><is><t>—</t></is></c>""")
            appendLine("""<c r="G$totRow" s="2" t="inlineStr"><is><t>—</t></is></c>""")
            appendLine("""<c r="H$totRow" s="5" t="n"><v>$totalAll</v></c>""")
            appendLine("""<c r="I$totRow" s="5" t="n"><v>$remAll</v></c>""")
            appendLine("""<c r="J$totRow" s="2" t="inlineStr"><is><t>—</t></is></c>""")
            appendLine("""<c r="K$totRow" s="2" t="inlineStr"><is><t>—</t></is></c>""")
            appendLine("""<c r="L$totRow" s="2" t="inlineStr"><is><t>—</t></is></c>""")
            appendLine("""<c r="M$totRow" s="2" t="inlineStr"><is><t>—</t></is></c>""")
            appendLine("""</row>""")

            appendLine("""</sheetData>""")
            appendLine("""<mergeCells count="1">""")
            appendLine("""<mergeCell ref="A1:M1"/>""")
            appendLine("""</mergeCells>""")
            appendLine("""</worksheet>""")
        }

        addEntry("xl/worksheets/sheet1.xml", sheetXml)

        zip.finish()
    }

    /**
     * خروجی رسمی PDF با فرمت استاندارد A4، سربرگ گرافیکی، کارت‌های خلاصه و جدول رسمی
     */
    fun pdf(ctx: Context, uri: Uri, items: List<Installment>, titleScope: String = "همه اقساط") {
        val pdfDoc = PdfDocument()

        val pageWidth = 595
        val pageHeight = 842
        val margin = 28f
        val contentWidth = pageWidth - (margin * 2)

        val totalAll = items.sumOf { it.totalAmount }
        val paidAll = items.sumOf { it.paidAmount }
        val remAll = items.sumOf { it.remainingAmount }
        val progressPct = if (totalAll > 0) ((paidAll.toDouble() / totalAll) * 100).toInt() else 0

        val itemsPerPage = 14
        val pageCount = ((items.size + itemsPerPage - 1) / itemsPerPage).coerceAtLeast(1)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            textSize = 15f
            color = Color.WHITE
            textAlign = Paint.Align.RIGHT
        }
        val subTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9.5f
            color = Color.rgb(230, 244, 237)
            textAlign = Paint.Align.RIGHT
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            textSize = 9.5f
            color = Color.rgb(31, 122, 84)
            textAlign = Paint.Align.CENTER
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f
            color = Color.rgb(30, 41, 59)
            textAlign = Paint.Align.RIGHT
        }
        val centerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f
            color = Color.rgb(30, 41, 59)
            textAlign = Paint.Align.CENTER
        }
        val smallMutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 7.5f
            color = Color.rgb(100, 116, 139)
            textAlign = Paint.Align.CENTER
        }

        for (pageIndex in 0 until pageCount) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
            val page = pdfDoc.startPage(pageInfo)
            val canvas = page.canvas

            canvas.drawColor(Color.WHITE)

            // ۱. سربرگ رسمی زمردی
            val headerHeight = 72f
            paint.color = Color.rgb(31, 122, 84)
            canvas.drawRoundRect(RectF(margin, margin, pageWidth - margin, margin + headerHeight), 14f, 14f, paint)

            canvas.drawText("قسط‌یار • گزارش جامع اقساط و تعهدات مالی", pageWidth - margin - 18f, margin + 28f, titlePaint)
            canvas.drawText("فیلتر: $titleScope  |  تاریخ صدور: ${LocalDate.now().formatJalaliWithWeekday()}", pageWidth - margin - 18f, margin + 50f, subTitlePaint)

            var currentY = margin + headerHeight + 14f

            // ۲. اگر صفحه اول است، باکس‌های خلاصه آماری مدیریتی چاپ شوند
            if (pageIndex == 0) {
                val boxHeight = 52f
                val cardGap = 8f
                val cardWidth = (contentWidth - (cardGap * 3)) / 4

                val summaries = listOf(
                    Triple("کل تعهدات", "${totalAll.money()} ت", Color.rgb(30, 41, 59)),
                    Triple("کل پرداختی", "${paidAll.money()} ت", Color.rgb(31, 122, 84)),
                    Triple("مانده بدهی", "${remAll.money()} ت", Color.rgb(225, 29, 72)),
                    Triple("پیشرفت تسویه", "$progressPct٪", Color.rgb(217, 119, 6))
                )

                summaries.forEachIndexed { i, (label, value, color) ->
                    val left = margin + (i * (cardWidth + cardGap))
                    val rect = RectF(left, currentY, left + cardWidth, currentY + boxHeight)

                    paint.color = Color.rgb(248, 250, 252)
                    canvas.drawRoundRect(rect, 10f, 10f, paint)
                    paint.style = Paint.Style.STROKE
                    paint.color = Color.rgb(226, 232, 240)
                    paint.strokeWidth = 1f
                    canvas.drawRoundRect(rect, 10f, 10f, paint)
                    paint.style = Paint.Style.FILL

                    val textX = left + (cardWidth / 2)
                    smallMutedPaint.color = Color.rgb(100, 116, 139)
                    canvas.drawText(label, textX, currentY + 18f, smallMutedPaint)

                    val valPaint = Paint(centerTextPaint).apply {
                        typeface = Typeface.DEFAULT_BOLD
                        textSize = 9f
                        this.color = color
                    }
                    canvas.drawText(value, textX, currentY + 36f, valPaint)
                }

                currentY += boxHeight + 16f
            }

            // ۳. جدول رسمی اقساط
            val colWidths = floatArrayOf(24f, 110f, 75f, 55f, 65f, 55f, 65f, 50f, 40f)
            val colTitles = arrayOf("ردیف", "عنوان وام/قسط", "مقصد / بانک", "دسته", "مبلغ قسط", "پیشرفت", "مانده بدهی", "سررسید", "وضعیت")

            val tableHeaderHeight = 24f
            paint.color = Color.rgb(238, 246, 242)
            canvas.drawRoundRect(RectF(margin, currentY, pageWidth - margin, currentY + tableHeaderHeight), 6f, 6f, paint)

            var colX = pageWidth - margin
            colTitles.forEachIndexed { idx, colTitle ->
                val w = colWidths[idx]
                canvas.drawText(colTitle, colX - (w / 2), currentY + 15f, headerPaint)
                colX -= w
            }

            currentY += tableHeaderHeight + 2f

            val startIndex = pageIndex * itemsPerPage
            val endIndex = (startIndex + itemsPerPage).coerceAtMost(items.size)
            val pageItems = items.subList(startIndex, endIndex)

            val rowHeight = 24f
            pageItems.forEachIndexed { i, item ->
                val isEven = i % 2 == 0
                if (isEven) {
                    paint.color = Color.rgb(250, 252, 250)
                    canvas.drawRect(RectF(margin, currentY, pageWidth - margin, currentY + rowHeight), paint)
                }

                paint.color = Color.rgb(241, 245, 249)
                canvas.drawLine(margin, currentY + rowHeight, pageWidth - margin, currentY + rowHeight, paint)

                var x = pageWidth - margin

                // ۱. ردیف
                canvas.drawText((startIndex + i + 1).toString(), x - (colWidths[0] / 2), currentY + 15f, centerTextPaint)
                x -= colWidths[0]

                // ۲. عنوان وام
                val titleTrunc = if (item.title.length > 18) item.title.take(16) + ".." else item.title
                canvas.drawText(titleTrunc, x - 4f, currentY + 15f, textPaint)
                x -= colWidths[1]

                // ۳. مقصد/بانک
                val destTrunc = (if (item.destination.isBlank()) "—" else item.destination).let { if (it.length > 13) it.take(11) + ".." else it }
                canvas.drawText(destTrunc, x - (colWidths[2] / 2), currentY + 15f, centerTextPaint)
                x -= colWidths[2]

                // ۴. دسته
                val catTitle = InstallmentCategories.get(item.category).title
                canvas.drawText(catTitle, x - (colWidths[3] / 2), currentY + 15f, centerTextPaint)
                x -= colWidths[3]

                // ۵. مبلغ قسط
                canvas.drawText("${item.amount.money()} ت", x - (colWidths[4] / 2), currentY + 15f, centerTextPaint)
                x -= colWidths[4]

                // ۶. پیشرفت
                val progText = "${item.paidSessions} از ${item.totalSessions}"
                canvas.drawText(progText, x - (colWidths[5] / 2), currentY + 15f, centerTextPaint)
                x -= colWidths[5]

                // ۷. مانده بدهی
                canvas.drawText("${item.remainingAmount.money()} ت", x - (colWidths[6] / 2), currentY + 15f, centerTextPaint)
                x -= colWidths[6]

                // ۸. سررسید
                val dueDate = LocalDate.ofEpochDay(item.dueEpochDay).formatJalali()
                canvas.drawText(dueDate, x - (colWidths[7] / 2), currentY + 15f, centerTextPaint)
                x -= colWidths[7]

                // ۹. وضعیت
                val statusPaint = Paint(centerTextPaint).apply {
                    typeface = Typeface.DEFAULT_BOLD
                    textSize = 7.5f
                    color = if (item.isPaid) Color.rgb(31, 122, 84) else Color.rgb(217, 119, 6)
                }
                canvas.drawText(if (item.isPaid) "تسویه" else "فعال", x - (colWidths[8] / 2), currentY + 15f, statusPaint)

                currentY += rowHeight
            }

            paint.color = Color.rgb(226, 232, 240)
            canvas.drawLine(margin, pageHeight - 32f, pageWidth - margin, pageHeight - 32f, paint)

            canvas.drawText("صفحه ${pageIndex + 1} از $pageCount", margin + 30f, pageHeight - 18f, smallMutedPaint)
            val rightFooterPaint = Paint(smallMutedPaint).apply { textAlign = Paint.Align.RIGHT }
            canvas.drawText("تولید شده توسط دستیار هوشمند اقساط قسط‌یار 📱", pageWidth - margin, pageHeight - 18f, rightFooterPaint)

            pdfDoc.finishPage(page)
        }

        ctx.contentResolver.openOutputStream(uri)?.use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()
    }

    /**
     * اشتراک‌گذاری مستقیم فایل PDF یا اکسل در تلگرام، واتساپ، ایمیل یا پرینتر
     */
    fun shareReportFile(ctx: Context, items: List<Installment>, isPdf: Boolean, titleScope: String = "همه اقساط") {
        val reportsDir = File(ctx.cacheDir, "reports").apply { mkdirs() }
        val ext = if (isPdf) "pdf" else "xlsx"
        val mime = if (isPdf) "application/pdf" else "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        val fileName = "ghestyar-report-${LocalDate.now().formatJalali().replace("/", "-")}.$ext"
        val tempFile = File(reportsDir, fileName)

        if (isPdf) {
            val pUri = Uri.fromFile(tempFile)
            pdf(ctx, pUri, items, titleScope)
        } else {
            FileOutputStream(tempFile).use { fos ->
                writeXlsxZip(fos, items, titleScope)
            }
        }

        val shareUri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", tempFile)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, shareUri)
            putExtra(Intent.EXTRA_SUBJECT, "گزارش اقساط و تعهدات مالی قسط‌یار")
            putExtra(Intent.EXTRA_TEXT, "گزارش مالی اقساط ($titleScope) - اپلیکیشن قسط‌یار")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        ctx.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری گزارش مالی"))
    }

    /** پشتیبان کامل JSON شامل تمام بخش‌های برنامه (اقساط، دخل و خرج، قلک‌ها، چک و طلب، پروفایل‌ها) */
    fun jsonBackup(ctx: Context, uri: Uri, data: FullBackupData) {
        ctx.contentResolver.openOutputStream(uri)?.use { out ->
            val root = JSONObject()
            root.put("version", 3)
            root.put("timestamp", System.currentTimeMillis())

            // ۱. اقساط
            val instArr = JSONArray()
            data.installments.forEach { i ->
                val obj = JSONObject()
                    .put("title", i.title)
                    .put("amount", i.amount)
                    .put("startEpochDay", i.startEpochDay)
                    .put("dueEpochDay", i.dueEpochDay)
                    .put("totalSessions", i.totalSessions)
                    .put("paidSessions", i.paidSessions)
                    .put("isPaid", i.isPaid)
                    .put("paidAtEpochDay", i.paidAtEpochDay ?: JSONObject.NULL)
                    .put("colorIndex", i.colorIndex)
                    .put("category", i.category)
                    .put("remind", i.remind)
                    .put("note", i.note)
                    .put("destination", i.destination)
                    .put("profileId", i.profileId)
                instArr.put(obj)
            }
            root.put("installments", instArr)

            // ۲. تراکنش‌های دخل و خرج
            val txArr = JSONArray()
            data.transactions.forEach { t ->
                val obj = JSONObject()
                    .put("title", t.title)
                    .put("amount", t.amount)
                    .put("isIncome", t.isIncome)
                    .put("category", t.category)
                    .put("epochDay", t.epochDay)
                    .put("note", t.note)
                    .put("profileId", t.profileId)
                    .put("isRecurring", t.isRecurring)
                txArr.put(obj)
            }
            root.put("transactions", txArr)

            // ۳. قلک‌ها و اهداف پس‌انداز
            val goalArr = JSONArray()
            data.savingsGoals.forEach { g ->
                val obj = JSONObject()
                    .put("title", g.title)
                    .put("targetAmount", g.targetAmount)
                    .put("currentAmount", g.currentAmount)
                    .put("targetEpochDay", g.targetEpochDay)
                    .put("emoji", g.emoji)
                    .put("colorIndex", g.colorIndex)
                    .put("note", g.note)
                    .put("profileId", g.profileId)
                goalArr.put(obj)
            }
            root.put("savings_goals", goalArr)

            // ۴. چک‌ها و طلب/بدهی‌ها
            val chequeArr = JSONArray()
            data.chequesAndDebts.forEach { c ->
                val obj = JSONObject()
                    .put("title", c.title)
                    .put("personName", c.personName)
                    .put("amount", c.amount)
                    .put("isCheque", c.isCheque)
                    .put("isReceivable", c.isReceivable)
                    .put("dueEpochDay", c.dueEpochDay)
                    .put("isCleared", c.isCleared)
                    .put("chequeNumber", c.chequeNumber)
                    .put("bankName", c.bankName)
                    .put("note", c.note)
                    .put("profileId", c.profileId)
                chequeArr.put(obj)
            }
            root.put("cheques_and_debts", chequeArr)

            // ۵. پروفایل‌های مالی
            val profArr = JSONArray()
            data.userProfiles.forEach { p ->
                val obj = JSONObject()
                    .put("name", p.name)
                    .put("emoji", p.emoji)
                    .put("colorIndex", p.colorIndex)
                    .put("isDefault", p.isDefault)
                    .put("createdAtEpoch", p.createdAtEpoch)
                profArr.put(obj)
            }
            root.put("user_profiles", profArr)

            // ۶. صندوق‌های وام و قرعه‌کشی
            val poolArr = JSONArray()
            data.loanPools.forEach { pool ->
                val obj = JSONObject()
                    .put("id", pool.id)
                    .put("title", pool.title)
                    .put("monthlyAmount", pool.monthlyAmount)
                    .put("totalMembers", pool.totalMembers)
                    .put("startEpochDay", pool.startEpochDay)
                    .put("winnerPayout", pool.winnerPayout)
                    .put("currentRound", pool.currentRound)
                    .put("note", pool.note)
                    .put("profileId", pool.profileId)
                poolArr.put(obj)
            }
            root.put("loan_pools", poolArr)

            val memberArr = JSONArray()
            data.loanPoolMembers.forEach { m ->
                val obj = JSONObject()
                    .put("id", m.id)
                    .put("poolId", m.poolId)
                    .put("name", m.name)
                    .put("phone", m.phone)
                    .put("lotteryPosition", m.lotteryPosition)
                    .put("hasWon", m.hasWon)
                    .put("wonMonth", m.wonMonth)
                    .put("paidThisMonth", m.paidThisMonth)
                memberArr.put(obj)
            }
            root.put("loan_pool_members", memberArr)

            out.write(root.toString(2).toByteArray(Charsets.UTF_8))
        }
    }

    /** بازیابی جامع پشتیبان JSON با پشتیبانی از تمام بخش‌ها و فایل‌های قبلی */
    fun jsonRestore(ctx: Context, uri: Uri): FullBackupData {
        ctx.contentResolver.openInputStream(uri)?.use { stream ->
            val text = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).readText()
            val root = JSONObject(text)

            // ۱. اقساط
            val instList = mutableListOf<Installment>()
            val instArr = root.optJSONArray("installments") ?: JSONArray()
            for (idx in 0 until instArr.length()) {
                val obj = instArr.getJSONObject(idx)
                instList.add(
                    Installment(
                        title = obj.getString("title"),
                        amount = obj.getLong("amount"),
                        startEpochDay = obj.optLong("startEpochDay", LocalDate.now().toEpochDay()),
                        dueEpochDay = obj.getLong("dueEpochDay"),
                        totalSessions = obj.optInt("totalSessions", 1),
                        paidSessions = obj.optInt("paidSessions", 0),
                        isPaid = obj.optBoolean("isPaid", false),
                        paidAtEpochDay = if (obj.isNull("paidAtEpochDay")) null else obj.optLong("paidAtEpochDay"),
                        colorIndex = obj.optInt("colorIndex", 0),
                        category = obj.optString("category", "bank"),
                        remind = obj.optBoolean("remind", true),
                        note = obj.optString("note", ""),
                        destination = obj.optString("destination", ""),
                        profileId = obj.optLong("profileId", 1L)
                    )
                )
            }

            // ۲. تراکنش‌ها
            val txList = mutableListOf<Transaction>()
            val txArr = root.optJSONArray("transactions") ?: JSONArray()
            for (idx in 0 until txArr.length()) {
                val obj = txArr.getJSONObject(idx)
                txList.add(
                    Transaction(
                        title = obj.getString("title"),
                        amount = obj.getLong("amount"),
                        isIncome = obj.getBoolean("isIncome"),
                        category = obj.optString("category", "other_expense"),
                        epochDay = obj.optLong("epochDay", LocalDate.now().toEpochDay()),
                        note = obj.optString("note", ""),
                        profileId = obj.optLong("profileId", 1L),
                        isRecurring = obj.optBoolean("isRecurring", false)
                    )
                )
            }

            // ۳. قلک‌ها
            val goalList = mutableListOf<SavingsGoal>()
            val goalArr = root.optJSONArray("savings_goals") ?: JSONArray()
            for (idx in 0 until goalArr.length()) {
                val obj = goalArr.getJSONObject(idx)
                goalList.add(
                    SavingsGoal(
                        title = obj.getString("title"),
                        targetAmount = obj.getLong("targetAmount"),
                        currentAmount = obj.optLong("currentAmount", 0L),
                        targetEpochDay = obj.optLong("targetEpochDay", LocalDate.now().plusMonths(3).toEpochDay()),
                        emoji = obj.optString("emoji", "🎯"),
                        colorIndex = obj.optInt("colorIndex", 0),
                        note = obj.optString("note", ""),
                        profileId = obj.optLong("profileId", 1L)
                    )
                )
            }

            // ۴. چک‌ها و طلب‌ها
            val chequeList = mutableListOf<ChequeOrDebt>()
            val chequeArr = root.optJSONArray("cheques_and_debts") ?: JSONArray()
            for (idx in 0 until chequeArr.length()) {
                val obj = chequeArr.getJSONObject(idx)
                chequeList.add(
                    ChequeOrDebt(
                        title = obj.getString("title"),
                        personName = obj.optString("personName", ""),
                        amount = obj.getLong("amount"),
                        isCheque = obj.optBoolean("isCheque", true),
                        isReceivable = obj.optBoolean("isReceivable", false),
                        dueEpochDay = obj.optLong("dueEpochDay", LocalDate.now().toEpochDay()),
                        isCleared = obj.optBoolean("isCleared", false),
                        chequeNumber = obj.optString("chequeNumber", ""),
                        bankName = obj.optString("bankName", ""),
                        note = obj.optString("note", ""),
                        profileId = obj.optLong("profileId", 1L)
                    )
                )
            }

            // ۵. پروفایل‌ها
            val profList = mutableListOf<UserProfile>()
            val profArr = root.optJSONArray("user_profiles") ?: JSONArray()
            for (idx in 0 until profArr.length()) {
                val obj = profArr.getJSONObject(idx)
                profList.add(
                    UserProfile(
                        name = obj.getString("name"),
                        emoji = obj.optString("emoji", "👤"),
                        colorIndex = obj.optInt("colorIndex", 0),
                        isDefault = obj.optBoolean("isDefault", false),
                        createdAtEpoch = obj.optLong("createdAtEpoch", System.currentTimeMillis())
                    )
                )
            }

            // ۶. صندوق‌های وام
            val poolList = mutableListOf<LoanPool>()
            val poolArr = root.optJSONArray("loan_pools") ?: JSONArray()
            for (idx in 0 until poolArr.length()) {
                val obj = poolArr.getJSONObject(idx)
                poolList.add(
                    LoanPool(
                        id = obj.optLong("id", 0L),
                        title = obj.getString("title"),
                        monthlyAmount = obj.getLong("monthlyAmount"),
                        totalMembers = obj.getInt("totalMembers"),
                        startEpochDay = obj.getLong("startEpochDay"),
                        winnerPayout = obj.getLong("winnerPayout"),
                        currentRound = obj.optInt("currentRound", 1),
                        note = obj.optString("note", ""),
                        profileId = obj.optLong("profileId", 1L)
                    )
                )
            }

            val memberList = mutableListOf<LoanPoolMember>()
            val memberArr = root.optJSONArray("loan_pool_members") ?: JSONArray()
            for (idx in 0 until memberArr.length()) {
                val obj = memberArr.getJSONObject(idx)
                memberList.add(
                    LoanPoolMember(
                        id = obj.optLong("id", 0L),
                        poolId = obj.getLong("poolId"),
                        name = obj.getString("name"),
                        phone = obj.optString("phone", ""),
                        lotteryPosition = obj.optInt("lotteryPosition", 0),
                        hasWon = obj.optBoolean("hasWon", false),
                        wonMonth = obj.optInt("wonMonth", 0),
                        paidThisMonth = obj.optBoolean("paidThisMonth", false)
                    )
                )
            }

            return FullBackupData(
                version = root.optInt("version", 1),
                timestamp = root.optLong("timestamp", System.currentTimeMillis()),
                installments = instList,
                transactions = txList,
                savingsGoals = goalList,
                chequesAndDebts = chequeList,
                userProfiles = profList,
                loanPools = poolList,
                loanPoolMembers = memberList
            )
        }
        return FullBackupData()
    }

    /** تولید دفترچه رسمی اقساط بانکی (Printable Official Loan Booklet PDF) */
    fun loanBookletPdf(ctx: Context, uri: Uri, installment: Installment) {
        ctx.contentResolver.openOutputStream(uri)?.use { out ->
            val doc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val baseTf = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            val boldTf = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

            // ۱. کادر حاشیه کل صفحه
            paint.style = Paint.Style.STROKE
            paint.color = Color.parseColor("#006A6E")
            paint.strokeWidth = 2f
            canvas.drawRoundRect(RectF(20f, 20f, 575f, 822f), 12f, 12f, paint)

            paint.strokeWidth = 0.8f
            paint.color = Color.parseColor("#C8D6AF")
            canvas.drawRoundRect(RectF(24f, 24f, 571f, 818f), 10f, 10f, paint)

            // ۲. نوار سربرگ طلایی-سبز رسمی
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#006A6E")
            canvas.drawRoundRect(RectF(30f, 30f, 565f, 95f), 10f, 10f, paint)

            paint.color = Color.parseColor("#D4AF37")
            paint.typeface = boldTf
            paint.textSize = 15f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("دفترچه رسمی و برنامه زمان‌بندی بازپرداخت اقساط", 297f, 58f, paint)

            paint.color = Color.WHITE
            paint.typeface = baseTf
            paint.textSize = 9.5f
            canvas.drawText("سامانه مدیریت هوشمند تعهدات مالی و اعتباری قسط‌یار", 297f, 78f, paint)

            // ۳. کادر مشخصات کلی وام
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#F4F7F4")
            canvas.drawRoundRect(RectF(30f, 105f, 565f, 185f), 8f, 8f, paint)

            paint.style = Paint.Style.STROKE
            paint.color = Color.parseColor("#E0E8E0")
            paint.strokeWidth = 1f
            canvas.drawRoundRect(RectF(30f, 105f, 565f, 185f), 8f, 8f, paint)

            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#2C3E50")
            paint.textAlign = Paint.Align.RIGHT
            paint.textSize = 10.5f

            val totalDebt = installment.amount * installment.totalSessions
            val startJ = LocalDate.ofEpochDay(installment.startEpochDay).toJalali()
            val endJ = LocalDate.ofEpochDay(installment.dueEpochDay).toJalali()

            paint.typeface = boldTf
            canvas.drawText("عنوان تسهیلات: ${installment.title}", 550f, 128f, paint)
            canvas.drawText("بانک / مقصد پرداخت: ${if (installment.destination.isNotBlank()) installment.destination else InstallmentCategories.get(installment.category).title}", 300f, 128f, paint)

            paint.typeface = baseTf
            canvas.drawText("مبلغ هر قسط: ${installment.amount.money()} تومان", 550f, 150f, paint)
            canvas.drawText("تعداد کل اقساط: ${installment.totalSessions.faDigits()} ماه", 300f, 150f, paint)
            canvas.drawText("مبلغ کل بازپرداخت: ${totalDebt.money()} تومان", 550f, 172f, paint)
            canvas.drawText("دوره زمانی: از ${startJ.jy.faDigits()}/${startJ.jm.faDigits()} تا ${endJ.jy.faDigits()}/${endJ.jm.faDigits()}", 300f, 172f, paint)

            // ۴. جدول سررسید اقساط ماه به ماه
            val tableTop = 200f
            paint.color = Color.parseColor("#006A6E")
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(RectF(30f, tableTop, 565f, tableTop + 24f), 6f, 6f, paint)

            paint.color = Color.WHITE
            paint.typeface = boldTf
            paint.textSize = 9.5f
            paint.textAlign = Paint.Align.CENTER

            canvas.drawText("ردیف", 545f, tableTop + 16f, paint)
            canvas.drawText("تاریخ سررسید (شمسی)", 465f, tableTop + 16f, paint)
            canvas.drawText("مبلغ قسط (تومان)", 355f, tableTop + 16f, paint)
            canvas.drawText("وضعیت پرداخت", 230f, tableTop + 16f, paint)
            canvas.drawText("کد پیگیری / تاریخ واریز", 115f, tableTop + 16f, paint)

            var rowY = tableTop + 26f
            val baseStartDate = LocalDate.ofEpochDay(installment.startEpochDay)

            for (session in 1..installment.totalSessions) {
                if (rowY > 770f) break // صفحه اول گنجایش تا ۲۴ قسط را با فاصله دقیق دارد
                val isEven = session % 2 == 0
                val isPaid = session <= installment.paidSessions

                paint.style = Paint.Style.FILL
                paint.color = if (isPaid) Color.parseColor("#E8F5E9") else if (isEven) Color.parseColor("#F9FBF9") else Color.WHITE
                canvas.drawRect(30f, rowY, 565f, rowY + 22f, paint)

                paint.style = Paint.Style.STROKE
                paint.color = Color.parseColor("#E5EBE5")
                paint.strokeWidth = 0.6f
                canvas.drawRect(30f, rowY, 565f, rowY + 22f, paint)

                val sessionDate = baseStartDate.plusMonths((session - 1).toLong()).toJalali()
                val dateStr = "${sessionDate.jy.faDigits()}/${sessionDate.jm.faDigits().padStart(2, '۰')}/${sessionDate.jd.faDigits().padStart(2, '۰')}"

                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#2C3E50")
                paint.typeface = if (isPaid) boldTf else baseTf
                paint.textSize = 9f
                paint.textAlign = Paint.Align.CENTER

                canvas.drawText(session.faDigits(), 545f, rowY + 15f, paint)
                canvas.drawText(dateStr, 465f, rowY + 15f, paint)
                canvas.drawText(installment.amount.money(), 355f, rowY + 15f, paint)

                if (isPaid) {
                    paint.color = Color.parseColor("#2E7D32")
                    canvas.drawText("پرداخت شده [✓]", 230f, rowY + 15f, paint)
                } else {
                    paint.color = Color.parseColor("#888888")
                    canvas.drawText("در انتظار پرداخت [ ]", 230f, rowY + 15f, paint)
                }

                paint.color = Color.parseColor("#666666")
                paint.textSize = 8.5f
                canvas.drawText(if (isPaid) "تأیید شد" else "___________", 115f, rowY + 15f, paint)

                rowY += 22f
            }

            // ۵. پاورقی رسمی اعتبارسنجی
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#006A6E")
            paint.typeface = boldTf
            paint.textSize = 9f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("این سند به منزله دفترچه رسمی برنامه بازپرداخت اقساط است و توسط قسط‌یار صادر گردیده است.", 297f, 800f, paint)

            doc.finishPage(page)
            doc.writeTo(out)
            doc.close()
        }
    }
}