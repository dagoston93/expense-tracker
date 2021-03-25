package com.diamont.expense.tracker.util

import com.diamont.expense.tracker.R

/**
 * This enum lists the possible options for planned/unplanned transactions
 */
enum class TransactionPlanned(val value : Int, val stringId : Int) {
    PLANNED(0, R.string.planned),
    NOT_PLANNED(1, R.string.not_planned);

    /**
     * Make it possible to get the type by its int value
     * (for example from database or from xml attribute)
     */
    companion object{
        private val map = TransactionPlanned.values().associateBy(TransactionPlanned::value)
        fun fromInt(type : Int) = map[type]
    }
}