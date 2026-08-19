// ═══ data/Transaction.kt ═══
package com.iliyateam.ghestyar.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Long,           // تومان
    val isIncome: Boolean,      // true: درآمد / false: هزینه
    val category: String,       // دسته‌بندی
    val epochDay: Long,         // تاریخ ثبت
    val note: String = "",
    val profileId: Long = 1L
)

data class TransactionCategory(
    val id: String,
    val title: String,
    val emoji: String,
    val isIncome: Boolean
)

object TransactionCategories {
    val expenseCategories = listOf(
        TransactionCategory("food", "خوراک و سوپرمارکت", "🛒", false),
        TransactionCategory("housing", "مسکن و اجاره", "🏠", false),
        TransactionCategory("transport", "حمل و نقل و بنزین", "🚗", false),
        TransactionCategory("bills", "قبوض و شارژ", "⚡", false),
        TransactionCategory("entertainment", "تفریح و رستوران", "☕", false),
        TransactionCategory("clothing", "پوشاک و خرید", "👕", false),
        TransactionCategory("health", "درمان و دارو", "💊", false),
        TransactionCategory("education", "آموزش و کتاب", "📚", false),
        TransactionCategory("other_expense", "سایر هزینه‌ها", "📦", false)
    )

    val incomeCategories = listOf(
        TransactionCategory("salary", "حقوق و دستمزد", "💼", true),
        TransactionCategory("freelance", "پروژه و فریلنس", "💻", true),
        TransactionCategory("investment", "سود و سرمایه‌گذاری", "📈", true),
        TransactionCategory("gift", "هدیه و پاداش", "🎁", true),
        TransactionCategory("subsidy", "یارانه", "🏛️", true),
        TransactionCategory("other_income", "سایر درآمدها", "💰", true)
    )

    fun get(id: String, isIncome: Boolean): TransactionCategory {
        val list = if (isIncome) incomeCategories else expenseCategories
        return list.firstOrNull { it.id == id } ?: list.last()
    }
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY epochDay DESC, id DESC")
    fun observeAll(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE isIncome = 1 ORDER BY epochDay DESC")
    fun observeIncomes(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE isIncome = 0 ORDER BY epochDay DESC")
    fun observeExpenses(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE epochDay BETWEEN :startDay AND :endDay ORDER BY epochDay DESC")
    fun observeMonthRange(startDay: Long, endDay: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions")
    suspend fun getAll(): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Transaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Transaction>): List<Long>

    @Update
    suspend fun update(item: Transaction)

    @Delete
    suspend fun delete(item: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
