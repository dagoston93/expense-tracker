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

    /**
     * Insert plan
     */
    @Insert(entity = Plan::class)
    fun insertPlan(plan: Plan)

    /**
     * Get all plans
     */
    @Query("SELECT * FROM plan_data ORDER BY plan_id DESC")
    fun getAllPlans() : List<Plan>


    /**
     * * * * * * * * * SUSPEND FUNCTIONS * * * * * * * * * *
     *
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
     * Suspend function to get all transaction excluded plans
     */
    suspend fun getAllTransactionsExcludePlansSuspend() : List<Transaction>{
        return withContext(Dispatchers.IO){
            val data : List<Transaction> = getAllTransactionsExcludePlans(TransactionType.PLAN_EXPENSE, TransactionType.PLAN_INCOME)
            data
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


}