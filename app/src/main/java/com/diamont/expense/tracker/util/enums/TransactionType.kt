package com.diamont.expense.tracker.util.enums

import android.content.Context
import com.diamont.expense.tracker.R

/**
 * This enum lists the possible types of transaction
 */
enum class TransactionType(val id : Int, val stringId :Int) {
    /** Do NOT change the id's to keep database valid */
    EXPENSE(1, R.string.expense),
    INCOME(0, R.string.income),
    WITHDRAW(2, R.string.withdraw),
    DEPOSIT(3, R.string.deposit);

    /**
     * Companion object
     */
    companion object{
        /**
         * Make it possible to get the transaction type by its int value
         * (for example from database or from xml attribute)
         */
        private val map = values().associateBy(TransactionType::id)
        fun fromInt(fromId : Int) = map[fromId]

        /**
         * Return the id from the index in the array given by getValuesAsStringList()
         * Need it for exposed dropdown menu and database usage
         */
        fun getIdFromIndex(index : Int) = values()[index].id

        /**
         * Return the enum value from the index
         */
        fun getEnumValueFromIndex(index: Int) = values()[index]

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

