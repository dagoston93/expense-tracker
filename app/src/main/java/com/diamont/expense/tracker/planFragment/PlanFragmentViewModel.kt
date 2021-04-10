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
        organizePlans(expensePlans)

        /*for(plan in expensePlans){

        }*/

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
        val monthlyTotal = calculateTotalAmountWithinInterval(
            expensePlans,
            calendarStart,
            calendarEnd
        )
        _totalMonthlyString.value = decimalFormat?.format(monthlyTotal)
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
    private fun calculateTotalAmountWithinInterval(planList: List<Plan>, startDate: Calendar, endDate: Calendar): Float{
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
    private fun organizePlans(planList: MutableList<Plan>){

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
                calendarPlan.timeInMillis = plan.firstExpectedDate

               if(calendarNow.get(Calendar.DAY_OF_YEAR) > calendarPlan.get(Calendar.DAY_OF_YEAR)){
                   calendarPlan.set(Calendar.YEAR, calendarNow.get(Calendar.YEAR) + 1)
               }else{
                   calendarPlan.set(Calendar.YEAR, calendarNow.get(Calendar.YEAR))
               }

               plan.nextExpectedDate = calendarPlan.timeInMillis
           }else if(plan.frequency == TransactionFrequency.MONTHLY_SUM || plan.frequency == TransactionFrequency.MONTHLY_ONCE){
               /**
                * Monthly plans
                */
               calendarPlan.timeInMillis = calendarNow.timeInMillis
               val lastDayOfThisMonth = calendarNow.getActualMaximum(Calendar.DAY_OF_MONTH)

               calendarPlan.add(Calendar.MONTH, 1)
               val lastDayOfNextMonth = calendarNow.getActualMaximum(Calendar.DAY_OF_MONTH)

               calendarPlan.timeInMillis = plan.firstExpectedDate
               calendarPlan.set(Calendar.YEAR, calendarNow.get(Calendar.YEAR))
               val planDay = calendarPlan.get(Calendar.DAY_OF_MONTH)

               /** Check if today is past the day of month of plan */
               if(calendarNow.get(Calendar.DAY_OF_MONTH) > calendarPlan.get(Calendar.DAY_OF_MONTH)){
                   /**
                    * Next expected in next month
                    * */
                   calendarPlan.set(Calendar.MONTH, calendarNow.get(Calendar.MONTH))
                   calendarPlan.add(Calendar.MONTH, 1)

                   /**
                    * Check if the required day is past the last day of the month.
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
                   calendarPlan.set(Calendar.MONTH, calendarNow.get(Calendar.MONTH))

                   /**
                    * Check if the required day is past the last day of the month.
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

               plan.nextExpectedDate = calendarPlan.timeInMillis
           }
        }
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
        }else{
            /**
             * Income tab selected
             */
            _cardViewTitle.value = appContext.resources.getString(R.string.total_planned_incomes)
            _plansToDisplay.value = incomePlans
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
     * onCleared() is called when view model is destroyed
     * in this case we need to cancel coroutines
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}