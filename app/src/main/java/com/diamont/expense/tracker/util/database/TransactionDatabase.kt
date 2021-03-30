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
    entities = [Transaction::class, TransactionCategory::class, VenueData::class],
    version = 11,
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
                        .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7,
                            MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                        .build()

                    INSTANCE = instance
                }
                return instance
            }
        }

        private val MIGRATION_3_4 = object : Migration(3,4){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `venue_data` (`venue_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `venue_name` TEXT NOT NULL)");
                database.execSQL("INSERT INTO venue_data (venue_name) VALUES ('Interspar')")
                database.execSQL("INSERT INTO venue_data (venue_name) VALUES ('TESCO')")
                database.execSQL("INSERT INTO venue_data (venue_name) VALUES ('Spar')")
                database.execSQL("INSERT INTO venue_data (venue_name) VALUES ('Aldi')")
                database.execSQL("INSERT INTO venue_data (venue_name) VALUES ('Lidl')")
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

        private val MIGRATION_7_8 = object : Migration(7,8){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE plan_data")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8,9){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `venue_data` ADD `is_recipient` INTEGER NOT NULL DEFAULT 1")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9,10){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `venue_data` RENAME TO `second_party_data`")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10,11){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE `second_party_data`")
                database.execSQL("CREATE TABLE IF NOT EXISTS `second_party_data` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `is_recipient` INTEGER NOT NULL)")
                database.execSQL("INSERT INTO `second_party_data` (`name`, `is_recipient`) VALUES ('Interspar', 1)")
                database.execSQL("INSERT INTO `second_party_data` (`name`, `is_recipient`) VALUES ('TESCO', 1)")
                database.execSQL("INSERT INTO `second_party_data` (`name`, `is_recipient`) VALUES ('Spar', 1)")
                database.execSQL("INSERT INTO `second_party_data` (`name`, `is_recipient`) VALUES ('Aldi', 1)")
                database.execSQL("INSERT INTO `second_party_data` (`name`, `is_recipient`) VALUES ('Lidl', 1)")
            }
        }
    }
}