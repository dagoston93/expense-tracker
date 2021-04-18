package com.diamont.expense.tracker.planFragment

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.Currency
import com.diamont.expense.tracker.util.KEY_PREF_CURRENCY_ID
import com.diamont.expense.tracker.util.PlanCalculator
import com.diamont.expense.tracker.util.database.Plan
import com.diamont.expense.tracker.util.database.TransactionCategory
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import com.diamont.expense.tracker.util.enums.TransactionFrequency
import com.diamont.expense.tracker.util.enums.TransactionType
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PlanFragmentViewModel (
    private val appContext: Application,
    private val databaseDao: TransactionDatabaseDao,
    private val sharedPreferences: SharedPreferences
) : AndroidViewModel(appContext) {
    /**
     * Set up some live data
     */
    private val _categories = MutableLiveData<List<TransactionCategory>>()
    val categories : LiveData<List<TransactionCategory>>
        get() = _categories

    private val _plansToDisplay = MutableLiveData<List<Plan>>()
    val plansToDisplay: LiveData<List<Plan>>
        get() = _plansToDisplay

    private val _cardViewTitle = MutableLiveData<String>(appContext.resources.getString(R.string.total_planned_expenses))
    val cardViewTitle: LiveData<String>
        get() = _cardViewTitle

    private val _totalMonthlyString = MutableLiveData<String>("")
    val totalMonthlyString: LiveData<String>
        get() = _totalMonthlyString

    private val _totalYearlyString = MutableLiveData<String>("")
    val totalYearlyString: LiveData<String>
        get() = _totalYearlyString


    private var _selectedPlanType = MutableLiveData<TransactionType>(TransactionType.PLAN_EXPENSE)
    val selectedPlanType: LiveData<TransactionType>
        get() = _selectedPlanType

    /**
     * Set up some variables
     */
    private var currencyInUse: Currency? = null
    private var _decimalFormat: DecimalFormat? = null
    val decimalFormat: DecimalFormat?
        get() = _decimalFormat

    private var incomePlans = mutableListOf<Plan>()
    private var expensePlans = mutableListOf<Plan>()
    private val planCalculator = PlanCalculator()

    /**
     * Trigger this event when user clicks on an edit icon
     * by setting the plan id as the value.
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
        getCurrencyInUse()
        getPlanData()
    }

    /**
     * Call this method when plan data from database received
     */
    private fun onPlanDataReceived(){
        /**
         * Sort the lists
         */
        val comparator = kotlin.Comparator{ plan1: Plan, plan2: Plan ->
            plan2.compareTo(plan1)
        }

        expensePlans.sortWith(comparator)
        incomePlans.sortWith(comparator)

        /** Display expense plans */
        _plansToDisplay.value = expensePlans
        planCalculator.setCurrentPlanList(expensePlans)
        displayMonthlyAndYearlyPlannedAmount()

        /** TEST */
//        calendarStart.set(Calendar.YEAR, 2008)
//        calendarStart.set(Calendar.MONTH, Calendar.MARCH)
//        calendarStart.set(Calendar.DAY_OF_MONTH, 6)
//
//        calendarEnd.set(Calendar.YEAR, 2038)
//        calendarEnd.set(Calendar.MONTH, Calendar.APRIL)
//        calendarEnd.set(Calendar.DAY_OF_MONTH, 30)
//
//        calculateTotalPlannedAmountWithinPeriod(expensePlans, calendarStart, calendarEnd)
    }

    /**
     * This method displays the monthly and yearly plan amounts
     */
    private fun displayMonthlyAndYearlyPlannedAmount() {
        _totalMonthlyString.value = decimalFormat?.format(planCalculator.getCurrentMonthTotalPlanAmount())
        _totalYearlyString.value = decimalFormat?.format(planCalculator.getCurrentYearTotalPlanAmount())
    }

    /**
     * This method retrieves plan data from database
     */
    private fun getPlanData(){
        uiScope.launch {
            _categories.value = databaseDao.getCategoriesSuspend()
            expensePlans = databaseDao.getExpensePlansSuspend().toMutableList()
            incomePlans = databaseDao.getIncomePlansSuspend().toMutableList()

            onPlanDataReceived()
        }
    }

    /**
     * Call this method to find the currency in use
     */
    private fun getCurrencyInUse() {
        val currencyId = sharedPreferences.getInt(KEY_PREF_CURRENCY_ID, 0)
        currencyInUse = Currency.availableCurrencies[currencyId]
        _decimalFormat = Currency.getDecimalFormat(currencyId)
    }

    /**
     * Call this method if tabs are changed
     *
     * @param tabPosition 0 if Expense tab selected,
     *                    1 if Income tab selected
     */
    fun onSelectedTabChanged(tabPosition: Int){
        if(tabPosition == 0){
            /**
             * Expense tab selected
             */
            _cardViewTitle.value = appContext.resources.getString(R.string.total_planned_expenses)
            _plansToDisplay.value = expensePlans
            _selectedPlanType.value = TransactionType.PLAN_EXPENSE
            planCalculator.setCurrentPlanList(expensePlans)
            displayMonthlyAndYearlyPlannedAmount()
        }else{
            /**
             * Income tab selected
             */
            _cardViewTitle.value = appContext.resources.getString(R.string.total_planned_incomes)
            _plansToDisplay.value = incomePlans
            _selectedPlanType.value = TransactionType.PLAN_INCOME
            planCalculator.setCurrentPlanList(incomePlans)
            displayMonthlyAndYearlyPlannedAmount()
        }
    }

    /**
     * Call this method when user clicks the delete button of a transaction
     */
    fun deletePlan(planId: Int){
        uiScope.launch {
            databaseDao.deletePlanSuspend(planId)
        }
    }

    /**
     * Call this method when user clicks the cancel button of a transaction
     */
    fun cancelPlan(planId: Int, position: Int){
        /** First cancel in database */
        uiScope.launch {
            databaseDao.cancelPlanSuspend(planId)
        }

        /** Create a comparator to sort the list with */
        val comparator = kotlin.Comparator{ plan1: Plan, plan2: Plan ->
            plan2.compareTo(plan1)
        }

        /** Then cancel in our list and reorganize it */
        if(_selectedPlanType.value == TransactionType.PLAN_EXPENSE){
            expensePlans[position].cancellationDate = MaterialDatePicker.todayInUtcMilliseconds()
            expensePlans[position].isStatusActive = false
            expensePlans.sortWith(comparator)

            /** Refresh the plans we are displaying */
            _plansToDisplay.value = expensePlans
        }else{
            incomePlans[position].cancellationDate = MaterialDatePicker.todayInUtcMilliseconds()
            incomePlans[position].isStatusActive = false
            incomePlans.sortWith(comparator)

            /** Refresh the plans we are displaying */
            _plansToDisplay.value = incomePlans
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
