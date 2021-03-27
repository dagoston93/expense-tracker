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
    version = 4,
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
                        .addMigrations(MIGRATION_3_4)
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
    }
}