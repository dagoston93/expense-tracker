package com.diamont.expense.tracker.historyFragment

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diamont.expense.tracker.util.DateRangeSelectorFragmentViewModel
import com.diamont.expense.tracker.util.database.Plan
import com.diamont.expense.tracker.util.database.Transaction
import com.diamont.expense.tracker.util.database.TransactionCategory
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import com.diamont.expense.tracker.util.enums.TransactionType
import kotlinx.coroutines.*

class HistoryFragmentViewModel(
    private val appContext: Application,
    private val databaseDao: TransactionDatabaseDao
) : DateRangeSelectorFragmentViewModel(appContext) {

    /**
     * Set up some live data
     */
    private val _transactionDataToDisplay = MutableLiveData<List<Transaction>>()
    val transactionDataToDisplay : LiveData<List<Transaction>>
        get() = _transactionDataToDisplay

    private val _categories = MutableLiveData<List<TransactionCategory>>()
    val categories : LiveData<List<TransactionCategory>>
        get() = _categories

    private val _plans = MutableLiveData<List<Plan>>()
    val plans : LiveData<List<Plan>>
        get() = _plans

    /**
     * Declare some variables
     */
    private var transactionData : List<Transaction> = listOf<Transaction>()
    private var filteredTransactionData : List<Transaction> = mutableListOf<Transaction>()

    /**
     * The items in the filter list are going to be shown.
     */
    private var _filterTransactionTypes: MutableList<TransactionType> = mutableListOf(
        TransactionType.EXPENSE,
        TransactionType.INCOME,
        TransactionType.WITHDRAW,
        TransactionType.DEPOSIT
    )
    val  filterTransactionTypes: MutableList<TransactionType>
        get() = _filterTransactionTypes

    private var _filterCategoryIds: MutableList<Int> = mutableListOf()
    val filterCategoryIds: MutableList<Int>
        get() = _filterCategoryIds
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
     * Call this method if user selects filter
     */
    fun onFiltersSelected(
        transactionTypes: MutableList<TransactionType>,
        categoryIds: MutableList<Int>
    ){
        _filterTransactionTypes = transactionTypes
        _filterCategoryIds = categoryIds

        filterItems()
    }

    /**
     * Call this method to filter the list
     */
    override fun filterItems(){
        Log.d("GUS", "filtering...")
        filteredTransactionData = transactionData.filter{
            var isItemDisplayed = true

            /** Check if item is within date range */
            if(selectedPeriodIndex != IDX_WHOLE_PERIOD) {
                if (it.date !in calendarStartDate.timeInMillis..calendarEndDate.timeInMillis) {
                    isItemDisplayed = false
                }
            }

            /** Check the category filters */
            if(!_filterCategoryIds.contains(it.categoryId)){
                isItemDisplayed = false
            }

            /** Check transaction type filters */
            if(!_filterTransactionTypes.contains(it.transactionType)){
                isItemDisplayed = false
            }

            isItemDisplayed
        }

        _transactionDataToDisplay.value = filteredTransactionData
    }

    /**
     * Call this method when data from database received
     */
    private fun onDataReceived(){
        _transactionDataToDisplay.value = transactionData

        for(category in _categories.value!!){
            _filterCategoryIds.add(category.categoryId)
        }
    }

    /**
     * This method retrieves transaction data
     */
    private fun getTransactionData(){
        uiScope.launch {
            _categories.value = databaseDao.getCategoriesSuspend()
            _plans.value = databaseDao.getAllPlansSuspend()
            transactionData = databaseDao.getAllTransactionsSuspend()
            onDataReceived()
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