package com.diamont.expense.tracker.homeFragment

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.diamont.expense.tracker.util.*
import com.diamont.expense.tracker.util.Currency
import com.diamont.expense.tracker.util.database.Plan
import com.diamont.expense.tracker.util.database.Transaction
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import com.diamont.expense.tracker.util.enums.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.*

class HomeFragmentViewModel(
    private val appContext: Application,
    private val databaseDao: TransactionDatabaseDao,
    private val sharedPreferences: SharedPreferences
) : AndroidViewModel(appContext) {
    /**
     * Set up some live data
     */
    private val _totalBalance = MutableLiveData<Float?>(null)
    val totalBalance: LiveData<String> = Transformations.map(_totalBalance){
        displayAmount(_totalBalance.value)
    }

    private val _totalCash = MutableLiveData<Float?>(null)
    val totalCash: LiveData<String> = Transformations.map(_totalCash){
        displayAmount(_totalCash.value)
    }

    private val _totalCard = MutableLiveData<Float?>(null)
    val totalCard: LiveData<String> = Transformations.map(_totalCard){
        displayAmount(_totalCard.value)
    }

    private val _plannedMonthlyIncome = MutableLiveData<Float?>(null)
    val plannedMonthlyIncome: LiveData<String> = Transformations.map(_plannedMonthlyIncome){
        displayAmount(_plannedMonthlyIncome.value)
    }

    private val _totalMonthlyIncome = MutableLiveData<Float?>(null)
    val totalMonthlyIncome: LiveData<String> = Transformations.map(_totalMonthlyIncome){
        displayAmount(_totalMonthlyIncome.value)
    }

    private val _plannedMonthlyExpense = MutableLiveData<Float?>(null)
    val plannedMonthlyExpense: LiveData<String> = Transformations.map(_plannedMonthlyExpense){
        displayAmount(_plannedMonthlyExpense.value)
    }

    private val _totalMonthlyExpense = MutableLiveData<Float?>(null)
    val totalMonthlyExpense: LiveData<String> = Transformations.map(_totalMonthlyExpense){
        displayAmount(_totalMonthlyExpense.value)
    }

    private val _monthlyIncomeProgress = MutableLiveData<Int>(0)
    val monthlyIncomeProgress: LiveData<Int>
        get() = _monthlyIncomeProgress

    private val _monthlyExpenseProgress = MutableLiveData<Int>(0)
    val monthlyExpenseProgress: LiveData<Int>
        get() = _monthlyExpenseProgress

    private val _plannedMonthlySavings = MutableLiveData<Float?>(null)
    val plannedMonthlySavings: LiveData<String> = Transformations.map(_plannedMonthlySavings){
        displayAmount(_plannedMonthlySavings.value)
    }

    private val _totalSavings = MutableLiveData<Float?>(null)
    val totalSavings: LiveData<String> = Transformations.map(_totalSavings){
        displayAmount(_totalSavings.value)
    }

    /**
     * Set up some variables
     */
    private var transactionData = listOf<Transaction>()
    private var incomePlans = mutableListOf<Plan>()
    private var expensePlans = mutableListOf<Plan>()

    private var calendars = CurrentCalendars()
    private var transactionCalculator = TransactionCalculator(calendars)
    private var planCalculator = PlanCalculator(calendars)
    private var currencyInUse: Currency? = null
    private var decimalFormat: DecimalFormat? = null
    private var initialCash: Float = 0f
    private var initialCard: Float = 0f

    /**
     * Set up coroutine job and the scope
     */
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Constructor
     */
    init{
        getCurrencyInUse()
        getInitialBalance()
        getDataFromDatabase()
    }

    /**
     * This method retrieves transaction data
     */
    private fun getDataFromDatabase(){
        uiScope.launch {
            transactionData = databaseDao.getAllTransactionsSuspend()
            expensePlans = databaseDao.getExpensePlansSuspend().toMutableList()
            incomePlans = databaseDao.getIncomePlansSuspend().toMutableList()

            onTransactionDataReceived()
        }
    }

    /**
     * Call this method to find the currency in use
     */
    private fun getCurrencyInUse() {
        val currencyId = sharedPreferences.getInt(KEY_PREF_CURRENCY_ID, 0)
        currencyInUse = Currency.availableCurrencies[currencyId]
        decimalFormat = Currency.getDecimalFormat(currencyId)
    }

    /**
     * Call this method to get the initial balance
     */
    private fun getInitialBalance() {
        initialCash = sharedPreferences.getFloat(KEY_PREF_INITIAL_CASH, 0f)
        initialCard = sharedPreferences.getFloat(KEY_PREF_INITIAL_CARD, 0f)

        transactionCalculator.setInitialBalance(initialCash, initialCard)
    }

    /**
     * Call this method after transaction data received
     */
    private fun onTransactionDataReceived(){
        transactionCalculator.setCurrentTransactionList(transactionData)

        /**
         * Get the current balance
         */
        _totalCard.value = transactionCalculator.totalCard
        _totalCash.value = transactionCalculator.totalCash
        _totalBalance.value = transactionCalculator.totalBalance

        /**
         * Get the planned and total expenses
         */
        planCalculator.setCurrentPlanList(expensePlans)
        _plannedMonthlyExpense.value = planCalculator.getCurrentMonthTotalPlanAmount()
        _totalMonthlyExpense.value = transactionCalculator.getCurrentMonthTotalActualAmount(TransactionType.EXPENSE)

        /**
         * Get the planned and total incomes
         */
        planCalculator.setCurrentPlanList(incomePlans)
        _plannedMonthlyIncome.value = planCalculator.getCurrentMonthTotalPlanAmount()
        _totalMonthlyIncome.value = transactionCalculator.getCurrentMonthTotalActualAmount(TransactionType.INCOME)

        /**
         * Calculate plan/total percentages
         */
        val incomePercentage = if(_plannedMonthlyIncome.value!! != 0f){
            _totalMonthlyIncome.value!! /_plannedMonthlyIncome.value!!
        }else{
            0f
        }

        val expensePercentage = if(_plannedMonthlyExpense.value!! != 0f ){
            _totalMonthlyExpense.value!! /_plannedMonthlyExpense.value!!
        }else{
            0f
        }

        _monthlyIncomeProgress.value = (incomePercentage * 100).toInt()
        _monthlyExpenseProgress.value = (expensePercentage * 100).toInt()

        /**
         * Calculate planned monthly savings
         */
        _plannedMonthlySavings.value = _plannedMonthlyIncome.value!! - _plannedMonthlyExpense.value!!

        /**
         * Of first transaction and the date of the date of the day before the first day of this month
         */
        val startDate = Calendar.getInstance()
        startDate.timeInMillis = transactionCalculator.getFirstTransactionDate()

        val endDate = Calendar.getInstance()
        endDate.timeInMillis = calendars.calendarStartOfMonth.timeInMillis

        endDate.add(Calendar.DAY_OF_YEAR, -1)
        endDate.set(Calendar.SECOND, 59)
        endDate.set(Calendar.MINUTE, 59)
        endDate.set(Calendar.HOUR, 23)

        /**
         * Get total incomes and expenses before this month
         */
        val actualIncomesSoFar = transactionCalculator.calculateTotalActualAmountWithinPeriodByType(
            startDate,
            endDate,
            TransactionType.INCOME
        )

        val actualExpensesSoFar = transactionCalculator.calculateTotalActualAmountWithinPeriodByType(
            startDate,
            endDate,
            TransactionType.EXPENSE
        )

        /**
         * Calculate total savings before this month
         */
        _totalSavings.value = initialCard + initialCash + actualIncomesSoFar - actualExpensesSoFar
    }

    /**
     * Call this method to convert an amount to a formatted string
     */
    private fun displayAmount(amount: Float?): String{
        var amountString: String = ""

        if (amount != null && decimalFormat != null) {
            amountString = decimalFormat!!.format(amount)
        }

        return amountString
    }
}
