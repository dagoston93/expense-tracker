package com.diamont.expense.tracker.util

import com.diamont.expense.tracker.R

/**
 * This enum lists the possible types of transaction
 */
enum class TransactionType(val value : Int, val stringId :Int) {
    INCOME(0, R.string.income),
    EXPENSE(1, R.string.expense),
    WITHDRAW(2, R.string.withdraw),
    DEPOSIT(3, R.string.deposit);

    /**
     * Make it possible to get the type by its int value
     * (for example from database or from xml attribute)
     */
    companion object{
        private val map = TransactionType.values().associateBy(TransactionType::value)
        fun fromInt(type : Int) = map[type]
    }
}