// ═══ data/ChequeOrDebt.kt ═══
package com.iliyateam.ghestyar.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cheques_and_debts")
data class ChequeOrDebt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val personName: String,
    val amount: Long,            // تومان
    val isCheque: Boolean,       // true: چک صیادی / false: قرض شخصی
    val isReceivable: Boolean,   // true: طلبکاریم (دریافتی) / false: بدهکاریم (پرداختی)
    val dueEpochDay: Long,       // تاریخ سررسید
    val isCleared: Boolean = false, // آیا پاس شده یا تسویه شده؟
    val chequeNumber: String = "",
    val bankName: String = "",
    val note: String = "",
    val profileId: Long = 1L
)

@Dao
interface ChequeOrDebtDao {
    @Query("SELECT * FROM cheques_and_debts WHERE isCleared = 0 ORDER BY dueEpochDay ASC")
    fun observePending(): Flow<List<ChequeOrDebt>>

    @Query("SELECT * FROM cheques_and_debts WHERE isCleared = 1 ORDER BY dueEpochDay DESC")
    fun observeCleared(): Flow<List<ChequeOrDebt>>

    @Query("SELECT * FROM cheques_and_debts")
    suspend fun getAll(): List<ChequeOrDebt>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ChequeOrDebt): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ChequeOrDebt>): List<Long>

    @Update
    suspend fun update(item: ChequeOrDebt)

    @Delete
    suspend fun delete(item: ChequeOrDebt)
}
