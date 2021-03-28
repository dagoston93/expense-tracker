package com.diamont.expense.tracker.util.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Our room database
 */
@Database(
    entities = [Transaction::class, TransactionCategory::class, VenueData::class, Plan::class],
    version = 7,
    exportSchema = false
)
abstract class TransactionDatabase : RoomDatabase(){
    abstract val transactionDatabaseDao : TransactionDatabaseDao

    companion object{
        @Volatile
        private var INSTANCE : TransactionDatabase? = null

        fun getInstance(context: Context) : TransactionDatabase {
            synchronized(this) { // Only one thread can enter this block of code at once
                var instance = INSTANCE

                if(instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        TransactionDatabase::class.java,
                        "transaction_database"
                    )
                        //.fallbackToDestructiveMigration()
                        .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                        .build()

                    INSTANCE = instance
                }
                return instance
            }
        }

        private val MIGRATION_3_4 = object : Migration(3,4){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `venue_data` (`venue_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `venue_name` TEXT NOT NULL)");
                database.execSQL("INSERT INTO venue_data (venue_name) VALUES ('Intersparhelt')")
                database.execSQL("INSERT INTO venue_data (venue_name) VALUES ('TESCO')")
                database.execSQL("INSERT INTO venue_data (venue_name) VALUES ('Spar')")
                database.execSQL("INSERT INTO venue_data (venue_name) VALUES ('Aldi')")
                database.execSQL("INSERT INTO venue_data (venue_name) VALUES ('Lidli')")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4,5){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `plan_data` (`plan_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type` TEXT NOT NULL, `description` TEXT NOT NULL, `amount` REAL NOT NULL, `category` INTEGER NOT NULL, `second_party` TEXT NOT NULL, `frequency` TEXT NOT NULL, `date` INTEGER NOT NULL)");
            }
        }

        private val MIGRATION_5_6 = object : Migration(5,6){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `transaction_data` ADD `plan_id` INTEGER NOT NULL DEFAULT -1")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6,7){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `plan_data` ADD `is_active` INTEGER NOT NULL DEFAULT 1")
            }
        }
    }
}