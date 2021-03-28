package com.diamont.expense.tracker.addOrEditTransactionFragment

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.boolToVisibility
import com.diamont.expense.tracker.util.database.Plan
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

    private var allPlans = listOf<Plan>()
    private var incomePlans = mutableListOf<String>(appContext.resources.getString(R.string.not_planned))
    private var expensePlans = mutableListOf<String>(appContext.resources.getString(R.string.not_planned))

    /**
     * Set up some live data
     */
    private val _titleString = MutableLiveData<String>(appContext.resources.getString(R.string.add_expense))
    val titleString : LiveData<String>
        get() = _titleString

   private val _descriptionString = MutableLiveData<String>("")
    val descriptionString : LiveData<String>
        get() = _descriptionString

    private val _dateString = MutableLiveData<String>("")
    val dateString : LiveData<String>
        get() = _dateString

    private val _categories = MutableLiveData<List<TransactionCategory>>(listOf<TransactionCategory>())
    val categories : LiveData<List<TransactionCategory>>
        get() = _categories

    private val _currentPlanList = MutableLiveData<List<String>>(listOf<String>())
    val currentPlanList : LiveData<List<String>>
        get() = _currentPlanList

    private val _venues = MutableLiveData<List<String>>(listOf<String>())
    val venues : LiveData<List<String>>
        get() = _venues

    private val _isIsPlannedFieldVisible = MutableLiveData<Boolean>(true)
    val isIsPlannedFieldVisible = Transformations.map(_isIsPlannedFieldVisible){
        boolToVisibility(it)
    }

    private val _isCategoryFieldVisible = MutableLiveData<Boolean>(true)
    val isCategoryFieldVisible = Transformations.map(_isCategoryFieldVisible){
        boolToVisibility(it)
    }

    private val _isRecipientOrVenueFieldVisible = MutableLiveData<Boolean>(true)
    val isRecipientOrVenueFieldVisible = Transformations.map(_isRecipientOrVenueFieldVisible){
        boolToVisibility(it)
    }

    private val _isPaymentMethodFieldVisible = MutableLiveData<Boolean>(true)
    val isPaymentMethodFieldVisible = Transformations.map(_isPaymentMethodFieldVisible){
        boolToVisibility(it)
    }

    private val _isFrequencyFieldVisible = MutableLiveData<Boolean>(false)
    val isFrequencyFieldVisible = Transformations.map(_isFrequencyFieldVisible){
        boolToVisibility(it)
    }

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
        getDataFromDatabase()
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
            /** Set up the title */
            _titleString.value = appContext.resources.getString(R.string.add_income)

            /** Set the field visibilities */
            _isIsPlannedFieldVisible.value = true
            _isCategoryFieldVisible.value = true
            _isRecipientOrVenueFieldVisible.value = true
            _isPaymentMethodFieldVisible.value = true
            _isFrequencyFieldVisible.value = false

            /** Reset description */
            if(_descriptionString.value != ""){
                _descriptionString.value = ""
            }

            /** Set up the plan list */
            _currentPlanList.value = incomePlans

        }else if(newTransactionType == TransactionType.EXPENSE){
            /** Set up the title */
            _titleString.value = appContext.resources.getString(R.string.add_expense)

            /** Set the field visibilities */
            _isIsPlannedFieldVisible.value = true
            _isCategoryFieldVisible.value = true
            _isRecipientOrVenueFieldVisible.value = true
            _isPaymentMethodFieldVisible.value = true
            _isFrequencyFieldVisible.value = false

            /** Reset description */
            if(_descriptionString.value != ""){
                _descriptionString.value = ""
            }

            /** Set up the plan list */
            _currentPlanList.value = expensePlans

        }else if(newTransactionType == TransactionType.DEPOSIT){
            /** Set up the title */
            _titleString.value = appContext.resources.getString(R.string.add_deposit)

            /** Set the field visibilities */
            _isIsPlannedFieldVisible.value = false
            _isCategoryFieldVisible.value = false
            _isRecipientOrVenueFieldVisible.value = false
            _isPaymentMethodFieldVisible.value = false
            _isFrequencyFieldVisible.value = false

            /** Set description */
            _descriptionString.value = appContext.resources.getString(R.string.deposit)

        }else if(newTransactionType == TransactionType.WITHDRAW){
            /** Set up the title */
            _titleString.value = appContext.resources.getString(R.string.add_withdrawal)

            /** Set the field visibilities */
            _isIsPlannedFieldVisible.value = false
            _isCategoryFieldVisible.value = false
            _isRecipientOrVenueFieldVisible.value = false
            _isPaymentMethodFieldVisible.value = false
            _isFrequencyFieldVisible.value = false

            /** Set description */
            _descriptionString.value = appContext.resources.getString(R.string.withdraw)
        }else if(newTransactionType == TransactionType.PLAN_EXPENSE){
            /** Set up the title */
            _titleString.value = appContext.resources.getString(R.string.add_plan_expense)

            /** Set the field visibilities */
            _isIsPlannedFieldVisible.value = false
            _isCategoryFieldVisible.value = true
            _isRecipientOrVenueFieldVisible.value = true
            _isPaymentMethodFieldVisible.value = true
            _isFrequencyFieldVisible.value = true

            /** Reset description */
            if(_descriptionString.value != ""){
                _descriptionString.value = ""
            }

        }else if(newTransactionType == TransactionType.PLAN_INCOME){
            /** Set up the title */
            _titleString.value = appContext.resources.getString(R.string.add_plan_income)

            /** Set the field visibilities */
            _isIsPlannedFieldVisible.value = false
            _isCategoryFieldVisible.value = true
            _isRecipientOrVenueFieldVisible.value = true
            _isPaymentMethodFieldVisible.value = true
            _isFrequencyFieldVisible.value = true

            /** Reset description */
            if(_descriptionString.value != ""){
                _descriptionString.value = ""
            }

        }
    }

    /**
     * This method sorts the plans to expense or income plans
     */
    private fun sortPlans(){
        for(plan in allPlans){
            if(plan.transactionType == TransactionType.EXPENSE){
                expensePlans.add(plan.description)
            }else if(plan.transactionType == TransactionType.INCOME){
                incomePlans.add(plan.description)
            }
        }
    }

    /**
     * This method retrieves the categories from the database
     */
    private fun getDataFromDatabase(){
        uiScope.launch {
            _categories.value = getCategoriesSuspend()
            _venues.value = getVenuesSuspend()
            allPlans = getPlans()
            sortPlans()
            _currentPlanList.value = expensePlans
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

    /**
     * Suspend function to get plan data
     */
    private suspend fun getPlans() : List<Plan>{
        return withContext(Dispatchers.IO){
            val data : List<Plan> = databaseDao.getAllPlans()
            data
        }
    }

}