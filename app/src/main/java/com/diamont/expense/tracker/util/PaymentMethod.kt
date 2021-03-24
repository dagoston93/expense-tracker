package com.diamont.expense.tracker.util

import com.diamont.expense.tracker.R

/**
 * This enum lists the possible payment methods
 */
enum class PaymentMethod(val value : Int, val stringId : Int) {
    CASH(0, R.string.cash),
    CARD(1, R.string.card);

    /**
     * Make it possible to get the type by its int value
     * (for example from database or from xml attribute)
     */
    companion object{
        private val map = PaymentMethod.values().associateBy(PaymentMethod::value)
        fun fromInt(type : Int) = map[type]
    }


}