package com.diamont.expense.tracker.util.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.diamont.expense.tracker.util.enums.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
     * Delete transaction
     */
    @Query("DELETE FROM transaction_data WHERE transaction_id=:id")
    fun deleteTransaction(id: Int)

    /**
     * Get all transactions
     */
    @Query("SELECT * FROM transaction_data ORDER BY date DESC")
    fun getAllTransactions() : List<Transaction>

    /**
     * Get transactions, exclude plans
     */
    @Query("SELECT * FROM transaction_data WHERE type != :expensePlan AND type != :incomePlan ORDER BY date DESC")
    fun getAllTransactionsExcludePlans(expensePlan: TransactionType, incomePlan: TransactionType) : List<Transaction>

    /**
     * Get Transaction types
     */
    @Query("SELECT * FROM transaction_data WHERE type = :type ORDER BY DATE DESC")
    fun getTransactionsByType(type : TransactionType) : List<Transaction>

    /**
     * Get Transaction by id
     */
    @Query("SELECT * FROM transaction_data WHERE transaction_id = :id LIMIT 1")
    fun getTransactionById(id : Int) : Transaction

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
     * Add new venue/source
     */
    @Insert(entity = SecondPartyData::class)
    fun insertSecondPartyData(data: SecondPartyData)

    /**
     * Get all venues and recipients
     */
    @Query("SELECT name FROM second_party_data WHERE is_recipient=1 ORDER BY name")
    fun getAllVenues() : List<String>

    /**
     * Get all sources
     */
    @Query("SELECT name FROM second_party_data WHERE is_recipient=0 ORDER BY name")
    fun getAllSources() : List<String>

    /**
     * * * * * * * * * SUSPEND FUNCTIONS * * * * * * * * * *
     *
     * Suspend function to insert a transaction
     */
    suspend fun insertTransactionSuspend(transaction: Transaction){
        return withContext(Dispatchers.IO){
            insertTransaction(transaction)
        }
    }

    /**
     * Suspend function to get all transaction excluded plans
     */
    suspend fun getAllTransactionsExcludePlansSuspend() : List<Transaction>{
        return withContext(Dispatchers.IO){
            val data : List<Transaction> = getAllTransactionsExcludePlans(TransactionType.PLAN_EXPENSE, TransactionType.PLAN_INCOME)
            data
        }
    }

    /**
     * Suspend function to get a transaction by its id
     */
    suspend fun getTransactionByIdSuspend(id: Int) : Transaction{
        return withContext(Dispatchers.IO){
            val transaction = getTransactionById(id)
            transaction
        }
    }

    /**
     * Suspend function to update a transaction
     */
    suspend fun updateTransactionSuspend(transaction: Transaction){
        return withContext(Dispatchers.IO){
            updateTransaction(transaction)
        }
    }

    /**
     * Suspend function to delete a transaction
     */
    suspend fun deleteTransactionSuspend(id: Int){
        return withContext(Dispatchers.IO){
            deleteTransaction(id)
        }
    }

    /**
     * Suspend function to retrieve categories from database
     */
    suspend fun getCategoriesSuspend() : List<TransactionCategory>{
        return withContext(Dispatchers.IO){
            val data : List<TransactionCategory> = getAllCategories()
            data
        }
    }

    /**
     * Suspend function to retrieve expense plans
     */
    suspend fun getExpensePlansSuspend() : List<Transaction>{
        return withContext(Dispatchers.IO){
            val data : List<Transaction> = getTransactionsByType(TransactionType.PLAN_EXPENSE)
            data
        }
    }

    /**
     * Suspend function to retrieve income plans
     */
    suspend fun getIncomePlansSuspend() : List<Transaction>{
        return withContext(Dispatchers.IO){
            val data : List<Transaction> = getTransactionsByType(TransactionType.PLAN_INCOME)
            data
        }
    }

    /**
     * Suspend function to insert second part data
     */
    suspend fun insertSecondPartyDataSuspend(data: SecondPartyData){
        return withContext(Dispatchers.IO){
            insertSecondPartyData(data)
        }
    }

    /**
     * Suspend function to retrieve venues
     */
    suspend fun getVenuesSuspend() : List<String> {
        return withContext(Dispatchers.IO){
            val data : List<String> = getAllVenues()
            data
        }
    }

    /**
     * Suspend function to retrieve venues
     */
    suspend fun getSourcesSuspend() : List<String> {
        return withContext(Dispatchers.IO){
            val data : List<String> = getAllSources()
            data
        }
    }


}