package com.diamont.expense.tracker.util.enums

import android.content.Context
import com.diamont.expense.tracker.R

/**
 * This enum lists the possible options for planned/unplanned transactions
 */
enum class TransactionPlanned(val id : Int, val stringId : Int) {
    PLANNED(0, R.string.planned),
    NOT_PLANNED(1, R.string.not_planned);

    /**
     * Companion object
     */
    companion object{
        /**
         * Make it possible to get the enum value by its int value
         * (for example from database or from xml attribute)
         */
        private val map = values().associateBy(TransactionPlanned::id)
        fun fromInt(fromId : Int) = map[fromId]

        /**
         * Return the id from the index in the array given by getValuesAsStringList()
         * Need it for exposed dropdown menu and database usage
         */
        fun getIdFromIndex(index : Int) = values()[index].id

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