package com.diamont.expense.tracker.historyFragment

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.database.Plan
import com.diamont.expense.tracker.util.database.Transaction
import com.diamont.expense.tracker.util.database.TransactionCategory
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import com.diamont.expense.tracker.util.enums.TransactionType
import kotlinx.coroutines.*
import java.util.*

class HistoryFragmentViewModel(
    private val appContext: Application,
    private val databaseDao: TransactionDatabaseDao) : AndroidViewModel(appContext) {

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

    private val _periodStringList = MutableLiveData<List<String>>(listOf<String>())
    val periodStringList : LiveData<List<String>>
        get() = _periodStringList

    /**
     * Declare some variables
     */
    private var transactionData : List<Transaction> = listOf<Transaction>()
    private var filteredTransactionData : List<Transaction> = mutableListOf<Transaction>()
    private var selectedIndex: Int? = 0
    private var calendarStartDate = Calendar.getInstance()
    private var calendarEndDate = Calendar.getInstance()

    private var filterTransactionTypes: MutableList<TransactionType> = mutableListOf()
    private var filterCategoryIds: MutableList<Int> = mutableListOf()

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

        /** Set up the array adapter */
        _periodStringList.value = listOf(
            appContext.resources.getString(R.string.all_transactions),
            appContext.resources.getString(R.string.current_month),
            appContext.resources.getString(R.string.last_seven_days),
            appContext.resources.getString(R.string.previous_month),
            appContext.resources.getString(R.string.this_year),
            appContext.resources.getString(R.string.select_period)
        )
    }

    companion object{
        const val IDX_ALL_TRANSACTIONS: Int = 0
        const val IDX_CURRENT_MONTH: Int = 1
        const val IDX_LAST_SEVEN_DAYS: Int = 2
        const val IDX_PREVIOUS_MONTH: Int = 3
        const val IDX_THIS_YEAR: Int = 4
        const val IDX_DATE_RANGE: Int = 5
    }

    /**
     * Call this method if user selects an option from the dropdown menu
     */
    fun onPeriodDropdownItemSelected(index: Int?){
        Log.d("GUS", "item: $index")

        if(index != null){
            selectedIndex = index
            filterTransactionList()
        }else{
            selectedIndex = IDX_DATE_RANGE
        }
    }

    /**
     * Call this method if user selects a date range
     */
    fun onDateRangeSelected(startDate: Long?, endDate: Long?){
        Log.d("GUS", "date range picked: $startDate, $endDate")

        if(startDate != null && endDate != null) {
            calendarStartDate.timeInMillis = startDate
            calendarEndDate.timeInMillis = endDate
        }

        selectedIndex = IDX_DATE_RANGE
        filterTransactionList()
    }

    /**
     * Call this method to filter the list
     */
    private fun filterTransactionList(){
        Log.d("GUS", "filtering...")
        filteredTransactionData = transactionData.filter{
            var isItemDisplayed = true

            /** Check if item is within date range */
            if(selectedIndex != IDX_ALL_TRANSACTIONS) {
                if (it.date !in calendarStartDate.timeInMillis..calendarEndDate.timeInMillis) {
                    isItemDisplayed = false
                }
            }

            /** Check the category filters */
            if(filterCategoryIds.isNotEmpty()){
                if(!filterCategoryIds.contains(it.categoryId)){
                    isItemDisplayed = false
                }
            }

            /** Check transaction type filters */
            if(filterTransactionTypes.isNotEmpty()){
                if(!filterTransactionTypes.contains(it.transactionType)){
                    isItemDisplayed = false
                }
            }

            isItemDisplayed
        }

        _transactionDataToDisplay.value = filteredTransactionData
    }

    /**
     * This method retrieves transaction data
     */
    private fun getTransactionData(){
        uiScope.launch {
            _categories.value = databaseDao.getCategoriesSuspend()
            _plans.value = databaseDao.getAllPlansSuspend()
            transactionData = databaseDao.getAllTransactionsSuspend()
            _transactionDataToDisplay.value = transactionData
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