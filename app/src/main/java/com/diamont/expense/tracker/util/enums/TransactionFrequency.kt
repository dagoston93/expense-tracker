package com.diamont.expense.tracker.util.enums

import android.content.Context
import com.diamont.expense.tracker.R

/**
 * This enum lists the possible frequencies of transactions
 */
enum class TransactionFrequency(val id : Int, val stringId :Int) {
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
     * Companion object
     */
    companion object{
        /**
         * Make it possible to get the frequency by its int value
         * (for example from database or from xml attribute)
         */
        private val map = values().associateBy(TransactionFrequency::id)
        fun fromInt(fromId : Int) = map[fromId]

        /**
         * Return the id from the index in the array given by getValuesAsStringList()
         * Need it for exposed dropdown menu and database usage
         */
        fun getIdFromIndex(index : Int) = values()[index].id

        /**
         * Return the enum value from the index
         * To make it easier to find the item chosen in a dropdown
         */
        fun getFromIndex(index: Int) = values()[index]

        /**
         * Return the possible values as a string array
         * We need it for array adapters for exposed
         * dropdown menus
         */
        fun getValuesAsStringList(context: Context) : List<String>{
            val stringList = mutableListOf<String>()
            val enumValues = values()
            for(i in enumValues.indices){
                stringList.add(context.resources.getString(enumValues[i].stringId))
            }
            return stringList
        }
    }
}