package com.diamont.expense.tracker.historyFragment

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.enums.TransactionFrequency
import com.diamont.expense.tracker.util.enums.TransactionType
import com.diamont.expense.tracker.util.database.Transaction
import com.diamont.expense.tracker.util.database.TransactionCategory
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import com.diamont.expense.tracker.util.enums.PaymentMethod
import com.diamont.expense.tracker.util.enums.TransactionPlanned
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
     * Trigger this event when user clicks on an edit icon
     * by setting the transaction id as the value.
     *
     * After passing the event to activity view model reset it to null.
     */
    val eventNavigateToEditFragment = MutableLiveData<Int?>(null)

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
            _categories.value = databaseDao.getCategoriesSuspend()
            _transactionData.value = databaseDao.getAllTransactionsExcludePlansSuspend()
        }
    }

    /**
     * Suspend function for inserting transaction
     */
    private suspend fun insertTransactionSuspend(){
        return withContext(Dispatchers.IO){
            val plan1 = Transaction(0, TransactionType.PLAN_EXPENSE, "Sth to spend money on", 55.55f,
                2, "Any shop", PaymentMethod.CARD, TransactionPlanned.PLANNED,
                TransactionFrequency.ONE_TIME, 1617207973000, 0)

            val plan2 = Transaction(0, TransactionType.PLAN_EXPENSE, "Monthly food", 99.88f,
                0, "Food shops", PaymentMethod.CARD, TransactionPlanned.PLANNED,
                TransactionFrequency.MONTHLY_SUM, 1612113973000, 0)

            val plan3 = Transaction(0, TransactionType.PLAN_INCOME, "Sell a car", 4555.00f,
                2, "Whoever buys it", PaymentMethod.CARD, TransactionPlanned.PLANNED,
                TransactionFrequency.ONE_TIME, 1618935973000, 0)

            val plan4 = Transaction(0, TransactionType.PLAN_INCOME, "Saaaaaalary", 1250.00f,
                2, "The boossss", PaymentMethod.CARD, TransactionPlanned.PLANNED,
                TransactionFrequency.MONTHLY_ONCE, 1597249573000, 0)

//            val plan2 = Plan(0, TransactionType.EXPENSE, "Monthly food", 99.88f,
//                2, "Food shops", TransactionFrequency.MONTHLY_SUM, 1612113973000)
//
//            val plan3 = Plan(0, TransactionType.INCOME, "Sell a car", 4555.00f,
//                2, "Whoever buys it", TransactionFrequency.ONE_TIME, 1618935973000)
//
//            val plan4 = Plan(0, TransactionType.INCOME, "Saaaaaalary", 1250.00f,
//                2, "The boossss", TransactionFrequency.MONTHLY_ONCE, 1597249573000)

            databaseDao.insertTransaction(plan1)
            databaseDao.insertTransaction(plan2)
            databaseDao.insertTransaction(plan3)
            databaseDao.insertTransaction(plan4)

        }
    }

    /**
     * Call this method when user clicks the delete button of a transaction
     */
    fun deleteTransaction(transactionId: Int){
        Log.d("GUS", "del: $transactionId")
        uiScope.launch {
            databaseDao.deleteTransactionSuspend(transactionId)
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