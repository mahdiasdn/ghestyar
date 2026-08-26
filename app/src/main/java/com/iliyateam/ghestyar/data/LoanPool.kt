// ═══ data/LoanPool.kt ═══
package com.iliyateam.ghestyar.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "loan_pools",
    indices = [Index(value = ["profileId"])]
)
data class LoanPool(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val monthlyAmount: Long, // سهم ماهانه هر نفر
    val totalMembers: Int, // تعداد اعضا / ماه‌ها
    val startEpochDay: Long,
    val winnerPayout: Long, // مبلغ کل وام برنده = monthlyAmount * totalMembers
    val currentRound: Int = 1, // دور/ماه فعلی
    val note: String = "",
    val profileId: Long = 1
)

@Entity(
    tableName = "loan_pool_members",
    foreignKeys = [
        ForeignKey(
            entity = LoanPool::class,
            parentColumns = ["id"],
            childColumns = ["poolId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["poolId"])]
)
data class LoanPoolMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val poolId: Long,
    val name: String,
    val phone: String = "",
    val lotteryPosition: Int = 0, // نوبت قرعه کشی (۱ تا N)
    val hasWon: Boolean = false,
    val wonMonth: Int = 0,
    val paidThisMonth: Boolean = false
)

data class LoanPoolWithMembers(
    @Embedded val pool: LoanPool,
    @Relation(
        parentColumn = "id",
        entityColumn = "poolId"
    )
    val members: List<LoanPoolMember>
)

@Dao
interface LoanPoolDao {
    @Query("SELECT * FROM loan_pools WHERE profileId = :profileId ORDER BY id DESC")
    fun getPoolsByProfile(profileId: Long): Flow<List<LoanPool>>

    @androidx.room.Transaction
    @Query("SELECT * FROM loan_pools WHERE profileId = :profileId ORDER BY id DESC")
    fun getPoolsWithMembers(profileId: Long): Flow<List<LoanPoolWithMembers>>

    @androidx.room.Transaction
    @Query("SELECT * FROM loan_pools WHERE id = :poolId LIMIT 1")
    fun getPoolWithMembersById(poolId: Long): Flow<LoanPoolWithMembers?>

    @Query("SELECT * FROM loan_pool_members WHERE poolId = :poolId ORDER BY lotteryPosition ASC, id ASC")
    fun getMembersByPool(poolId: Long): Flow<List<LoanPoolMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPool(pool: LoanPool): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<LoanPoolMember>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: LoanPoolMember): Long

    @Update
    suspend fun updatePool(pool: LoanPool)

    @Update
    suspend fun updateMember(member: LoanPoolMember)

    @Update
    suspend fun updateMembers(members: List<LoanPoolMember>)

    @Delete
    suspend fun deletePool(pool: LoanPool)

    @Delete
    suspend fun deleteMember(member: LoanPoolMember)

    @Query("SELECT * FROM loan_pools")
    suspend fun getAllPools(): List<LoanPool>

    @Query("SELECT * FROM loan_pool_members")
    suspend fun getAllMembers(): List<LoanPoolMember>

    @Query("DELETE FROM loan_pools WHERE profileId = :profileId")
    suspend fun deletePoolsByProfileId(profileId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPools(pools: List<LoanPool>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMembers(members: List<LoanPoolMember>): List<Long>
}
