package com.diamont.expense.tracker.util

/**
 * This enum lists the possible types of transaction
 */
enum class TransactionType(val value : Int) {
    INCOME_ONE_TIME(0),
    INCOME_REGULAR(1),
    EXPENSE_ONE_TIME(2),
    EXPENSE_REGULAR(3),
    INCOME_PLAN(4),
    EXPENSE_PLAN(5),
    WITHDRAW(6),
    DEPOSIT(7);

    /**
     * Make it possible to get the type by its int value
     * (for example from database or from xml attribute)
     */
    companion object{
        private val map = TransactionType.values().associateBy(TransactionType::value)
        fun fromInt(type : Int) = map[type]
    }


}