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
            _categories.value = databaseDao.getCategoriesSuspend()
            _transactionData.value = databaseDao.getAllTransactionsExcludePlansSuspend()
        }
    }

    /**
     * Call this method when user clicks the delete button of a transaction
     */
    fun deleteTransaction(transactionId: Int){
        uiScope.launch {
            databaseDao.deleteTransactionSuspend(transactionId)
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