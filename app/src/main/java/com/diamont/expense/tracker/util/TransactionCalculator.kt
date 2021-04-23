package com.diamont.expense.tracker.util

import com.diamont.expense.tracker.util.database.Transaction
import com.diamont.expense.tracker.util.enums.PaymentMethod
import com.diamont.expense.tracker.util.enums.TransactionType
import java.util.*

class TransactionCalculator(private val calendars: CurrentCalendars) {

    private var _totalCard: Float = 0f
    val totalCard: Float
        get() = _totalCard

    private var _totalCash: Float = 0f
    val totalCash: Float
        get() = _totalCash

    private var _totalBalance: Float = 0f
    val totalBalance: Float
        get() = _totalBalance

    private var initialCash: Float = 0f
    private var initialCard: Float = 0f

    /**
     * The list of transactions
     */
    private var transactionList = listOf<Transaction>()

    /**
     * This method sets the initial balance
     */
    fun setInitialBalance(cash: Float, card: Float){
        initialCard = card
        initialCash = cash
    }

    /**
     * This method sets the list of transactions we use
     */
    fun setCurrentTransactionList(list: List<Transaction>){
        transactionList = list
        calculateBalance()
    }

    /**
     * Call this method to get total amount of transactions during a period
     */
    fun calculateTotalActualAmountWithinPeriodByType(
        startDate: Calendar,
        endDate: Calendar,
        transactionType:TransactionType
    ): Float{

        var total: Float = 0f

        /**
         * Iterate through the list and add amount to total if within period
         */
        for(transaction in transactionList){
            if(transaction.date in startDate.timeInMillis..endDate.timeInMillis){
                if(transaction.transactionType == transactionType) {
                    total += transaction.amount
                }
            }
        }

        return total
    }

    /**
     * Call this method to get total amount of transactions during a period for a given plan
     */
    fun calculateTotalActualAmountWithinPeriodByPlanIdAndType(
        startDate: Calendar,
        endDate: Calendar,
        transactionType:TransactionType,
        planId: Int
    ): Float{

        var total: Float = 0f

        /**
         * Iterate through the list and add amount to total if within period
         */
        for(transaction in transactionList){
            if (transaction.date in startDate.timeInMillis..endDate.timeInMillis) {
                if (transaction.transactionType == transactionType && transaction.planId == planId ) {
                    total += transaction.amount
                }
            }
        }

        return total
    }

    /**
     * Call this method to get total amount of planned transactions during a period
     */
    fun calculateTotalActualPlannedAmountWithinPeriodByType(
        startDate: Calendar,
        endDate: Calendar,
        transactionType:TransactionType,
    ): Float{

        var total: Float = 0f

        /**
         * Iterate through the list and add amount to total if within period
         */
        for(transaction in transactionList){
            if (transaction.date in startDate.timeInMillis..endDate.timeInMillis) {
                if (transaction.transactionType == transactionType && transaction.planId != -1) {
                    total += transaction.amount
                }
            }
        }

        return total
    }

    /**
     * Returns the total amount of non planned transactions within period
     */
    fun calculateTotalActualNotPlannedAmountWithinPeriodByType(
        startDate: Calendar,
        endDate: Calendar,
        transactionType:TransactionType,
    ): Float{
        return calculateTotalActualAmountWithinPeriodByPlanIdAndType(startDate, endDate, transactionType, -1)
    }

    /**
     * Call this method to calculate the total actual amount this month
     */
    fun getCurrentMonthTotalActualAmount(transactionType:TransactionType): Float{
        return calculateTotalActualAmountWithinPeriodByType(
            calendars.calendarStartOfMonth,
            calendars.calendarEndOfMonth,
            transactionType
        )
    }

    /**
     * Call this method to calculate the balance
     */
    private fun calculateBalance() {
        /**
         * If no transactions added yet, we simply set every value to 0
         */
        if(transactionList.isEmpty()){
            _totalBalance = initialCard + initialCash
            _totalCash = initialCash
            _totalCard = initialCard
        }else{
            /** The required variables */
            var total: Float = initialCard + initialCash
            var cash: Float = initialCash
            var card: Float = initialCard

            /** Iterate through all transactions */
            for(transaction in transactionList){
                if(transaction.transactionType == TransactionType.EXPENSE){
                    /**
                     * Expense
                     */
                    total -= transaction.amount

                    /** Check if it is cash or card */
                    if(transaction.method == PaymentMethod.CARD){
                        card -= transaction.amount
                    }else{
                        cash -= transaction.amount
                    }
                }else if(transaction.transactionType == TransactionType.INCOME){
                    /**
                     * Income
                     */
                    total += transaction.amount

                    /** Check if it is cash or card */
                    if(transaction.method == PaymentMethod.CARD){
                        card += transaction.amount
                    }else{
                        cash += transaction.amount
                    }
                }else if(transaction.transactionType == TransactionType.DEPOSIT){
                    /**
                     * Deposit
                     */
                    cash -= transaction.amount
                    card += transaction.amount
                }else if(transaction.transactionType == TransactionType.WITHDRAW){
                    /**
                     * Withdrawal
                     */
                    cash += transaction.amount
                    card -= transaction.amount
                }
            }

            /** Update the live data */
            _totalBalance = total
            _totalCard = card
            _totalCash = cash
        }
    }

    /**
     * Call this method to get the transaction amounts within period
     * by category ids
     */
    fun getTransactionAmountByCategories(
        startDate: Calendar,
        endDate: Calendar,
        transactionType:TransactionType
    ): List<AmountListItem> {
        val list = mutableListOf<AmountListItem>()

        for(transaction in transactionList){
            if (transaction.date in startDate.timeInMillis..endDate.timeInMillis) {
                if (transaction.transactionType == transactionType) {
                    /** If list doesn't contain category yet, we add it*/
                    var listItem = list.find { it.id == transaction.categoryId }

                    if(listItem == null){
                        list.add(AmountListItem(transaction.categoryId, 0f))
                        listItem = list.find { it.id == transaction.categoryId }
                    }

                    listItem!!.amount += transaction.amount
                }
            }
        }

        return list
    }

    /**
     * Call this method to get the transaction amounts within period
     * by category ids
     */
    fun getTransactionAmountByPlans(
        startDate: Calendar,
        endDate: Calendar,
        transactionType:TransactionType
    ): List<AmountListItem> {
        val list = mutableListOf<AmountListItem>()

        for(transaction in transactionList){
            if (transaction.date in startDate.timeInMillis..endDate.timeInMillis) {
                if (transaction.transactionType == transactionType) {
                    /** If list doesn't contain the plan yet, we add it*/
                    var listItem = list.find { it.id == transaction.planId }

                    if(listItem == null){
                        list.add(AmountListItem(transaction.planId, 0f))
                        listItem = list.find { it.id == transaction.planId }
                    }

                    listItem!!.amount += transaction.amount
                }
            }
        }

        return list
    }

    /**
     * Call this method to get the first transaction date
     */
    fun getFirstTransactionDate(): Long{
        var firstDate: Long = 0L

        if(transactionList.isNotEmpty()){
            firstDate = transactionList[0].date

            for(transaction in transactionList)
            {
                if(transaction.date < firstDate){
                    firstDate = transaction.date
                }
            }
        }
        return firstDate
    }

    /**
     * Call this method to get the last transaction date
     */
    fun getLastTransactionDate(): Long{
        var lastDate: Long = 0L

        if(transactionList.isNotEmpty()){
            lastDate = transactionList[0].date

            for(transaction in transactionList)
            {
                if(transaction.date > lastDate){
                    lastDate = transaction.date
                }
            }
        }
        return lastDate
    }

}