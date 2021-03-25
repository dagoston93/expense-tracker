package com.diamont.expense.tracker.util

import com.diamont.expense.tracker.R

/**
 * This enum lists the possible frequencies of transactions
 */
enum class TransactionFrequency(val value : Int, val stringId :Int) {
    ONE_TIME(0, R.string.one_time),
    WEEKLY_ONCE(1, R.string.weekly_once),
    WEEKLY_SUM(2, R.string.weekly_sum),
    FORTNIGHTLY_ONCE(3, R.string.fortnightly_once),
    FORTNIGHTLY_SUM(4, R.string.fortnightly_sum),
    MONTHLY_ONCE(5, R.string.monthly_once),
    MONTHLY_SUM(6, R.string.monthly_sum),
    YEARLY_ONCE(7, R.string.yearly_once),
    YEARLY_SUM(8, R.string.yearly_sum);

    /**
     * Make it possible to get the type by its int value
     * (for example from database or from xml attribute)
     */
    companion object{
        private val map =TransactionFrequency.values().associateBy(TransactionFrequency::value)
        fun fromInt(type : Int) = map[type]
    }
}