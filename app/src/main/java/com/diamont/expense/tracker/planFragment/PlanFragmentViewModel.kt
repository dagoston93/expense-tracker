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
import com.diamont.expense.tracker.util.calendarToString
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

/** TODO add checks if period starts AFTER camcellation of plan! */

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
        calendarStart.set(Calendar.HOUR, 0)
        calendarStart.set(Calendar.MINUTE, 0)
        calendarStart.set(Calendar.SECOND, 0)
        calendarStart.set(Calendar.DAY_OF_MONTH, 1)

        calendarEnd.set(Calendar.HOUR, 23)
        calendarEnd.set(Calendar.MINUTE, 59)
        calendarEnd.set(Calendar.SECOND, 59)
        calendarEnd.set(Calendar.DAY_OF_MONTH, calendarEnd.getActualMaximum(Calendar.DAY_OF_MONTH))

        //Log.d("GUS", "$calendarStart")
        //Log.d("GUS", "$calendarEnd")

        /**
         * Display monthly expenses
         */
//        val monthlyTotal = calculateTotalPlannedAmountWithinPeriod(
//            expensePlans,
//            calendarStart,
//            calendarEnd
//        )
//        _totalMonthlyString.value = decimalFormat?.format(monthlyTotal)

        /**
         * Find first and last day of year
         */
        calendarStart.set(Calendar.HOUR, 0)
        calendarStart.set(Calendar.MINUTE, 0)
        calendarStart.set(Calendar.SECOND, 0)
        calendarStart.set(Calendar.DAY_OF_MONTH, 1)
        calendarStart.set(Calendar.MONTH, Calendar.JANUARY)

        //Log.d("GUS", "start of year: ${calendarToString(calendarStart)}")
        //Log.d("GUS", "dof y: ${calendarEnd.get(Calendar.DAY_OF_YEAR)}")
        //Log.d("GUS", "max dof y: ${calendarEnd.getActualMaximum(Calendar.DAY_OF_YEAR)}")

        calendarEnd.set(Calendar.DAY_OF_YEAR, calendarEnd.getActualMaximum(Calendar.DAY_OF_YEAR))
        //Log.d("GUS", "dof y after set: ${calendarEnd.get(Calendar.DAY_OF_YEAR)}")


        //calendarEnd.set(Calendar.HOUR, 23)
        //calendarEnd.set(Calendar.MINUTE, 59)
        //calendarEnd.set(Calendar.SECOND, 59)

        //Log.d("GUS", "eof year: ${calendarToString(calendarEnd)}")

        /**
         * Display yearly expenses
         */
