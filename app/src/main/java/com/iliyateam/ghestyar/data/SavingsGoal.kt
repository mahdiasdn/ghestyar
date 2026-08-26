// ═══ data/SavingsGoal.kt ═══
package com.iliyateam.ghestyar.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "savings_goals",
    indices = [
        Index(value = ["profileId"])
    ]
)
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAmount: Long,      // مبلغ هدف به تومان
    val currentAmount: Long = 0, // مبلغ پس‌انداز شده فعلی
    val targetEpochDay: Long,    // تاریخ موعد هدف
    val emoji: String = "🎯",
    val colorIndex: Int = 0,
    val note: String = "",
    val profileId: Long = 1L
) {
    val progress: Float
        get() = if (targetAmount > 0) (currentAmount.toFloat() / targetAmount).coerceIn(0f, 1f) else 0f

    val remainingAmount: Long
        get() = (targetAmount - currentAmount).coerceAtLeast(0L)

    val isCompleted: Boolean
        get() = currentAmount >= targetAmount
}

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY targetEpochDay ASC")
    fun observeAll(): Flow<List<SavingsGoal>>

    @Query("SELECT * FROM savings_goals")
    suspend fun getAll(): List<SavingsGoal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: SavingsGoal): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<SavingsGoal>): List<Long>

    @Update
    suspend fun update(goal: SavingsGoal)

    @Delete
    suspend fun delete(goal: SavingsGoal)

    @Query("DELETE FROM savings_goals WHERE profileId = :profileId")
    suspend fun deleteByProfileId(profileId: Long)
}
