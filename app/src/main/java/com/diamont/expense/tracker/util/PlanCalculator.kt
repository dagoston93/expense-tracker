package com.diamont.expense.tracker.util

import com.diamont.expense.tracker.util.database.Plan
import com.diamont.expense.tracker.util.enums.TransactionFrequency
import java.util.*
import java.util.concurrent.TimeUnit
import android.util.Log
import com.diamont.expense.tracker.util.database.Transaction

class PlanCalculator(private val calendars: CurrentCalendars) {
    /**
     * The list of the plans we use for our calculations
     */
    private var planList = mutableListOf<Plan>()

    /**
     * This method sets the list of plans we use
     */
    fun setCurrentPlanList(list: MutableList<Plan>){
        planList = list
        calculateNextExpectedDates()
    }

    /**
     * Call this method to calculate total expenses/incomes within an interval
     *
     * @param startDate A calendar containing the start date of the period.
     * @param endDate A calendar containing the end date of the period.
     */
    fun calculateTotalPlannedAmountWithinPeriod(startDate: Calendar, endDate: Calendar): Float{
        var total: Float = 0f

        /**
         * Iterate through the list
         */
        for(plan in planList){


            when(plan.frequency){
                /**
                 * One time
                 */
                TransactionFrequency.ONE_TIME -> {
                    if(plan.nextExpectedDate in startDate.timeInMillis..endDate.timeInMillis){
                        total += plan.amount
                    }
                }

                /** Weekly once */
                TransactionFrequency.WEEKLY_ONCE -> {
                    total += calculateOneTimePlanAmountForPeriod(
                        plan,
                        startDate,
                        endDate
                    ){ calendarNow: Calendar, firstPlanDate: Long ->
                        calculateNextWeeklyPlanDate(calendarNow, firstPlanDate)
                    }
                }

                /**
                 * Fortnightly once
                 */
                TransactionFrequency.FORTNIGHTLY_ONCE -> {
                    total += calculateOneTimePlanAmountForPeriod(
                        plan,
                        startDate,
                        endDate
                    ){ calendarNow: Calendar, firstPlanDate: Long ->
                        calculateNextFortnightlyPlanDate(calendarNow, firstPlanDate)
                    }
                }

                /**
                 * Monthly once
                 */
                TransactionFrequency.MONTHLY_ONCE -> {
                    total += calculateOneTimePlanAmountForPeriod(
                        plan,
                        startDate,
                        endDate
                    ){ calendarNow: Calendar, firstPlanDate: Long ->
                        calculateNextMonthlyPlanDate(calendarNow, firstPlanDate)
                    }
                }

                /**
                 * Yearly once
                 */
                TransactionFrequency.YEARLY_ONCE ->{
                    total += calculateOneTimePlanAmountForPeriod(
                        plan,
                        startDate,
                        endDate
                    ){ calendarNow: Calendar, firstPlanDate: Long ->
                        calculateNextYearlyPlanDate(calendarNow, firstPlanDate)
                    }
                }

                /**
                 * Weekly sum
                 */
                TransactionFrequency.WEEKLY_SUM -> {
                    /** Only calculate if plan is active within period */
                    if(!((!plan.isStatusActive
                                && plan.cancellationDate < startDate.timeInMillis)
                                || endDate.timeInMillis < plan.firstExpectedDate)){

                        val calendar = Calendar.getInstance()
                        val cancellationDate = Calendar.getInstance()

                        /** Set initial plan date to the later of the first date of the plan or the first date of period */
                        calendar.timeInMillis = if (startDate.timeInMillis >= plan.firstExpectedDate) {
                            startDate.timeInMillis
                        } else {
                            plan.firstExpectedDate
                        }

                        cancellationDate.timeInMillis = plan.cancellationDate

                        /** Calculate the last day of the required period */
                        val lastDate = Calendar.getInstance()
                        lastDate.timeInMillis = if(!plan.isStatusActive
                            && plan.cancellationDate <= endDate.timeInMillis){
                            plan.cancellationDate
                        }else{
                            endDate.timeInMillis
                        }

                        /** Calculate the next expected date */
                        val nextDate = calculateNextWeeklyPlanDate(calendar, plan.firstExpectedDate)

                        /** Check if period is over before next date */
                        if(nextDate >= lastDate.timeInMillis){
                            /** If so calculate the amount for those few days*/
                            total += calculateSumTypePlanSubPeriodAmount(
                                calendar.timeInMillis,
                                lastDate.timeInMillis,
                                plan.amount,
                                7)
                        }else{
                            /** Calculate the amount and days between start date of period and the start of the first full week*/
                            var difference =  nextDate - calendar.timeInMillis
                            var days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS).toInt()
                            total += calculateSumTypePlanSubPeriodAmount(
                                calendar.timeInMillis,
                                nextDate,
                                plan.amount,
                                7)

                            /** Calculate how many days left till end of period */
                            calendar.add(Calendar.DAY_OF_YEAR, days)
                            difference = lastDate.timeInMillis - calendar.timeInMillis
                            days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS).toInt()

                            /** Calculate the amount for the full weeks */
                            val fullWeeks = days / 7
                            total += fullWeeks * plan.amount

                            /** Calculate the amount for the remaining days */
                            val remainingDays = days - ( fullWeeks * 7)
                            total += (plan.amount / 7) * remainingDays
                        }
                    }
                }

                /**
                 * Fortnightly sum
                 */
                TransactionFrequency.FORTNIGHTLY_SUM -> {
                    /** Only calculate if plan is active within period */
                    if(!((!plan.isStatusActive
                                && plan.cancellationDate < startDate.timeInMillis)
                                || endDate.timeInMillis < plan.firstExpectedDate)){

                        val calendar = Calendar.getInstance()
                        val cancellationDate = Calendar.getInstance()

                        /** Set initial plan date to the later of the first date of the plan or the first date of period */
                        calendar.timeInMillis = if (startDate.timeInMillis >= plan.firstExpectedDate){
                            startDate.timeInMillis
                        } else {
                            plan.firstExpectedDate
                        }

                        cancellationDate.timeInMillis = plan.cancellationDate

                        /** Calculate the last day of the required period */
                        val lastDate = Calendar.getInstance()
                        lastDate.timeInMillis =if (!plan.isStatusActive && plan.cancellationDate <= endDate.timeInMillis){
                            plan.cancellationDate
                        } else {
                            endDate.timeInMillis
                        }

                        /** Calculate the next expected date */
                        val nextDate = calculateNextFortnightlyPlanDate(calendar, plan.firstExpectedDate)

                        /** Check if period is over before next date */
                        if(nextDate >= lastDate.timeInMillis){
                            total += calculateSumTypePlanSubPeriodAmount(calendar.timeInMillis, lastDate.timeInMillis, plan.amount, 14)
                        }else{
                            /** Calculate the amount and days between start date of period and the start of the first full fortnight*/
                            var difference =  nextDate - calendar.timeInMillis
                            var days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS).toInt()
                            total += calculateSumTypePlanSubPeriodAmount(calendar.timeInMillis, nextDate, plan.amount, 14)

                            /** Calculate how many days left till end of period */
                            calendar.add(Calendar.DAY_OF_YEAR, days)
                            difference = lastDate.timeInMillis - calendar.timeInMillis
                            days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS).toInt()

                            /** Calculate the amount for the full fortnights */
                            val fullFortnights = days / 14
                            total += fullFortnights * plan.amount

                            /** Calculate the amount for the remaining days */
                            val remainingDays = days - ( fullFortnights * 14)
                            total += (plan.amount / 14) * remainingDays
                        }
                    }
                }

                /**
                 * Monthly sum
                 */
                TransactionFrequency.MONTHLY_SUM -> {
                    /** Only calculate if plan is active within period */
                    if(!((!plan.isStatusActive
                                && plan.cancellationDate < startDate.timeInMillis)
                                || endDate.timeInMillis < plan.firstExpectedDate)){

                        val calendar = Calendar.getInstance()
                        val cancellationDate = Calendar.getInstance()

                        /** Set initial plan date to the later of the first date of the plan or the first date of period */
                        calendar.timeInMillis = if(startDate.timeInMillis >= plan.firstExpectedDate) {
                            startDate.timeInMillis
                        }else{
                            plan.firstExpectedDate
                        }

                        cancellationDate.timeInMillis = plan.cancellationDate

                        while(calendar.timeInMillis <= endDate.timeInMillis){
                            /** Check if plan is cancelled before the end of month */
                            if(!plan.isStatusActive
                                && calendar.get(Calendar.MONTH) == cancellationDate.get(Calendar.MONTH)
                                && calendar.get(Calendar.YEAR) == cancellationDate.get(Calendar.YEAR)){

                                /** If cancelled we add the correct amount than break the loop*/
                                total += calculateSumTypePlanSubPeriodAmount(
                                    calendar.timeInMillis,
                                    plan.cancellationDate,
                                    plan.amount,
                                    calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                                )

                                break
                            }else{
                                /** Check if period is over this month */
                                if(calendar.get(Calendar.MONTH) == endDate.get(Calendar.MONTH)
                                    && calendar.get(Calendar.YEAR) == endDate.get(Calendar.YEAR)){

                                    /** If over we add the correct amount than break the loop*/
                                    total += calculateSumTypePlanSubPeriodAmount(
                                        calendar.timeInMillis,
                                        endDate.timeInMillis,
                                        plan.amount,
                                        calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                                    )
                                }else{
                                    /** If sub-period begins on the 1st, we add full month amount */
                                    if(calendar.get(Calendar.DAY_OF_MONTH) == 1){
                                        total += plan.amount
                                    }else{
                                        /** If sub-period does NOT begin on the 1st, we add the correct amount */
                                        val endOfMonth = Calendar.getInstance()
                                        endOfMonth.timeInMillis = calendar.timeInMillis

                                        endOfMonth.set(Calendar.HOUR, 23)
                                        endOfMonth.set(Calendar.MINUTE, 59)
                                        endOfMonth.set(Calendar.SECOND, 59)
                                        endOfMonth.set(
                                            Calendar.DAY_OF_MONTH, endOfMonth.getActualMaximum(
                                                Calendar.DAY_OF_MONTH))

                                        total += calculateSumTypePlanSubPeriodAmount(
                                            calendar.timeInMillis,
                                            endOfMonth.timeInMillis,
                                            plan.amount,
                                            calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                                        )

                                        calendar.set(Calendar.DAY_OF_MONTH, 1)
                                    }
                                }
                            }

                            /** Jump to next month */
                            calendar.add(Calendar.MONTH, 1)
                        }
                    }
                }

                /**
                 * Yearly sum
                 */
                TransactionFrequency.YEARLY_SUM -> {
                    /** Only calculate if plan is active within period */
                    if(!((!plan.isStatusActive
                                && plan.cancellationDate < startDate.timeInMillis)
                                || endDate.timeInMillis < plan.firstExpectedDate)){

                        val calendar = Calendar.getInstance()
                        val cancellationDate = Calendar.getInstance()

                        /** Set initial plan date to the later of the first date of the plan or the first date of period */
                        calendar.timeInMillis = if(startDate.timeInMillis >= plan.firstExpectedDate) {
                            startDate.timeInMillis
                        }else{
                            plan.firstExpectedDate
                        }

                        cancellationDate.timeInMillis = plan.cancellationDate

                        /** Calculate the last day of the required period */
                        val lastDate = Calendar.getInstance()
                        lastDate.timeInMillis = if(!plan.isStatusActive
                            && plan.cancellationDate <= endDate.timeInMillis){
                            plan.cancellationDate
                        }else{
                            endDate.timeInMillis
                        }

                        var nextDate = calculateNextYearlyPlanDate(calendar, plan.firstExpectedDate)

                        if(nextDate >= lastDate.timeInMillis){
                            /** If period is over before next full year starts we calculate amount for the shorter period */
                            total += calculateSumTypePlanSubPeriodAmount(
                                calendar.timeInMillis,
                                lastDate.timeInMillis,
                                plan.amount,
                                365
                            )
                        }else{
                            /** Now calculate the amount for the days before the first full year */
                            total += calculateSumTypePlanSubPeriodAmount(
                                calendar.timeInMillis,
                                nextDate,
                                plan.amount,
                                365)

                            /** Calculate the date of the next full year */
                            calendar.timeInMillis = nextDate
                            calendar.add(Calendar.DAY_OF_YEAR, 1)
                            nextDate = calculateNextYearlyPlanDate(calendar, plan.firstExpectedDate)

                            /** As long as the date of next full year is before the end of period, we keep adding the yearly amount */
                            while(nextDate <= lastDate.timeInMillis){
                                total += plan.amount

                                calendar.timeInMillis = nextDate
                                calendar.add(Calendar.DAY_OF_YEAR, 1)
                                nextDate = calculateNextYearlyPlanDate(calendar, plan.firstExpectedDate)
                                calendar.add(Calendar.DAY_OF_YEAR, -1)

                            }

                            /** Now we add the amount for the remaining days till the end of period */
                            total += calculateSumTypePlanSubPeriodAmount(
                                calendar.timeInMillis,
                                lastDate.timeInMillis,
                                plan.amount,
                                365
                            )
                        }
                    }
                }
            }
        }

        return total
    }

    /**
     * Calculate sub-period amount for sum type plans
     */
    private fun calculateSumTypePlanSubPeriodAmount(
        startOfSubPeriod: Long,
        endOfSubPeriod: Long,
        amountForFullPeriod: Float,
        daysInFullPeriod: Int
    ): Float{
        /** Calculate the amount for the shorter period */
        val difference = endOfSubPeriod - startOfSubPeriod
        val days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS).toInt()
        val dailyAmount = amountForFullPeriod / daysInFullPeriod

        return days.toFloat() * dailyAmount
    }

    /**
     * This helper method calculates the planned amount of a single plan
     * within the given period for regular one time type plans.
     * We need this helper method because while calculating different periodic
     * plans, we would repeat a lot of code otherwise.
     *
     * @param currentPlan The plan to calculate the amount.*
     * @param startDate The calendar containing the starting date of the period.
     * @param endDate The calendar containing the end date of the period.
     * @param nextDateCalculatorMethod A lambda that gives the next expected date of the plan.
     *
     */
    private fun calculateOneTimePlanAmountForPeriod(
        currentPlan: Plan,
        startDate: Calendar,
        endDate: Calendar,
        nextDateCalculatorMethod: (calendarNow: Calendar, firstPlanDate: Long) -> Long
    ): Float {
        val calendar = Calendar.getInstance()
        var amount = 0f
        var nextExpectedDate: Long = 0

        /** Set initial plan date to the later of the first date of the plan or the first date of period */
        calendar.timeInMillis = if(startDate.timeInMillis >= currentPlan.firstExpectedDate) {
            startDate.timeInMillis
        }else{
            currentPlan.firstExpectedDate
        }

        /** Get the first expected date within the period */
        calendar.timeInMillis -= DAY_IN_MILLIS
        nextExpectedDate = nextDateCalculatorMethod(calendar, currentPlan.firstExpectedDate)

        /** Calculate the last day of the required period */
        val lastDate = Calendar.getInstance()
        lastDate.timeInMillis = if(!currentPlan.isStatusActive
            && currentPlan.cancellationDate <= endDate.timeInMillis){
            currentPlan.cancellationDate
        }else{
            endDate.timeInMillis
        }

        /** While next date is within the given period we keep going */
        while (nextExpectedDate <= endDate.timeInMillis) {
            if (nextExpectedDate in calendar.timeInMillis..lastDate.timeInMillis) {
                /** Check if plan is cancelled at the time of next expected date */
                if (!currentPlan.isStatusActive && nextExpectedDate > currentPlan.cancellationDate) {
                    /** Plan is cancelled, exit loop */
                    break
                } else {
                    /** Plan is active, add amount */
                    amount += currentPlan.amount
                }
            }else{
                break
            }

            /** Get the next expected date */
            calendar.timeInMillis = nextExpectedDate + DAY_IN_MILLIS
            nextExpectedDate = nextDateCalculatorMethod(calendar, currentPlan.firstExpectedDate)
        }
        return amount
    }

    /**
     * This method calculates the next expected dates of the plan list
     * and organizes the list by them
     */
    fun calculateNextExpectedDates(){

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
     * This method returns the total planned amount during the current month
     */
    fun getCurrentMonthTotalPlanAmount(): Float{
        /**
         * Return monthly expenses
         */
        return calculateTotalPlannedAmountWithinPeriod(
            calendars.calendarStartOfMonth,
            calendars.calendarEndOfMonth
        )
    }

    /**
     * This method returns the total planned amount during the current year
     */
    fun getCurrentYearTotalPlanAmount(): Float{
        /**
         * Return yearly expenses
         */
        return calculateTotalPlannedAmountWithinPeriod(
            calendars.calendarStartOfYear,
            calendars.calendarEndOfYear
        )
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

    companion object{
        const val DAY_IN_MILLIS = 1000*24*60*60
    }
}