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



    private val calendarStart = Calendar.getInstance()
    private val calendarEnd = Calendar.getInstance()

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

        /** Test some stuff with date */
        val date1 = Date(MaterialDatePicker.todayInUtcMilliseconds())
        date1.hours = 23
        date1.minutes = 59
        date1.seconds = 59
        date1.year = 121
        date1.month = 4
        date1.date = 29

        val date2 = Date()
        date2.hours = 0
        date2.minutes = 0
        date2.seconds = 0
        date2.year = 121
        date2.month = 2
        date2.date = 3

        val c1 = Calendar.getInstance()
        c1.timeInMillis = date1.time

        val c2 = Calendar.getInstance()
        c2.timeInMillis = date2.time


        //Log.d("GUS", "${c1.get(Calendar.YEAR)} ${c1.get(Calendar.MONTH)} ${c1.get(Calendar.DAY_OF_MONTH)} ")
        //Log.d("GUS", "$date1")
        //Log.d("GUS", "DoW: ${c1.get(Calendar.DAY_OF_WEEK)}")

        //Log.d("GUS", "Method: ${getDayOfWeekCount(4, c2, c1 )}")
    }



    /**
     * Call this method when plan data from database received
     */
    private fun onPlanDataReceived(){
        /**
         * Calculate next expected dates for plans
         */
        calculateNextExpectedDates(expensePlans)

        val comparator = kotlin.Comparator{ plan1: Plan, plan2: Plan ->
            plan2.compareTo(plan1)
        }

        expensePlans.sortWith(comparator)


        /** Display expense plans */
        _plansToDisplay.value = expensePlans

        /**
         * Find first and last day of month
         */
        calendarStart.set(Calendar.DAY_OF_MONTH, 1)
        calendarStart.set(Calendar.HOUR, 0)
        calendarStart.set(Calendar.MINUTE, 0)
        calendarStart.set(Calendar.SECOND, 0)

        calendarEnd.set(Calendar.DAY_OF_MONTH, calendarEnd.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendarEnd.set(Calendar.HOUR, 23)
        calendarEnd.set(Calendar.MINUTE, 59)
        calendarEnd.set(Calendar.SECOND, 59)

        //Log.d("GUS", "$calendarStart")
        //Log.d("GUS", "$calendarEnd")

        /**
         * Display monthly expenses
         */
        val monthlyTotal = calculatePlannedAmountWithinInterval(
            expensePlans,
            calendarStart,
            calendarEnd
        )
        _totalMonthlyString.value = decimalFormat?.format(monthlyTotal)

        /** TEST */
        calendarStart.set(Calendar.YEAR, 2021)
        calendarStart.set(Calendar.MONTH, Calendar.MARCH)
        calendarStart.set(Calendar.DAY_OF_MONTH, 3)

        calendarEnd.set(Calendar.YEAR, 2021)
        calendarEnd.set(Calendar.MONTH, Calendar.APRIL)
        calendarEnd.set(Calendar.DAY_OF_MONTH, 19)

        val x = calculateNextFortnightlyPlanDate(calendarEnd, calendarStart.timeInMillis)
        //Log.d("GUS", "Plan: ${Date(calendarStart.timeInMillis)}")
        //Log.d("GUS", "Now: ${Date(calendarEnd.timeInMillis)}")
        //Log.d("GUS", "Next: ${Date(x)}")

        for(i in expensePlans){
            Log.d("GUS", "${i.description}  cancelled: ${Date(i.cancellationDate)}")
        }

    }

    /**
     * This method counts how many 'day of week' is between the
     * given dates.
     *
     * @param dayOfWeek The day of week given by Calendar.get(DAY_OF_WEEK) (1: Sun - 7: Sat)
     * @param date1 The first date.
     * @param date2 The second date.
     */
    private fun getDayOfWeekCount(dayOfWeek: Int, date1: Calendar, date2: Calendar): Int{
        val difference = date2.timeInMillis - date1.timeInMillis
        val days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS).toInt()
        val weeks = days / 7
        val remainingDays = days % 7

        var sinceLast = date2.get(Calendar.DAY_OF_WEEK) - dayOfWeek
        if(sinceLast < 0)
        {
            sinceLast += 7
        }

        return if(remainingDays >= sinceLast) {
            weeks + 1
         }else{
            weeks
        }
    }

    /**
     * Call this method to calculate total expenses/incomes within an interval
     */
    private fun calculatePlannedAmountWithinInterval(planList: List<Plan>, startDate: Calendar, endDate: Calendar): Float{
        var total: Float = 0f
        val calendar = Calendar.getInstance()

        /**
         * Iterate through the list
         */
        for(plan in planList){

        }

        return total
    }

    /**
     * This method calculates the next expected dates of a plan list
     * and organizes the list by them
     */
    private fun calculateNextExpectedDates(planList: MutableList<Plan>){

        val calendarNow = Calendar.getInstance()
        val calendarPlan = Calendar.getInstance()

        for(plan in planList){
           if(plan.frequency == TransactionFrequency.ONE_TIME){
               /**
                * One time plans
                */
               plan.nextExpectedDate = plan.firstExpectedDate
           }else if(plan.frequency == TransactionFrequency.YEARLY_ONCE || plan.frequency == TransactionFrequency.YEARLY_SUM){
               /**
                * Yearly plans
                */
               plan.nextExpectedDate = calculateNextYearlyPlanDate(calendarNow, plan.firstExpectedDate)
           }else if(plan.frequency == TransactionFrequency.MONTHLY_SUM || plan.frequency == TransactionFrequency.MONTHLY_ONCE){
               /**
                * Monthly plans
                */
               plan.nextExpectedDate = calculateNextMonthlyPlanDate(calendarNow, plan.firstExpectedDate)
           }else if(plan.frequency == TransactionFrequency.FORTNIGHTLY_SUM || plan.frequency == TransactionFrequency.FORTNIGHTLY_ONCE){
               /**
                * Fortnightly plans
                */
               plan.nextExpectedDate = calculateNextFortnightlyPlanDate(calendarNow, plan.firstExpectedDate)
           }else if(plan.frequency == TransactionFrequency.WEEKLY_SUM || plan.frequency == TransactionFrequency.WEEKLY_ONCE){
               /**
                * Weekly plans
                */
               plan.nextExpectedDate = calculateNextWeeklyPlanDate(calendarNow, plan.firstExpectedDate)
           }
        }
    }

    /**
     * This method returns whether a year is a leap year
     */
    private fun isLeapYear(year: Int): Boolean{
        var isLeapYear: Boolean = false

        /** Check if year is divisible by 4 */
        if(year % 4 == 0){
            /** If it is a century, only every 4th is leap year*/
            if(year % 100 == 0){
                isLeapYear = (year % 400 == 0)
            }else{
                isLeapYear = true
            }
        }

        return isLeapYear
    }

    /**
     * This method calculates the next date of a yearly plan
     */
    private fun calculateNextYearlyPlanDate(calendarNow: Calendar, firstPlanDate: Long): Long{
        val calendarPlan = Calendar.getInstance()
        calendarPlan.timeInMillis = firstPlanDate

        /** If original plan was 29th of Feb and this is not a leap year we need to set 28th of Feb */
        if(calendarPlan.get(Calendar.MONTH) == Calendar.FEBRUARY && calendarPlan.get(Calendar.DAY_OF_MONTH) == 29){
            if(!isLeapYear(calendarNow.get(Calendar.YEAR))){
                calendarPlan.set(Calendar.DAY_OF_MONTH, 28)
            }
        }

        /** If current date passed plan date, we set next year otherwise we set this year */
        if(calendarNow.get(Calendar.DAY_OF_YEAR) > calendarPlan.get(Calendar.DAY_OF_YEAR)){
            calendarPlan.set(Calendar.YEAR, calendarNow.get(Calendar.YEAR) + 1)
        }else{
            calendarPlan.set(Calendar.YEAR, calendarNow.get(Calendar.YEAR))
        }

        return calendarPlan.timeInMillis
    }

    /**
     * This method calculates the next date of a monthly plan
     */
    private fun calculateNextMonthlyPlanDate(calendarNow: Calendar, firstPlanDate: Long): Long{
        val calendarPlan = Calendar.getInstance()

        /** Save the last day of the current and next months */
        calendarPlan.timeInMillis = calendarNow.timeInMillis
        val lastDayOfThisMonth = calendarNow.getActualMaximum(Calendar.DAY_OF_MONTH)

        calendarPlan.add(Calendar.MONTH, 1)
        val lastDayOfNextMonth = calendarNow.getActualMaximum(Calendar.DAY_OF_MONTH)

        /** Set first expected date and save the planned day */
        calendarPlan.timeInMillis = firstPlanDate
        val planDay = calendarPlan.get(Calendar.DAY_OF_MONTH)

        calendarPlan.set(Calendar.YEAR, calendarNow.get(Calendar.YEAR))
        calendarPlan.set(Calendar.MONTH, calendarNow.get(Calendar.MONTH))

        /** Check if today is past the day of month of plan */
        if(calendarNow.get(Calendar.DAY_OF_MONTH) > planDay){
            /**
             * Next expected in next month.
             */
            calendarPlan.add(Calendar.MONTH, 1)

            /**
             * If the required day is past the last day of the month,
             * we use the last day of month, otherwise the planned day
             */
            calendarPlan.set(
                Calendar.DAY_OF_MONTH,

                if(planDay > lastDayOfThisMonth){
                    lastDayOfNextMonth
                }else{
                    planDay
                }
            )
        }else{
            /**
             * Next expected in this month.
             *
             * If the required day is past the last day of the month,
             * we use the last day of month, otherwise the planned day
             */
            calendarPlan.set(
                Calendar.DAY_OF_MONTH,

                if(planDay > lastDayOfThisMonth){
                    lastDayOfThisMonth
                }else{
                    planDay
                }
            )
        }

        return calendarPlan.timeInMillis
    }

    /**
     * Calculate next expected fortnightly plan date
     */
    private fun calculateNextFortnightlyPlanDate(calendarNow: Calendar, firstPlanDate: Long): Long {
        val difference = calendarNow.timeInMillis - firstPlanDate
        val days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS).toInt()

        val remainingDays = days % 14
        var daysTillNext = 14 - remainingDays

        /** If 2 weeks left we take the closest date which is today */
        if(daysTillNext == 14){
            daysTillNext = 0
        }

        val calendarPlan = Calendar.getInstance()
        calendarPlan.timeInMillis = calendarNow.timeInMillis
        calendarPlan.add(Calendar.DAY_OF_YEAR, daysTillNext)

        return calendarPlan.timeInMillis
    }

    /**
     * Calculate next weekly plan date
     */
    private fun calculateNextWeeklyPlanDate(calendarNow: Calendar, firstPlanDate: Long): Long {
        val calendarPlan = Calendar.getInstance()
        calendarPlan.timeInMillis = firstPlanDate

        val planDayOfWeek = calendarPlan.get(Calendar.DAY_OF_WEEK)
        val nowDayOfWeek = calendarNow.get(Calendar.DAY_OF_WEEK)
        var sinceLastDay = nowDayOfWeek - planDayOfWeek

        if(sinceLastDay < 0) {
            sinceLastDay += 7
        }

        calendarPlan.timeInMillis = calendarNow.timeInMillis
        calendarPlan.add(Calendar.DAY_OF_YEAR, 7 - sinceLastDay)

        return calendarPlan.timeInMillis
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
    fun selectedTabChanged(tabPosition: Int){
        if(tabPosition == 0){
            /**
             * Expense tab selected
             */
            _cardViewTitle.value = appContext.resources.getString(R.string.total_planned_expenses)
            _plansToDisplay.value = expensePlans
            _selectedPlanType.value = TransactionType.PLAN_EXPENSE
        }else{
            /**
             * Income tab selected
             */
            _cardViewTitle.value = appContext.resources.getString(R.string.total_planned_incomes)
            _plansToDisplay.value = incomePlans
            _selectedPlanType.value = TransactionType.PLAN_INCOME
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