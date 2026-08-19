// ═══ data/Installment.kt ═══
package com.iliyateam.ghestyar.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "installments")
data class Installment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Long,                 // تومان
    val startEpochDay: Long,          // شروع دورهٔ فعلی
    val dueEpochDay: Long,            // سررسید
    val totalSessions: Int = 1,       // تعداد کل اقساط
    val paidSessions: Int = 0,
    val isPaid: Boolean = false,      // آیا دورهٔ فعلی پرداخت شده؟
    val paidAtEpochDay: Long? = null,
    val colorIndex: Int = 0,
    val category: String = "bank",    // دسته‌بندی قسط
    val remind: Boolean = true,
    val note: String = "",
    val profileId: Long = 1L
) {
    /** مجموع کل مبلغ وام/قسط */
    val totalAmount: Long
        get() = amount * totalSessions

    /** مجموع مبلغ پرداخت‌شده تاکنون */
    val paidAmount: Long
        get() = amount * paidSessions

    /** مبلغ باقیمانده */
    val remainingAmount: Long
        get() = (amount * (totalSessions - paidSessions)).coerceAtLeast(0L)

    /** درصد پیشرفت کل اقساط */
    val overallProgress: Float
        get() = if (totalSessions > 0) (paidSessions.toFloat() / totalSessions).coerceIn(0f, 1f) else 0f
}

data class InstallmentCategory(
    val id: String,
    val title: String,
    val emoji: String
)

object InstallmentCategories {
    val list = listOf(
        InstallmentCategory("bank", "وام بانکی", "🏦"),
        InstallmentCategory("check", "چک", "✍️"),
        InstallmentCategory("shopping", "خرید قسطی", "🛍️"),
        InstallmentCategory("vehicle", "خودرو", "🚗"),
        InstallmentCategory("home", "مسکن و اجاره", "🏠"),
        InstallmentCategory("personal", "قرض شخصی", "🤝"),
        InstallmentCategory("insurance", "بیمه", "🛡️"),
        InstallmentCategory("other", "متفرقه", "📦")
    )

    fun get(id: String): InstallmentCategory =
        list.firstOrNull { it.id == id } ?: list.last()
}

@Dao
interface InstallmentDao {
    @Query("SELECT * FROM installments WHERE isPaid = 0 ORDER BY dueEpochDay ASC")
    fun observeActive(): Flow<List<Installment>>

    @Query("SELECT * FROM installments WHERE isPaid = 1 ORDER BY paidAtEpochDay DESC")
    fun observeHistory(): Flow<List<Installment>>

    @Query("SELECT * FROM installments")
    suspend fun getAll(): List<Installment>

    @Query("SELECT * FROM installments WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Installment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Installment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Installment>): List<Long>

    @Update
    suspend fun update(item: Installment)

    @Delete
    suspend fun delete(item: Installment)

    @Query("DELETE FROM installments")
    suspend fun deleteAll()
}