package com.diamont.expense.tracker.historyFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.database.Plan
import com.diamont.expense.tracker.util.enums.PaymentMethod
import com.diamont.expense.tracker.util.enums.TransactionFrequency
import com.diamont.expense.tracker.util.enums.TransactionPlanned
import com.diamont.expense.tracker.util.enums.TransactionType
import com.diamont.expense.tracker.util.database.Transaction
import com.diamont.expense.tracker.util.database.TransactionCategory
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import kotlinx.coroutines.*

class HistoryFragmentViewModel(
    private val appContext: Application,
    private val databaseDao: TransactionDatabaseDao) : AndroidViewModel(appContext) {

    /**
     * Set up some live data
     */
    private val _transactionData = MutableLiveData<List<Transaction>>()
    val transactionData : LiveData<List<Transaction>>
        get() = _transactionData

    private val _categories = MutableLiveData<List<TransactionCategory>>()
    val categories : LiveData<List<TransactionCategory>>
        get() = _categories

    /**
     * Set up coroutine job and the scope
     */
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Constructor
     */
    init{

        getTransactionData()
    }

    /**
     * This method retrieves transaction data
     */
    private fun getTransactionData(){
        uiScope.launch {
            //insertTransactionSuspend()
            //insertCategorySuspend()
            _categories.value = getCategoriesSuspend()
            _transactionData.value = getTransactionDataSuspend()
        }
    }

    /**
     * Suspend function to retrieve transaction data
     */
    private suspend fun getTransactionDataSuspend() : List<Transaction>{
        return withContext(Dispatchers.IO){
            val data : List<Transaction> = databaseDao.getAllTransactions()
            data
        }
    }

    /**
     * Suspend function to retrieve categories
     */
    private suspend fun getCategoriesSuspend() : List<TransactionCategory>{
        return withContext(Dispatchers.IO){
            val data : List<TransactionCategory> = databaseDao.getAllCategories()
            data
        }
    }
    /**
     * Suspend function for inserting transaction
     */
    private suspend fun insertTransactionSuspend(){
        return withContext(Dispatchers.IO){
            val plan1 = Plan(0, TransactionType.EXPENSE, "Sth to spend money on", 55.55f,
                2, "Any shop", TransactionFrequency.ONE_TIME, 1617207973000)

            val plan2 = Plan(0, TransactionType.EXPENSE, "Monthly food", 99.88f,
                2, "Food shops", TransactionFrequency.MONTHLY_SUM, 1612113973000)

            val plan3 = Plan(0, TransactionType.INCOME, "Sell a car", 4555.00f,
                2, "Whoever buys it", TransactionFrequency.ONE_TIME, 1618935973000)

            val plan4 = Plan(0, TransactionType.INCOME, "Saaaaaalary", 1250.00f,
                2, "The boossss", TransactionFrequency.MONTHLY_ONCE, 1597249573000)

            databaseDao.inserPlan(plan1)
            databaseDao.inserPlan(plan2)
            databaseDao.inserPlan(plan3)
            databaseDao.inserPlan(plan4)

        }
    }

    /**
     * Suspend function for inserting category
     */
    private suspend fun insertCategorySuspend(){
        return withContext(Dispatchers.IO){
            val cat3 = TransactionCategory(0,"Unspecified", android.R.color.holo_blue_dark)
            val cat1 = TransactionCategory(0,"Food", R.color.secondaryColor)
            val cat2 = TransactionCategory(0,"Clothes", R.color.circularProgressbarBackground)
            val cat5 = TransactionCategory(0,"Salary", android.R.color.holo_purple)

            databaseDao.insertCategory(cat3)
            databaseDao.insertCategory(cat1)
            databaseDao.insertCategory(cat2)
            databaseDao.insertCategory(cat5)
        }
    }

    /**
     * onCleared() is called when view model is destroyed
     * in this case we need to cancel coroutines
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }


}