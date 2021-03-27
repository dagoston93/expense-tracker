package com.diamont.expense.tracker.addOrEditTransactionFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.database.TransactionCategory
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import com.diamont.expense.tracker.util.enums.TransactionType
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.*
import java.util.*

class AddOrEditTransactionFragmentViewModel(
    private val appContext: Application,
    private val databaseDao: TransactionDatabaseDao
) : AndroidViewModel(appContext) {
    /**
     * Declare required variables
     */
    private var date : Date = Date(MaterialDatePicker.todayInUtcMilliseconds())
    private val dateFormat = android.text.format.DateFormat.getDateFormat(appContext)

    /**
     * Set up some live data
     */
    private val _titleString = MutableLiveData<String>(appContext.resources.getString(R.string.add_expense))
    val titleString : LiveData<String>
        get() = _titleString

    private val _dateString = MutableLiveData<String>("")
    val dateString : LiveData<String>
        get() = _dateString

    private val _categories = MutableLiveData<List<TransactionCategory>>(listOf<TransactionCategory>())
    val categories : LiveData<List<TransactionCategory>>
        get() = _categories

    private val _venues = MutableLiveData<List<String>>(listOf<String>())
    val venues : LiveData<List<String>>
        get() = _venues

    /**
     * Set up coroutine job and the scope
     */
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Constructor
     */
    init{
        _dateString.value = dateFormat.format(date)
        getCategoriesAndVenues()
    }

    /**
     * Call this method when the selected date changes
     */
    fun onSelectedDateChanged(newDate: Long){
        date.time=newDate
        _dateString.value = dateFormat.format(date)
    }

    /**
     * Call this method whenever the selected
     * transaction type changes
     *
     * @param index: The index in the enum class
     * of the selected transaction type.
     * (Same as the index in the array adapter.)
     */
    fun onTransactionTypeChanged(index: Int?){
        if(index == null) return

        val newTransactionType = TransactionType.getEnumValueFromIndex(index)

        if(newTransactionType == TransactionType.INCOME)
        {
            _titleString.value = appContext.resources.getString(R.string.add_income)
        }else if(newTransactionType == TransactionType.EXPENSE){
            _titleString.value = appContext.resources.getString(R.string.add_expense)
        }else if(newTransactionType == TransactionType.DEPOSIT){
            _titleString.value = appContext.resources.getString(R.string.add_deposit)
        }else if(newTransactionType == TransactionType.WITHDRAW){
            _titleString.value = appContext.resources.getString(R.string.add_withdrawal)
        }
    }

    /**
     * This method retrieves the categories from the database
     */
    private fun getCategoriesAndVenues(){
        uiScope.launch {
            _categories.value = getCategoriesSuspend()
            _venues.value = getVenuesSuspend()
        }
    }

    /**
     * Suspend function to retrieve categories from database
     */
    private suspend fun getCategoriesSuspend() : List<TransactionCategory>{
        return withContext(Dispatchers.IO){
            val data : List<TransactionCategory> = databaseDao.getAllCategories()
            data
        }
    }

    /**
     * Suspend function to retrieve venues
     */
    private suspend fun getVenuesSuspend() : List<String> {
        return withContext(Dispatchers.IO){
            val data : List<String> = databaseDao.getAllVenues()
            data
        }
    }

}