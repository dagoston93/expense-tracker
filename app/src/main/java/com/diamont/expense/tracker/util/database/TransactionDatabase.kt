package com.diamont.expense.tracker.util.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Our room database
 */
@Database(
    entities = [Transaction::class, TransactionCategory::class],
    version = 3,
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
                        .fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}