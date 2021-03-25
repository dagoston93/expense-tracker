package com.diamont.expense.tracker.util

/**
 * This data class holds information about a transaction category
 */
data class TransactionCategory(
    val categoryId : Int,
    val categoryName: String,
    val categoryColorResId : Int
)