//        val yearlyTotal = calculateTotalPlannedAmountWithinPeriod(
//            expensePlans,
//            calendarStart,
//            calendarEnd
//        )
//        _totalYearlyString.value = decimalFormat?.format(yearlyTotal)

        /** TEST */
        calendarStart.set(Calendar.YEAR, 2021)
        calendarStart.set(Calendar.MONTH, Calendar.MARCH)
        calendarStart.set(Calendar.DAY_OF_MONTH, 6)

        calendarEnd.set(Calendar.YEAR, 2021)
        calendarEnd.set(Calendar.MONTH, Calendar.APRIL)
        calendarEnd.set(Calendar.DAY_OF_MONTH, 30)

        calculateTotalPlannedAmountWithinPeriod(expensePlans, calendarStart, calendarEnd)

        val x = calculateNextFortnightlyPlanDate(calendarEnd, calendarStart.timeInMillis)
        //Log.d("GUS", "Plan: ${Date(calendarStart.timeInMillis)}")
        //Log.d("GUS", "Now: ${Date(calendarEnd.timeInMillis)}")
        //Log.d("GUS", "Next: ${Date(x)}")

        for(i in expensePlans){
            //Log.d("GUS", "${i.description}  cancelled: ${Date(i.cancellationDate)}")
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
     *
     * @param planList A list of plans (expense/income) to iterate.
     * @param startDate A calendar containing the start date of the period.
     * @param endDate A calendar containing the end date of the period.
     */
    private fun calculateTotalPlannedAmountWithinPeriod(planList: List<Plan>, startDate: Calendar, endDate: Calendar): Float{
        var total: Float = 0f
        var currentPlan = Plan()

        /**
         * Iterate through the list
         */
        for(plan in planList){
            currentPlan = plan.copy()

            when(currentPlan.frequency){
                /**
                 * One time
                 */
                TransactionFrequency.ONE_TIME -> {
//                    if(currentPlan.nextExpectedDate in startDate.timeInMillis..endDate.timeInMillis){
//                        total += plan.amount
//                    }
                }

                /** Weekly once */
                TransactionFrequency.WEEKLY_ONCE -> {
//                    total += calculatePlanAmountForPeriod(
//                        currentPlan,
//                        startDate,
//                        endDate
//                    ){ calendarNow: Calendar, firstPlanDate: Long ->
//                        calculateNextWeeklyPlanDate(calendarNow, firstPlanDate)
//                    }
                }

                /**
                 * Fortnightly once
                 */
                TransactionFrequency.FORTNIGHTLY_ONCE -> {
//                    total += calculatePlanAmountForPeriod(
//                        currentPlan,
//                        startDate,
//                        endDate
//                    ){ calendarNow: Calendar, firstPlanDate: Long ->
//                        calculateNextFortnightlyPlanDate(calendarNow, firstPlanDate)
//                    }
                }

                /**
                 * Monthly once
                 */
                TransactionFrequency.MONTHLY_ONCE -> {
//                    total += calculatePlanAmountForPeriod(
//                        currentPlan,
//                        startDate,
//                        endDate
//                    ){ calendarNow: Calendar, firstPlanDate: Long ->
//                        calculateNextMonthlyPlanDate(calendarNow, firstPlanDate)
//                    }
                }

                /**
                 * Yearly once
                 */
                TransactionFrequency.YEARLY_ONCE ->{
//                    total += calculatePlanAmountForPeriod(
//                        currentPlan,
//                        startDate,
//                        endDate
//                    ){ calendarNow: Calendar, firstPlanDate: Long ->
//                        calculateNextYearlyPlanDate(calendarNow, firstPlanDate)
//                    }
                }

                /**
                 * Weekly sum
                 */
                TransactionFrequency.WEEKLY_SUM -> {
//                    Log.d("GUS", "Weekly(sum) plan: ${plan.description}")
//                    Log.d("GUS", "Period examined: ${calendarToString(startDate)} to ${calendarToString(endDate)}")
//                    if(!(!plan.isStatusActive && plan.cancellationDate < startDate.timeInMillis)) {
//
//                        Log.d("GUS", "Not cancelled before start of period.")
//
//                        val calendar = Calendar.getInstance()
//                        val cancellationDate = Calendar.getInstance()
//
//                        /** Set initial plan date to the later of the first date of the plan or the first date of period */
//                        if (startDate.timeInMillis >= plan.firstExpectedDate) {
//                            Log.d("GUS", "Period start date is later than first date of plan.")
//                            calendar.timeInMillis = startDate.timeInMillis
//                        } else {
//                            Log.d("GUS", "First date of plan is later than period start date.")
//                            calendar.timeInMillis = plan.firstExpectedDate
//                        }
//
//                        Log.d("GUS", "Date set in calendar: ${calendarToString(calendar)}")
//
//                        cancellationDate.timeInMillis = plan.cancellationDate
//
//                        /** Calculate the last day of the required period */
//                        val lastDate = Calendar.getInstance()
//                        if(!plan.isStatusActive
//                            && plan.cancellationDate <= endDate.timeInMillis){
//                            Log.d("GUS", "plan cancelled before end of period")
//                            lastDate.timeInMillis = plan.cancellationDate
//                        }else{
//                            Log.d("GUS", "plan is NOT cancelled before end of period")
//                            lastDate.timeInMillis = endDate.timeInMillis
//                        }
//
//                        Log.d("GUS", "Last date to check: ${calendarToString(lastDate)}")
//
//                        /**
//                         * Calculate the amount between the first date to examine and the
//                         * first expected day of the plan within the period
//                         * */
//                        val nextDate = calculateNextWeeklyPlanDate(calendar, plan.firstExpectedDate)
//                        val c = Calendar.getInstance()
//                        c.timeInMillis=nextDate
//                        Log.d("GUS", "nextDate: ${calendarToString(c)}")
//
//
//                        /** Check if period is over before next date */
//                        if(nextDate >= lastDate.timeInMillis){
//                            val difference =  lastDate.timeInMillis - calendar.timeInMillis
//                            val days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS).toInt()
//
//                            Log.d("GUS", "This is a short period. Only $days days long.")
//
//                            val x = calculateSubPeriodAmount(
//                                calendar.timeInMillis,
//                                lastDate.timeInMillis,
//                                plan.amount,
//                                7)
//
//                            Log.d("GUS", "Added: $x")
//                            total += x
//
//                        }else{
//                            var difference =  nextDate - calendar.timeInMillis
//                            var days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS).toInt()
//
//                            Log.d("GUS", "$days left till first full week starts.")
//
//                            var x = calculateSubPeriodAmount(
//                                calendar.timeInMillis,
//                                nextDate,
//                                plan.amount,
//                                7)
//
//                            Log.d("GUS", "Added: $x")
//                            total += x
//
//                            calendar.add(Calendar.DAY_OF_YEAR, days)
//                            Log.d("GUS", "start full week calc from: ${calendarToString(calendar)}")
//
//                            difference = lastDate.timeInMillis - calendar.timeInMillis
//
//                            Log.d("GUS", "Diff: $difference")
//                            days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS).toInt()
//
//                            Log.d("GUS", "Days to calculate full weeks: $days")
//                            val fullWeeks = days / 7
//
//                            Log.d("GUS", "Full weeks: $fullWeeks")
//
//                            total += fullWeeks * plan.amount
//                            Log.d("GUS", "Added: ${fullWeeks * plan.amount}")
//
//                            val remainingDays = days - ( fullWeeks * 7)
//
//                            Log.d("GUS", "Remaining days: $remainingDays")
//
//                            x = (plan.amount / 7) * remainingDays
//
//                            total += x
//                            Log.d("GUS", "Added: $x")
//                        }
//
//                    }
                }

                /**
                 * Fortnightly sum
                 */
                TransactionFrequency.FORTNIGHTLY_SUM -> {
                    Log.d("GUS", "${plan.description} is forthnightly sum type")
                    Log.d("GUS", "")

                    Log.d("GUS", "Checking period between:")
                    Log.d("GUS", "${calendarToString(startDate)} and ${calendarToString(endDate)}")

                    if(!(!plan.isStatusActive && plan.cancellationDate < startDate.timeInMillis)) {
                        Log.d("GUS", "Not cancelled before start of period.")

                        val calendar = Calendar.getInstance()
                        val cancellationDate = Calendar.getInstance()

                        /** Set initial plan date to the later of the first date of the plan or the first date of period */
                        if (startDate.timeInMillis >= plan.firstExpectedDate) {
                            Log.d("GUS", "Period start date is later than first date of plan.")
                            calendar.timeInMillis = startDate.timeInMillis
                        } else {
                            Log.d("GUS", "First date of plan is later than period start date.")
                            calendar.timeInMillis = plan.firstExpectedDate
                        }

                        cancellationDate.timeInMillis = plan.cancellationDate

                        /** Calculate the last day of the required period */
                        val lastDate = Calendar.getInstance()
                        if (!plan.isStatusActive
                            && plan.cancellationDate <= endDate.timeInMillis
                        ) {
                            Log.d("GUS", "plan cancelled before end of period")
                            lastDate.timeInMillis = plan.cancellationDate
                        } else {
                            Log.d("GUS", "plan is NOT cancelled before end of period")
                            lastDate.timeInMillis = endDate.timeInMillis
                        }

                        Log.d("GUS", "Last date to check: ${calendarToString(lastDate)}")

                        var nextDate = calculateNextFortnightlyPlanDate(calendar, plan.firstExpectedDate)
                        val c = Calendar.getInstance()
                        c.timeInMillis = nextDate
                        Log.d("GUS", "NextDate: ${calendarToString(c)}")

                        /** Check if period is over before next date */
                        if(nextDate >= lastDate.timeInMillis){
                            val difference =  lastDate.timeInMillis - calendar.timeInMillis
                            val days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS).toInt()

                            Log.d("GUS", "This is a short period. Only $days days long.")

                            val x = calculateSubPeriodAmount(
                                calendar.timeInMillis,
                                lastDate.timeInMillis,
                                plan.amount,
                                14)

                            Log.d("GUS", "Added: $x")
                            total += x

                        }else{
                            var difference =  nextDate - calendar.timeInMillis
                            var days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS).toInt()

                            Log.d("GUS", "$days left till first full fortnight starts.")

                            var x = calculateSubPeriodAmount(
                                calendar.timeInMillis,
                                nextDate,
                                plan.amount,
                                14)

                            Log.d("GUS", "Added: $x")
                            total += x

                            calendar.add(Calendar.DAY_OF_YEAR, days)
                            Log.d("GUS", "start full week calc from: ${calendarToString(calendar)}")

                            difference = lastDate.timeInMillis - calendar.timeInMillis

                            Log.d("GUS", "Diff: $difference")
                            days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS).toInt()

                            Log.d("GUS", "Days to calculate full fortnights: $days")
                            val fullFortnights = days / 14

                            Log.d("GUS", "Full fortnights: $fullFortnights")

                            total += fullFortnights * plan.amount
                            Log.d("GUS", "Added: ${fullFortnights * plan.amount}")

                            val remainingDays = days - ( fullFortnights * 14)

                            Log.d("GUS", "Remaining days: $remainingDays")

                            x = (plan.amount / 14) * remainingDays

                            total += x
                            Log.d("GUS", "Added: $x")
                        }
                    }
                }

                /**
                 * Monthly sum
                 */
                TransactionFrequency.MONTHLY_SUM -> {
//
//
//                    //Log.d("GUS", "${plan.description} is monthly sum type")
//                    //Log.d("GUS", "")
//
//                    //Log.d("GUS", "Checking period between:")
//                    //Log.d("GUS", "${calendarToString(startDate)} and ${calendarToString(endDate)}")
//
//                    if(!(!plan.isStatusActive && plan.cancellationDate < startDate.timeInMillis)){
//                        //Log.d("GUS", "Not cancelled before start of period.")
//
//                        val calendar = Calendar.getInstance()
//                        val cancellationDate = Calendar.getInstance()
//
//                        /** Set initial plan date to the later of the first date of the plan or the first date of period */
//                        if(startDate.timeInMillis >= plan.firstExpectedDate) {
//                            //Log.d("GUS", "Period start date is later than first date of plan.")
//                            calendar.timeInMillis = startDate.timeInMillis
//                        }else{
//                            //Log.d("GUS", "First date of plan is later than period start date.")
//                            calendar.timeInMillis = plan.firstExpectedDate
//                        }
//
//                        cancellationDate.timeInMillis = plan.cancellationDate
//
//                        while(calendar.timeInMillis <= endDate.timeInMillis){
//                            //Log.d("GUS", "Start of examined sub-period: ${calendarToString(calendar)}")
//                            /** Check if plan is cancelled before the end of month */
//                            if(!plan.isStatusActive
//                                && calendar.get(Calendar.MONTH) == cancellationDate.get(Calendar.MONTH)
//                                && calendar.get(Calendar.YEAR) == cancellationDate.get(Calendar.YEAR)){
//
//                                //Log.d("GUS", "Plan is cancelled in this month.")
//                                //Log.d("GUS", "Calculating...")
//
//                                val x = calculateSubPeriodAmount(
//                                    calendar.timeInMillis,
//                                    plan.cancellationDate,
//                                    plan.amount,
//                                    calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
//                                )
//
//                                total += x
//
//                                //Log.d("GUS", "Added: $x")
//
//                                break
//                            }else{
//                                //Log.d("GUS", "Plan is NOT cancelled in this month.")
//
//                                if(calendar.get(Calendar.MONTH) == endDate.get(Calendar.MONTH)
//                                    && calendar.get(Calendar.YEAR) == endDate.get(Calendar.YEAR)){
//
//                                    //Log.d("GUS", "Period is over this month.")
//                                    //Log.d("GUS", "Calculating...")
//
//                                    val x = calculateSubPeriodAmount(
//                                        calendar.timeInMillis,
//                                        endDate.timeInMillis,
//                                        plan.amount,
//                                        calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
//                                    )
//
//                                    total += x
//
//                                    //Log.d("GUS", "Added: $x")
//
//                                }else{
//                                    //Log.d("GUS", "Period is NOT over this month.")
//
//                                    if(calendar.get(Calendar.DAY_OF_MONTH) == 1){
//                                        //Log.d("GUS", "Sub-period begins on 1st month.")
//                                        //Log.d("GUS", "Adding full month: ${plan.amount}")
//
//                                        total += plan.amount
//                                    }else{
//                                        //Log.d("GUS", "Sub-period DOES NOT begin on 1st.")
//                                        //Log.d("GUS", "Calculating...")
//
//                                        val endOfMonth = Calendar.getInstance()
//                                        endOfMonth.timeInMillis = calendar.timeInMillis
//
//                                        endOfMonth.set(Calendar.HOUR, 23)
//                                        endOfMonth.set(Calendar.MINUTE, 59)
//                                        endOfMonth.set(Calendar.SECOND, 59)
//                                        endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH))
//
//                                        val x = calculateSubPeriodAmount(
//                                            calendar.timeInMillis,
//                                            endOfMonth.timeInMillis,
//                                            plan.amount,
//                                            calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
//                                        )
//
//                                        total += x
//
//                                        //Log.d("GUS", "Added: $x")
//
//                                        calendar.set(Calendar.DAY_OF_MONTH, 1)
//                                    }
//                                }
//                            }
//
//                            calendar.add(Calendar.MONTH, 1)
//                        }
//                        /** */
//                        //Log.d("GUS", "All sub-periods examined. While loop done.")
//                    }else{
//                        //Log.d("GUS", "Cancelled before start of period.")
//                    }

                }

                /**
                 * Yearly sum
                 */
                TransactionFrequency.YEARLY_SUM -> {
//                    Log.d("GUS", "${plan.description} is yearly sum type")
//                    Log.d("GUS", "")
//
//                    Log.d("GUS", "Checking period between:")
//                    Log.d("GUS", "${calendarToString(startDate)} and ${calendarToString(endDate)}")
//
//                    if(!(!plan.isStatusActive && plan.cancellationDate < startDate.timeInMillis)){
//                        Log.d("GUS", "Not cancelled before start of period.")
//
//                        val calendar = Calendar.getInstance()
//                        val cancellationDate = Calendar.getInstance()
//
//                        /** Set initial plan date to the later of the first date of the plan or the first date of period */
//                        if(startDate.timeInMillis >= plan.firstExpectedDate) {
//                            Log.d("GUS", "Period start date is later than first date of plan.")
//                            calendar.timeInMillis = startDate.timeInMillis
//                        }else{
//                            Log.d("GUS", "First date of plan is later than period start date.")
//                            calendar.timeInMillis = plan.firstExpectedDate
//                        }
//
//                        cancellationDate.timeInMillis = plan.cancellationDate
//
//                        /** Calculate the last day of the required period */
//                        val lastDate = Calendar.getInstance()
//                        if(!plan.isStatusActive
//                            && plan.cancellationDate <= endDate.timeInMillis){
//                            Log.d("GUS", "plan cancelled before end of period")
//                            lastDate.timeInMillis = plan.cancellationDate
//                        }else{
//                            Log.d("GUS", "plan is NOT cancelled before end of period")
//                            lastDate.timeInMillis = endDate.timeInMillis
//                        }
//
//                        Log.d("GUS", "Last date to check: ${calendarToString(lastDate)}")
//
//                        var nextDate = calculateNextYearlyPlanDate(calendar, plan.firstExpectedDate)
//                        val c = Calendar.getInstance()
//                        c.timeInMillis=nextDate
//                        Log.d("GUS", "NextDate: ${calendarToString(c)}")
//
//                        if(nextDate >= lastDate.timeInMillis){
//                            val difference =  lastDate.timeInMillis - calendar.timeInMillis
//                            val days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS).toInt()
//
//                            Log.d("GUS", "This is a short period. Only $days days long.")
//
//                            val x = calculateSubPeriodAmount(
//                                calendar.timeInMillis,
//                                lastDate.timeInMillis,
//                                plan.amount,
//                                365)
//
//                            Log.d("GUS", "Added: $x")
//                            total += x
//
//                        }else{
//                            Log.d("GUS", "Period not over before next exp. date.")
//
//                            val difference =  nextDate - calendar.timeInMillis
//                            val days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS).toInt()
//
//                            Log.d("GUS", "$days days left till beginning of first full year.")
//
//                            var x = calculateSubPeriodAmount(
//                                calendar.timeInMillis,
//                                nextDate,
//                                plan.amount,
//                                365)
//
//                            Log.d("GUS", "Added for this period: $x")
//                            total += x
//
//                            calendar.timeInMillis = nextDate
//                            calendar.add(Calendar.DAY_OF_YEAR, 1)
//                            nextDate = calculateNextYearlyPlanDate(calendar, plan.firstExpectedDate)
//
//                            c.timeInMillis = nextDate
//                            Log.d("GUS", "End of first full year: ${calendarToString(c)}")
//
//                            while(nextDate <= lastDate.timeInMillis){
//                                Log.d("GUS", "next date is before last date -> Add full amount: ${plan.amount}")
//
//                                total += plan.amount
//
//                                calendar.timeInMillis = nextDate
//                                calendar.add(Calendar.DAY_OF_YEAR, 1)
//                                nextDate = calculateNextYearlyPlanDate(calendar, plan.firstExpectedDate)
//                                calendar.add(Calendar.DAY_OF_YEAR, -1)
//                                c.timeInMillis = nextDate
//                                Log.d("GUS", "Next date(increased in while loop): ${calendarToString(c)}")
//                            }
//
//                            Log.d("GUS", "While loop done. Now calculate the remaining days if any.")
//
//                            x = calculateSubPeriodAmount(
//                                calendar.timeInMillis,
//                                lastDate.timeInMillis,
//                                plan.amount,
//                                365)
//
//                            Log.d("GUS", "Added for this period: $x")
//                            total += x
//                        }
//                    }
                }



            }
        }

        Log.d("GUS", "Total added: $total")
        return total
    }


    /**
     * Calculate sub-period amount
     */
    private fun calculateSubPeriodAmount(startOfSubPeriod: Long, endOfSubPeriod: Long, amountForFullPeriod: Float, daysInFullPeriod: Int): Float{
        /** Calculate the amount for the shorter period */
        val difference = endOfSubPeriod - startOfSubPeriod
        val days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS).toInt()
        val dailyAmount = amountForFullPeriod / daysInFullPeriod

        Log.d("GUS", "days: $days")
        Log.d("GUS", "daily amount: $dailyAmount")

        return days.toFloat() * dailyAmount
    }


    /**
     * This helper method calculates the planned amount of a single plan
     * within the given period.
     * We need this helper method because while calculating different periodic
     * plans, we would repeat a lot of code otherwise.
     *
     * @param currentPlan The plan to calculate the amount.*
     * @param startDate The calendar containing the starting date of the period.
     * @param endDate The calendar containing the end date of the period.
     * @param nextDateCalculatorMethod A lambda that gives the next expected date of the plan.
     *
     */
    private fun calculatePlanAmountForPeriod(
        currentPlan: Plan,
        startDate: Calendar,
        endDate: Calendar,
        nextDateCalculatorMethod: (calendarNow: Calendar, firstPlanDate: Long) -> Long
    ): Float {
        val calendar = Calendar.getInstance()
        /** Get the first expected date within the period */
        var amount = 0f
        calendar.timeInMillis = startDate.timeInMillis - DAY_IN_MILLIS
        currentPlan.nextExpectedDate = nextDateCalculatorMethod(calendar, currentPlan.firstExpectedDate)

        /** While next date is within the given period we keep going */
        while (currentPlan.nextExpectedDate <= endDate.timeInMillis) {

            if (currentPlan.nextExpectedDate in startDate.timeInMillis..endDate.timeInMillis) {
                /** Check if plan is cancelled at the time of next expected date */
                if (!currentPlan.isStatusActive && currentPlan.nextExpectedDate > currentPlan.cancellationDate) {
                    /** Plan is cancelled, exit loop */
                    break
                } else {
                    /** Plan is active, add amount */
                    amount += currentPlan.amount
                }
            }

            /** Get the next expected date */
            calendar.timeInMillis = currentPlan.nextExpectedDate + DAY_IN_MILLIS
            currentPlan.nextExpectedDate = nextDateCalculatorMethod(calendar, currentPlan.firstExpectedDate)
        }
        return amount
    }

    /**
     * This method calculates the next expected dates of a plan list
     * and organizes the list by them
     */
    private fun calculateNextExpectedDates(planList: MutableList<Plan>){

        val calendarNow = Calendar.getInstance()

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

    companion object{
        const val DAY_IN_MILLIS = 1000*24*60*60
    }
}
/*
/** Only execute if plan wasn't cancelled before the given period */
if(!(!plan.isStatusActive && plan.cancellationDate <= startDate.timeInMillis)){
    Log.d("GUS", "Sum...")
    val calendar = Calendar.getInstance()
    val cancellationDate = Calendar.getInstance()

    /** Set initial plan date to the later of the first date of the plan or the first date of period */
    calendar.timeInMillis =
        if(startDate.timeInMillis >= plan.firstExpectedDate) {
            startDate.timeInMillis
        }else{
            plan.firstExpectedDate
        }

    //if(!plan.isStatusActive){
    cancellationDate.timeInMillis = plan.cancellationDate
    //}

    Log.d("GUS", "plan: ${plan.description}")
    Log.d("GUS", "period: ${Date(startDate.timeInMillis)} - ${Date(endDate.timeInMillis)}")
    Log.d("GUS", "initial cal. date: ${Date(calendar.timeInMillis)}")
    Log.d("GUS", "cancellation date: ${Date(cancellationDate.timeInMillis)}")


    while(calendar.timeInMillis < endDate.timeInMillis) {
        /**
         * If next period is not a full month either because the date of cancellation
         * or because of the given period we calculate the amount for the shorter period
         * otherwise we add a full month
         */
        if (!plan.isStatusActive
            && calendar.get(Calendar.MONTH) == cancellationDate.get(Calendar.MONTH)
            && calendar.get(Calendar.YEAR) == cancellationDate.get(Calendar.YEAR)) {

            /** Calculate the amount for the shorter period */
            val difference = cancellationDate.timeInMillis - calendar.timeInMillis
            val days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS).toInt()
            val dailyAmount = plan.amount / calendar.getActualMaximum(Calendar.MONTH)

            total += days.toFloat() * dailyAmount
        } else {
            if (calendar.get(Calendar.MONTH) == endDate.get(Calendar.MONTH)
                && calendar.get(Calendar.YEAR) == endDate.get(Calendar.MONTH)
            ) {
                /** Check if the last sub-period begins on the first day of the month*/
                if (calendar.get(Calendar.DAY_OF_MONTH) == 1) {
                    /** Add the amount for the full month */
                    total += plan.amount
                } else {
                    /** Otherwise calculate amount for the shorter period. */
                    val lastDayOfMonth = Calendar.getInstance()
                    lastDayOfMonth.set(
                        Calendar.DAY_OF_MONTH,
                        calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    )

                    val difference =
                        lastDayOfMonth.timeInMillis - calendar.timeInMillis
                    val days =
                        TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS)
                            .toInt()
                    val dailyAmount =
                        plan.amount / calendar.getActualMaximum(Calendar.MONTH)

                    total += days.toFloat() * dailyAmount

                    /** Now set the day of month to 1st */
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                }
            }else{
                /** Calculate the amount for the shorter period */
                val difference = endDate.timeInMillis - calendar.timeInMillis
                val days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS).toInt()
                val dailyAmount = plan.amount / calendar.getActualMaximum(Calendar.MONTH)
            }

            calendar.add(Calendar.MONTH, 1)
        }
    }
}*/