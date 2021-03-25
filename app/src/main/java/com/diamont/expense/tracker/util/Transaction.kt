package com.diamont.expense.tracker.util

/**
 * This data class holds information about a transaction
 */
data class Transaction(
    val transactionId : Int,
    val transactionType : TransactionType,
    val description : String,
    val amount : Float,
    val category: TransactionCategory,
    val secondParty : String,
    val method: PaymentMethod,
    val period : Int,
    val planned: TransactionPlanned
){

    /**
     * Call this method to get amount as string
     */
    fun getAmount() : String{
        return "$$amount"
    }

    /**
     * Call this method to get the date as string
     */
    fun getDate() : String = "2021. 02. 16."


}
