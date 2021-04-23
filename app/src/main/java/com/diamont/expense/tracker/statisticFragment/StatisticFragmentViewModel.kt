package com.diamont.expense.tracker.statisticFragment

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.*
import com.diamont.expense.tracker.util.Currency
import com.diamont.expense.tracker.util.database.Plan
import com.diamont.expense.tracker.util.database.Transaction
import com.diamont.expense.tracker.util.database.TransactionCategory
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import com.diamont.expense.tracker.util.enums.TransactionType
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.*
import kotlin.math.roundToInt

class StatisticFragmentViewModel (
    private val appContext: Application,
    private val databaseDao: TransactionDatabaseDao,
    private val sharedPreferences: SharedPreferences
) : DateRangeSelectorFragmentViewModel(appContext) {

    /**
     * Set up some live data
     */
    private val _statisticTypeStringList = MutableLiveData<List<String>>(listOf<String>())
    val statisticTypeStringList : LiveData<List<String>>
        get() = _statisticTypeStringList

    private val _totalIncomesPeriod = MutableLiveData<Float?>(null)
    val totalIncomesPeriod: LiveData<String> = Transformations.map(_totalIncomesPeriod){
        formatAmount(_totalIncomesPeriod.value)
    }

    private val _totalExpensesPeriod = MutableLiveData<Float?>(null)
    val totalExpensesPeriod: LiveData<String> = Transformations.map(_totalExpensesPeriod){
        formatAmount(_totalExpensesPeriod.value)
    }

    private val _plannedIncomesPeriod = MutableLiveData<Float?>(null)
    val plannedIncomesPeriod: LiveData<String> = Transformations.map(_plannedIncomesPeriod){
        formatAmount(_plannedIncomesPeriod.value)
    }

    private val _plannedExpensesPeriod = MutableLiveData<Float?>(null)
    val plannedExpensesPeriod: LiveData<String> = Transformations.map(_plannedExpensesPeriod){
        formatAmount(_plannedExpensesPeriod.value)
    }

    private val _notPlannedIncomesPeriod = MutableLiveData<Float?>(null)
    val notPlannedIncomesPeriod: LiveData<String> = Transformations.map(_notPlannedIncomesPeriod){
        formatAmount(_notPlannedIncomesPeriod.value)
    }

    private val _notPlannedExpensesPeriod = MutableLiveData<Float?>(null)
    val notPlannedExpensesPeriod: LiveData<String> = Transformations.map(_notPlannedExpensesPeriod){
        formatAmount(_notPlannedExpensesPeriod.value)
    }

    private val _savingsOrOverspendPeriod = MutableLiveData<Float?>(null)
    val savingsOrOverspendPeriod: LiveData<String> = Transformations.map(_savingsOrOverspendPeriod){
        formatAmount(_savingsOrOverspendPeriod.value)
    }

    private val _savingsOrOverspendLabel = MutableLiveData<String>("")
    val savingsOrOverspendLabel: LiveData<String>
        get() = _savingsOrOverspendLabel

    private val _pieChartData = MutableLiveData<PieData?>(null)
    val pieChartData: LiveData<PieData?>
        get() = _pieChartData

    private val _catPageTotalIncomeOrExpense = MutableLiveData<Float?>(null)
    val catPageTotalIncomeOrExpense: LiveData<String> = Transformations.map(_catPageTotalIncomeOrExpense){
        formatAmount(_catPageTotalIncomeOrExpense.value)
    }

    private val _catPageTotalIncomeOrExpenseLabel = MutableLiveData<String>("")
    val catPageTotalIncomeOrExpenseLabel: LiveData<String>
        get() = _catPageTotalIncomeOrExpenseLabel

    /**
     * Declare some variables
     */
    private var transactionCategories: List<TransactionCategory> = listOf<TransactionCategory>()
    private var transactionData = listOf<Transaction>()
    private var filteredTransactionData = listOf<Transaction>()
    private var incomePlans = mutableListOf<Plan>()
    //private var filteredIncomePlans = mutableListOf<Plan>()
    private var expensePlans = mutableListOf<Plan>()
    //private var filteredExpensePlans = mutableListOf<Plan>()
    private var selectedTransactionType: TransactionType = TransactionType.EXPENSE

    private var transactionCalculator = TransactionCalculator(calendars)
    private var planCalculator = PlanCalculator(calendars)
    private var currencyInUse: Currency? = null
    private var decimalFormat: DecimalFormat? = null
    private var selectedStatisticTypeIndex: Int = 0

    private var calendarFirstTransactionDate: Calendar = Calendar.getInstance()
    private var calendarLastTransactionDate: Calendar = Calendar.getInstance()

    private var amountList: List<AmountListItem> = listOf()

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
        getDataFromDatabase()

        _statisticTypeStringList.value = listOf(
            appContext.resources.getString(R.string.incomes_and_expenses),
            appContext.resources.getString(R.string.incomes_by_categories),
            appContext.resources.getString(R.string.expenses_by_categories),
            appContext.resources.getString(R.string.income_plans),
            appContext.resources.getString(R.string.expense_plans)
        )
    }

    /**
     * Create some constants for the statistic type indexes
     */
    companion object{
        const val IDX_INCOME_EXPENSE: Int = 0
        const val IDX_INCOME_CATEGORIES: Int = 1
        const val IDX_EXPENSE_CATEGORIES: Int = 2
        const val IDX_INCOME_PLANS: Int = 3
        const val IDX_EXPENSE_PLANS: Int = 4
    }

    /**
     * Call this method if selected statistic type changes
     */
    fun onSelectedStatisticTypeChanged(index: Int?){
        selectedStatisticTypeIndex = index ?: 0

        if(index == IDX_EXPENSE_PLANS || index == IDX_EXPENSE_CATEGORIES){
            selectedTransactionType = TransactionType.EXPENSE
        }else if(index == IDX_INCOME_PLANS || index == IDX_INCOME_CATEGORIES){
            selectedTransactionType = TransactionType.INCOME
        }

        updateData()
    }

    override fun filterItems() {
        filteredTransactionData = transactionData.filter{
            var isItemDisplayed = true

            /** Check if item is within date range */
            if(selectedPeriodIndex != IDX_WHOLE_PERIOD) {
                if (it.date !in calendarStartDate.timeInMillis..calendarEndDate.timeInMillis) {
                    isItemDisplayed = false
                }
            }

            isItemDisplayed
        }

        updateData()
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
     * Call this method when data from database received
     */
    private fun onDataReceived(){
        filteredTransactionData = transactionData
        transactionCalculator.setCurrentTransactionList(filteredTransactionData)
        calendarStartDate.timeInMillis = transactionCalculator.getFirstTransactionDate()
        calendarEndDate.timeInMillis = transactionCalculator.getLastTransactionDate()
        calendarFirstTransactionDate.timeInMillis = calendarStartDate.timeInMillis
        calendarLastTransactionDate.timeInMillis = calendarEndDate.timeInMillis

        updateData()
    }

    /**
     * Call this method to update data on screen in case a selected value changes
     */
    private fun updateData(){
        when(selectedStatisticTypeIndex){

            IDX_INCOME_EXPENSE -> {
                transactionCalculator.setCurrentTransactionList(filteredTransactionData)
                _totalIncomesPeriod.value = transactionCalculator.calculateTotalActualAmountWithinPeriodByType(
                    calendarStartDate,
                    calendarEndDate,
                    TransactionType.INCOME
                )

                _totalExpensesPeriod.value = transactionCalculator.calculateTotalActualAmountWithinPeriodByType(
                    calendarStartDate,
                    calendarEndDate,
                    TransactionType.EXPENSE
                )

                _plannedIncomesPeriod.value = transactionCalculator.calculateTotalActualPlannedAmountWithinPeriodByType(
                    calendarStartDate,
                    calendarEndDate,
                    TransactionType.INCOME
                )

                _plannedExpensesPeriod.value = transactionCalculator.calculateTotalActualPlannedAmountWithinPeriodByType(
                    calendarStartDate,
                    calendarEndDate,
                    TransactionType.EXPENSE
                )

                _notPlannedIncomesPeriod.value = transactionCalculator.calculateTotalActualNotPlannedAmountWithinPeriodByType(
                    calendarStartDate,
                    calendarEndDate,
                    TransactionType.INCOME
                )

                _notPlannedExpensesPeriod.value = transactionCalculator.calculateTotalActualNotPlannedAmountWithinPeriodByType(
                    calendarStartDate,
                    calendarEndDate,
                    TransactionType.EXPENSE
                )

                _savingsOrOverspendPeriod.value = _totalIncomesPeriod.value!! - _totalExpensesPeriod.value!!

                if( _savingsOrOverspendPeriod.value!! < 0){
                    _savingsOrOverspendPeriod.value = _savingsOrOverspendPeriod.value!! * -1
                    _savingsOrOverspendLabel.value = appContext.resources.getString(R.string.overspend_during_period)
                }else{
                    _savingsOrOverspendLabel.value = appContext.resources.getString(R.string.savings_during_period)
                }

            }

            IDX_INCOME_CATEGORIES, IDX_EXPENSE_CATEGORIES ->{
                amountList = transactionCalculator.getTransactionAmountByCategories(
                    calendarStartDate,
                    calendarEndDate,
                    selectedTransactionType
                )

                val total = transactionCalculator.calculateTotalActualAmountWithinPeriodByType(
                    calendarStartDate,
                    calendarEndDate,
                    selectedTransactionType
                )


                val values: MutableList<PieEntry> = mutableListOf<PieEntry>()
                val dataColorList: MutableList<Int> = mutableListOf<Int>()
                val percentTextColorList: MutableList<Int> = mutableListOf<Int>()

                for(dataIndex in amountList.indices){
                    val category = transactionCategories.find{ it.categoryId == amountList[dataIndex].id }
                    val description = category?.categoryName ?: ""
                    val percentage = ((amountList[dataIndex].amount/total) * 100).roundToInt()
                    val color = ContextCompat.getColor(appContext, category?.categoryColorResId ?: R.color.category_color1)

                    values.add(PieEntry(percentage.toFloat(), description, dataIndex))

                    ContextCompat.getColor(appContext, R.color.category_color1)
                    dataColorList.add(color)

                    percentTextColorList.add(ContextCompat.getColor(appContext,
                        when(category?.categoryColorResId){
                            R.color.category_color8,
                            R.color.category_color11,
                            R.color.category_color12,
                            R.color.category_color13,
                            R.color.category_color14-> R.color.black
                            else -> R.color.white
                        })
                    )
                }

                val dataSet = PieDataSet(values, "")
                dataSet.setColors(dataColorList)
                dataSet.valueTextSize = 14f
                dataSet.setValueTextColors(percentTextColorList)

                val data = PieData(dataSet)
                data.setValueFormatter(PercentageFormatter())

                _pieChartData.value = data
                _catPageTotalIncomeOrExpense.value = total
                _catPageTotalIncomeOrExpenseLabel.value = appContext.resources.getString(
                    if(selectedTransactionType == TransactionType.EXPENSE){
                        R.string.total_expenses
                    }else{
                        R.string.total_incomes
                    }
                )

                //Log.d("GUS", "v: $values")
                //Log.d("GUS", "$amounts")
                //Log.d("GUS", "$total")
            }
        }
    }


    /**
     * This method is called by the base class when the whole period option is selected
     */
    override fun onWholePeriodSelected() {
        calendarStartDate.timeInMillis = calendarFirstTransactionDate.timeInMillis
        calendarEndDate.timeInMillis = calendarLastTransactionDate.timeInMillis
    }

    /**
     * This method retrieves transaction data
     */
    private fun getDataFromDatabase(){
        uiScope.launch {
            transactionData = databaseDao.getAllTransactionsSuspend()
            transactionCategories = databaseDao.getCategoriesSuspend()
            expensePlans = databaseDao.getExpensePlansSuspend().toMutableList()
            incomePlans = databaseDao.getIncomePlansSuspend().toMutableList()

            onDataReceived()
        }
    }

    /**
     * Call this method to convert an amount to a formatted string
     */
    private fun formatAmount(amount: Float?): String{
        var amountString: String = ""

        if (amount != null && decimalFormat != null) {
            amountString = decimalFormat!!.format(amount)
        }

        return amountString
    }

    /**
     * Call this method to get a formatted amount from the amount list
     */
    fun getAmountStringFromList(idx: Int): String{
        return formatAmount(amountList[idx].amount)
    }

    /**
     * onCleared() is called when view model is destroyed
     * in this case we need to cancel coroutines
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}