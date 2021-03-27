package com.diamont.expense.tracker.util.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * The database access object for the database
 */
@Dao
interface TransactionDatabaseDao {
    /**
     * Insert a new transaction
     */
    @Insert(entity = Transaction::class)
    fun insertTransaction(transaction: Transaction)

    /**
     * Update transaction
     */
    @Update(entity = Transaction::class)
    fun updateTransaction(transaction: Transaction)

    /**
     * Get all transactions
     */
    @Query("SELECT * FROM transaction_data ORDER BY date DESC")
    fun getAllTransactions() : List<Transaction>

    /**
     * Clear transaction_data table
     */
    @Query("DELETE FROM transaction_data")
    fun clearTransactionDatabase()

    /**
     * Insert a new category
     */
    @Insert(entity = TransactionCategory::class)
    fun insertCategory(category: TransactionCategory)

    /**
     * Update category
     */
    @Update(entity = TransactionCategory::class)
    fun updateCategory(category: TransactionCategory)

    /**
     * Get all categories
     */
    @Query("SELECT * FROM transaction_category ORDER BY category_id")
    fun getAllCategories() : List<TransactionCategory>

    /**
     * Clear transaction_category table
     */
    @Query("DELETE FROM transaction_category")
    fun clearCategoryDatabase()

    /**
     * Get all venues
     */
    @Query("SELECT venue_name FROM venue_data ORDER BY venue_name")
    fun getAllVenues() : List<String>

}