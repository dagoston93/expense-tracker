package com.diamont.expense.tracker.util.enums

import android.content.Context
import com.diamont.expense.tracker.R

/**
 * This enum lists the possible payment methods
 */
enum class PaymentMethod(val id : Int, val stringId : Int) {
    CASH(0, R.string.cash),
    CARD(1, R.string.card);

    /**
     * Companion object
     */
    companion object{
        /**
         * Make it possible to get the payment method by its int value
         * (for example from database or from xml attribute)
         */
        private val map = values().associateBy(PaymentMethod::id)
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
         * This method returns the index of a payment method
         */
        fun getIndex(method: PaymentMethod): Int{
            var index = 0
            /** Find the index of the selected payment method */
            for (i in values().indices) {
                if (method == values()[i]) {
                    index = i
                }
            }

            return index
        }
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