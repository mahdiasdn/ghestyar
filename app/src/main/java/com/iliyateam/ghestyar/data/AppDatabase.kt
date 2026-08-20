// ═══ data/AppDatabase.kt ═══
package com.iliyateam.ghestyar.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Installment::class,
        Transaction::class,
        SavingsGoal::class,
        ChequeOrDebt::class,
        UserProfile::class,
        LoanPool::class,
        LoanPoolMember::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun installmentDao(): InstallmentDao
    abstract fun transactionDao(): TransactionDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun chequeOrDebtDao(): ChequeOrDebtDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun loanPoolDao(): LoanPoolDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `amount` INTEGER NOT NULL, `isIncome` INTEGER NOT NULL, `category` TEXT NOT NULL, `epochDay` INTEGER NOT NULL, `note` TEXT NOT NULL, `profileId` INTEGER NOT NULL DEFAULT 1)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `savings_goals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `targetAmount` INTEGER NOT NULL, `currentAmount` INTEGER NOT NULL, `emoji` TEXT NOT NULL, `colorIndex` INTEGER NOT NULL, `targetEpochDay` INTEGER, `profileId` INTEGER NOT NULL DEFAULT 1)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `cheques_or_debts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `amount` INTEGER NOT NULL, `dueEpochDay` INTEGER NOT NULL, `isCheque` INTEGER NOT NULL, `isPayable` INTEGER NOT NULL, `isCleared` INTEGER NOT NULL, `sayadNumber` TEXT NOT NULL, `bankName` TEXT NOT NULL, `contactPerson` TEXT NOT NULL, `note` TEXT NOT NULL, `profileId` INTEGER NOT NULL DEFAULT 1)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `user_profiles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `emoji` TEXT NOT NULL, `colorIndex` INTEGER NOT NULL, `isDefault` INTEGER NOT NULL)")
                db.execSQL("INSERT OR IGNORE INTO `user_profiles` (`id`, `name`, `emoji`, `colorIndex`, `isDefault`) VALUES (1, 'حساب اصلی', '👤', 0, 1)")
                try {
                    db.execSQL("ALTER TABLE `installments` ADD COLUMN `profileId` INTEGER NOT NULL DEFAULT 1")
                } catch (_: Exception) {}
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE `installments` ADD COLUMN `destination` TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `loan_pools` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `monthlyAmount` INTEGER NOT NULL, `totalMembers` INTEGER NOT NULL, `startEpochDay` INTEGER NOT NULL, `winnerPayout` INTEGER NOT NULL, `currentRound` INTEGER NOT NULL DEFAULT 1, `note` TEXT NOT NULL DEFAULT '', `profileId` INTEGER NOT NULL DEFAULT 1)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_loan_pools_profileId` ON `loan_pools` (`profileId`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `loan_pool_members` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `poolId` INTEGER NOT NULL, `name` TEXT NOT NULL, `phone` TEXT NOT NULL DEFAULT '', `lotteryPosition` INTEGER NOT NULL DEFAULT 0, `hasWon` INTEGER NOT NULL DEFAULT 0, `wonMonth` INTEGER NOT NULL DEFAULT 0, `paidThisMonth` INTEGER NOT NULL DEFAULT 0, FOREIGN KEY(`poolId`) REFERENCES `loan_pools`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_loan_pool_members_poolId` ON `loan_pool_members` (`poolId`)")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE `transactions` ADD COLUMN `isRecurring` INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {}
            }
        }

        fun get(ctx: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    ctx.applicationContext,
                    AppDatabase::class.java,
                    "ghestyar.db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                .fallbackToDestructiveMigrationOnDowngrade()
                .build().also { INSTANCE = it }
            }
    }
}