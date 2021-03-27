package com.diamont.expense.tracker.historyFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diamont.expense.tracker.R
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

    /******
     * SOME TEST DATA TO INSERT
     */
    val cat1 = TransactionCategory(0,"Food", R.color.secondaryColor)
    val cat2 = TransactionCategory(0,"Clothes", R.color.circularProgressbarBackground)
    val cat3 = TransactionCategory(0,"Unspecified", android.R.color.holo_blue_dark)
    val cat4 = TransactionCategory(0,"Unspecified", android.R.color.holo_blue_dark)
    val cat5 = TransactionCategory(0,"Salary", android.R.color.holo_purple)

    val tr1 = Transaction(
        0,
        TransactionType.EXPENSE,
        "Food shopping",
        21.35f,
        2,
        "TACSKO",
        PaymentMethod.CARD,
        TransactionPlanned.PLANNED,
        TransactionFrequency.MONTHLY_SUM,
        0
    )

    val tr2 = Transaction(
        0,
        TransactionType.EXPENSE,
        "Some jeans",
        59.99f,
        3,
        "ClothTHingSHop",
        PaymentMethod.CARD,
        TransactionPlanned.NOT_PLANNED,
        TransactionFrequency.FORTNIGHTLY_ONCE,
        0
    )

    val tr3 = Transaction(
        0,
        TransactionType.INCOME,
        "Sold some stuff",
        35.0f,
        1,
        "Whoever bought it",
        PaymentMethod.CASH,
        TransactionPlanned.NOT_PLANNED,
        TransactionFrequency.ONE_TIME,
        0
    )

    val tr4 = Transaction(
        0,
        TransactionType.WITHDRAW,
        "Withdrawal",
        10.0f,
        1,
        "Whatever shop",
        PaymentMethod.CARD,
        TransactionPlanned.NOT_PLANNED,
        TransactionFrequency.FORTNIGHTLY_ONCE,
        0
    )

    val tr5 = Transaction(
        0,
        TransactionType.EXPENSE,
        "Random shopping",
        24.15f,
        1,
        "At a shop",
        PaymentMethod.CARD,
        TransactionPlanned.NOT_PLANNED,
        TransactionFrequency.MONTHLY_ONCE,
        0
    )

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
            databaseDao.insertTransaction(tr1)
            databaseDao.insertTransaction(tr2)
            databaseDao.insertTransaction(tr3)
            databaseDao.insertTransaction(tr4)
            databaseDao.insertTransaction(tr5)
        }
    }

    /**
     * Suspend function for inserting category
     */
    private suspend fun insertCategorySuspend(){
        return withContext(Dispatchers.IO){
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