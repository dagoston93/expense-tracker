package com.diamont.expense.tracker.util.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.enums.TransactionFrequency
import com.diamont.expense.tracker.util.enums.TransactionType
import com.google.android.material.datepicker.MaterialDatePicker
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
    @Query("SELECT * FROM transaction_data ORDER BY date DESC, transaction_id DESC")
    fun getAllTransactions() : List<Transaction>

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
     * Replace the categoryId in transaction_data when category deleted with id of 'Unspecified' category
     */
    @Query("UPDATE transaction_data SET category=:replacementId WHERE category=:deletedId")
    fun replaceCategoryIds(deletedId: Int, replacementId: Int)

    /**
     * Replace the planId in transaction_data when plan deleted with -1 (not planned)
     */
    @Query("UPDATE transaction_data SET plan_id=-1 WHERE plan_id=:deletedId")
    fun replaceDeletedPlanIds(deletedId: Int)

    /**
     * Find the date of the last transaction with given plan id
     */
    @Query("SELECT date FROM transaction_data WHERE plan_id=:planId ORDER BY date DESC LIMIT 1")
    fun getLastTransactionDateByPlanId(planId: Int) : Long

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
     * Delete a category
     */
    @Query("DELETE FROM transaction_category WHERE category_id=:categoryId")
    fun deleteCategory(categoryId: Int)

    /**
     * Get category with given id
     */
    @Query("SELECT * FROM transaction_category WHERE category_id = :categoryId LIMIT 1")
    fun getCategoryById(categoryId: Int) : TransactionCategory

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
     * Clear all second parties
     */
    @Query("DELETE FROM second_party_data")
    fun clearSecondPartyData()

    /**
     * Insert plan
     */
    @Insert(entity = Plan::class)
    fun insertPlan(plan: Plan)

    /**
     * Update plan
     */
    @Update(entity = Plan::class)
    fun updatePlan(plan: Plan)

    /**
     * Delete plan
     */
    @Query("DELETE FROM plan_data WHERE id=:id")
    fun deletePlan(id: Int)

    /**
     * Get all plans
     */
    @Query("SELECT * FROM plan_data ORDER BY first_expected_date DESC")
    fun getAllPlans() : List<Plan>

    /**
     * Get plans by type
     */
    @Query("SELECT * FROM plan_data WHERE transaction_type = :type ORDER BY first_expected_date DESC")
    fun getPlansByType(type : TransactionType) : List<Plan>

    /**
     * Get Active plans by type
     */
    @Query("SELECT * FROM plan_data WHERE transaction_type = :type AND is_status_active=1 ORDER BY first_expected_date DESC")
    fun getActivePlansByType(type : TransactionType) : List<Plan>

    /**
     * Cancel a plan
     */
    @Query("UPDATE plan_data SET is_status_active=0 WHERE id=:id")
    fun cancelPlan(id: Int)

    /**
     * Save cancellation date of a plan
     */
    @Query("UPDATE plan_data SET cancellation_date=:date WHERE id=:id")
    fun saveCancellationDateOfPlan(id: Int, date: Long)

    /**
     * Save last completion date of a plan
     */
    @Query("UPDATE plan_data SET last_completed_date=:date WHERE id=:id")
    fun saveLastCompletionDateOfPlan(id: Int, date: Long)

    /**
     * Get plan with given id
     */
    @Query("SELECT * FROM plan_data WHERE id = :id LIMIT 1")
    fun getPlanById(id: Int) : Plan

    /**
     * Clear all plans
     */
    @Query("DELETE FROM plan_data")
    fun clearPlanData()

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
     * Suspend function to get all transactions
     */
    suspend fun getAllTransactionsSuspend() : List<Transaction>{
        return withContext(Dispatchers.IO){
            val data : List<Transaction> = getAllTransactions()
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
            /** Get the transaction before deleting it */
            val transaction = getTransactionById(id)
            deleteTransaction(id)

            /** If it is a planned one we need to update the plan */
            if(transaction.planId != -1){
                val plan = getPlanById(transaction.planId)

                /**
                 * If it is a regular plan, we find the last date it was completed
                 * and save it if the last completion is being deleted.
                 */
                if(plan.frequency != TransactionFrequency.ONE_TIME){
                    if(plan.lastCompletedDate == transaction.date){
                        val lastDate = getLastTransactionDateByPlanId(plan.id)
                        saveLastCompletionDateOfPlan(plan.id, lastDate)
                    }
                }else{
                    /** If it is a one time plan, we re-activate it. */
                    plan.isStatusActive = true
                    plan.lastCompletedDate = 0
                    updatePlan(plan)
                }
            }
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
     * Suspend function to insert category
     */
    suspend fun insertCategorySuspend(category: TransactionCategory, onInsertDone: () -> Unit = {}){
        return withContext(Dispatchers.IO){
            insertCategory(category)
            onInsertDone()
        }
    }

    /**
     * Suspend function to update a category
     */
    suspend fun updateCategorySuspend(category: TransactionCategory, onUpdateDone: ()-> Unit = {}){
        return withContext(Dispatchers.IO){
            updateCategory(category)
            onUpdateDone()
        }
    }

    /**
     * Suspend function to get a category by id
     */
    suspend fun getCategoryByIdSuspend(categoryId: Int) : TransactionCategory{
        return withContext(Dispatchers.IO){
            getCategoryById(categoryId)
        }
    }

    /**
     * Suspend function to delete a category
     */
    suspend fun deleteCategorySuspend(categoryIdToDelete: Int, replacementCategoryId: Int){
        return withContext(Dispatchers.IO){
            deleteCategory(categoryIdToDelete)
            replaceCategoryIds(categoryIdToDelete, replacementCategoryId)
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

    /**
     * Suspend function to insert plan
     */
    suspend fun insertPlanSuspend(plan: Plan){
        return withContext(Dispatchers.IO){
            insertPlan(plan)
        }
    }

    /**
     * Suspend function to update a plan
     */
    suspend fun updatePlanSuspend(plan: Plan){
        return withContext(Dispatchers.IO){
            updatePlan(plan)
        }
    }

    /**
     * Suspend function to delete a plan
     */
    suspend fun deletePlanSuspend(id: Int){
        return withContext(Dispatchers.IO){
            deletePlan(id)
            replaceDeletedPlanIds(id)
        }
    }

    /**
     * Suspend function to get all plans
     */
    suspend fun getAllPlansSuspend() : List<Plan>{
        return withContext(Dispatchers.IO){
            val data : List<Plan> = getAllPlans()
            data
        }
    }

    /**
     * Suspend function to get a transaction by its id
     */
    suspend fun getPlanByIdSuspend(id: Int) : Plan{
        return withContext(Dispatchers.IO){
            val plan = getPlanById(id)
            plan
        }
    }

    /**
     * Suspend function to retrieve expense plans
     */
    suspend fun getExpensePlansSuspend() : List<Plan>{
        return withContext(Dispatchers.IO){
            val data : List<Plan> = getPlansByType(TransactionType.PLAN_EXPENSE)
            data
        }
    }

    /**
     * Suspend function to retrieve income plans
     */
    suspend fun getIncomePlansSuspend() : List<Plan>{
        return withContext(Dispatchers.IO){
            val data : List<Plan> = getPlansByType(TransactionType.PLAN_INCOME)
            data
        }
    }

    /**
     * Suspend function to retrieve active expense plans
     */
    suspend fun getActiveExpensePlansSuspend() : List<Plan>{
        return withContext(Dispatchers.IO){
            val data : List<Plan> = getActivePlansByType(TransactionType.PLAN_EXPENSE)
            data
        }
    }

    /**
     * Suspend function to retrieve active income plans
     */
    suspend fun getActiveIncomePlansSuspend() : List<Plan>{
        return withContext(Dispatchers.IO){
            val data : List<Plan> = getActivePlansByType(TransactionType.PLAN_INCOME)
            data
        }
    }

    /**
     * Suspend function to cancel a plan
     */
    suspend fun cancelPlanSuspend(id: Int){
        return withContext(Dispatchers.IO){
            cancelPlan(id)
            saveCancellationDateOfPlan(id, MaterialDatePicker.todayInUtcMilliseconds())
        }
    }

    /**
     * Suspend function to save last completion date
     */
    suspend fun saveLastCompletionDateOfPlanSuspend(id: Int, date: Long){
        return withContext(Dispatchers.IO){
            saveLastCompletionDateOfPlan(id, date)
        }
    }

    /**
     * Suspend function to find the date of the last transaction with given plan id
     */
    suspend fun getLastTransactionDateByPlanIdSuspend(planId: Int) : Long{
        return withContext(Dispatchers.IO){
            val data: Long = getLastTransactionDateByPlanId(planId)
            data
        }
    }

    /**
     * Suspend function to clear all database
     */
    suspend fun clearDatabaseSuspend(){
        return withContext(Dispatchers.IO){
            clearTransactionDatabase()
            clearCategoryDatabase()
            clearPlanData()
            clearSecondPartyData()
        }
    }

    /**
     * Suspend function to add default categories
     */
    suspend fun addDefaultCategoriesSuspend(context: Context){

        val defaultCategories = listOf<TransactionCategory>(
            TransactionCategory(0, context.getString(R.string.unspecified), R.color.category_color7),
            TransactionCategory(0, context.getString(R.string.food), R.color.category_color1),
            TransactionCategory(0, context.getString(R.string.rent), R.color.category_color2),
            TransactionCategory(0, context.getString(R.string.car_costs), R.color.category_color3)
        )

        return withContext(Dispatchers.IO){
            for(category in defaultCategories){
                insertCategory(category)
            }
        }
    }

}