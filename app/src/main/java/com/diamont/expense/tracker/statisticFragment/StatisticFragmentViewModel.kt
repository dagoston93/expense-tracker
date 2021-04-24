package com.diamont.expense.tracker.statisticFragment

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
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

    private val _categoryPieChartData = MutableLiveData<PieData?>(null)
    val categoryPieChartData: LiveData<PieData?>
        get() = _categoryPieChartData

    private val _catPageTotalIncomeOrExpense = MutableLiveData<Float?>(null)
    val catPageTotalIncomeOrExpense: LiveData<String> = Transformations.map(_catPageTotalIncomeOrExpense){
        formatAmount(_catPageTotalIncomeOrExpense.value)
    }

    private val _catPageTotalIncomeOrExpenseLabel = MutableLiveData<String>("")
    val catPageTotalIncomeOrExpenseLabel: LiveData<String>
        get() = _catPageTotalIncomeOrExpenseLabel

    private val _planPlannedPieChartData = MutableLiveData<PieData?>(null)
    val planPlannedPieChartData: LiveData<PieData?>
        get() = _planPlannedPieChartData

    private val _planActualPieChartData = MutableLiveData<PieData?>(null)
    val planActualPieChartData: LiveData<PieData?>
        get() = _planActualPieChartData

    private val _planPageTotalPlanned = MutableLiveData<Float?>(null)
    val planPageTotalPlanned: LiveData<String> = Transformations.map(_planPageTotalPlanned){
        formatAmount(_planPageTotalPlanned.value)
    }

    private val _planPageTotalActual = MutableLiveData<Float?>(null)
    val planPageTotalActual: LiveData<String> = Transformations.map(_planPageTotalActual){
        formatAmount(_planPageTotalActual.value)
    }

    private val _planPageSelectedPlannedAmount = MutableLiveData<Float?>(null)
    val planPageSelectedPlannedAmount: LiveData<String> = Transformations.map(_planPageSelectedPlannedAmount){
        formatAmount(_planPageSelectedPlannedAmount.value)
    }

    private val _planPageSelectedActualAmount = MutableLiveData<Float?>(null)
    val planPageSelectedActualAmount: LiveData<String> = Transformations.map(_planPageSelectedActualAmount){
        formatAmount(_planPageSelectedActualAmount.value)
    }

    private val _planPageTotalPlannedIncomeOrExpenseLabel = MutableLiveData<String>("")
    val planPageTotalPlannedIncomeOrExpenseLabel: LiveData<String>
        get() = _planPageTotalPlannedIncomeOrExpenseLabel

    private val _planPageTotalActualIncomeOrExpenseLabel = MutableLiveData<String>("")
    val planPageTotalActualIncomeOrExpenseLabel: LiveData<String>
        get() = _planPageTotalActualIncomeOrExpenseLabel

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

    private var _actualAmountList: List<AmountListItem> = listOf()
    val actualAmountList: List<AmountListItem>
        get() = _actualAmountList

    private var _actualPieEntries: MutableList<PieEntry> = mutableListOf<PieEntry>()
    val actualPieEntries: MutableList<PieEntry>
        get() = _actualPieEntries

    private var _plannedAmountList: List<AmountListItem> = listOf()
    val plannedAmountList: List<AmountListItem>
        get() = _plannedAmountList

    private var _plannedPieEntries: MutableList<PieEntry> = mutableListOf<PieEntry>()
    val plannedPieEntries: MutableList<PieEntry>
        get() = _plannedPieEntries

    private var _dataColorList: MutableList<Int> = mutableListOf<Int>()
    val dataColorList: MutableList<Int>
        get() = _dataColorList

    private var _planStatisticDataList: MutableList<PlanStatisticData> = mutableListOf<PlanStatisticData>()
    val planStatisticDataList: MutableList<PlanStatisticData>
        get() = _planStatisticDataList

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
     * Create some constants for the statistic type indices
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
            planCalculator.setCurrentPlanList(expensePlans)
        }else if(index == IDX_INCOME_PLANS || index == IDX_INCOME_CATEGORIES){
            selectedTransactionType = TransactionType.INCOME
            planCalculator.setCurrentPlanList(incomePlans)
        }

        updateData()
    }

    /**
     * This method is called when filtering the items is needed
     */
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
                _actualAmountList = transactionCalculator.getTransactionAmountByCategories(
                    calendarStartDate,
                    calendarEndDate,
                    selectedTransactionType
                )

                val total = transactionCalculator.calculateTotalActualAmountWithinPeriodByType(
                    calendarStartDate,
                    calendarEndDate,
                    selectedTransactionType
                )


                _actualPieEntries = mutableListOf<PieEntry>()
                _dataColorList = mutableListOf<Int>()
                val percentTextColorList: MutableList<Int> = mutableListOf<Int>()

                for(dataIndex in _actualAmountList.indices){
                    val category = transactionCategories.find{ it.categoryId == _actualAmountList[dataIndex].id }
                    val description = category?.categoryName ?: ""
                    val percentage = getRoundedPercentage(total, _actualAmountList[dataIndex].amount)//((_actualAmountList[dataIndex].amount/total) * 100).roundToInt()
                    val color = ContextCompat.getColor(appContext, category?.categoryColorResId ?: R.color.category_color1)

                    _actualPieEntries.add(PieEntry(percentage, description, dataIndex))

                    _dataColorList.add(color)

                    percentTextColorList.add(getResolvedLabelColor(category?.categoryColorResId))
                }

                val dataSet = PieDataSet(_actualPieEntries, "")
                dataSet.setColors(_dataColorList)
                dataSet.valueTextSize = 14f
                dataSet.setValueTextColors(percentTextColorList)
                dataSet.setSliceSpace(4f)

                val data = PieData(dataSet)
                data.setValueFormatter(PercentageFormatter())

                _categoryPieChartData.value = data
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

            IDX_INCOME_PLANS, IDX_EXPENSE_PLANS -> {
                _actualAmountList = transactionCalculator.getTransactionAmountByPlans(
                    calendarStartDate,
                    calendarEndDate,
                    selectedTransactionType
                )

                val totalActual = transactionCalculator.calculateTotalActualAmountWithinPeriodByType(
                    calendarStartDate,
                    calendarEndDate,
                    selectedTransactionType
                )

                _plannedAmountList = planCalculator.getPlannedAmountsByPlanIds(calendarStartDate, calendarEndDate)
                val totalPlanned = planCalculator.calculateTotalPlannedAmountWithinPeriod(calendarStartDate, calendarEndDate)

                _planStatisticDataList = mutableListOf<PlanStatisticData>()

                val notPlanned = _actualAmountList.find { it.id == -1 }

                _planStatisticDataList.add(
                    PlanStatisticData(
                    -1,
                        appContext.resources.getString(R.string.not_planned),
                        ContextCompat.getColor(appContext, R.color.not_planned),
                        0f,
                        notPlanned?.amount ?: 0f,
                        0f,
                        //((notPlanned?.amount ?: 0f / totalActual) * 100).roundToInt().toFloat()
                        getRoundedPercentage(totalActual, notPlanned?.amount ?: 0f)
                    )
                )

                for(planData in _plannedAmountList){
                    //if(planData.amount != 0f){
                        val actualPlan = if(selectedTransactionType == TransactionType.EXPENSE){
                            expensePlans.find{ it.id == planData.id }
                        }else{
                            incomePlans.find{ it.id == planData.id }
                        }

                        val category = transactionCategories.find{ it.categoryId == actualPlan?.categoryId }
                        val actualData = _actualAmountList.find { it.id == planData.id }


                        val statData = PlanStatisticData(
                            planData.id,
                            actualPlan?.description ?: "",
                            ContextCompat.getColor(appContext, category?.categoryColorResId ?: R.color.not_planned),
                            planData.amount,
                            actualData?.amount ?: 0f,
                            getRoundedPercentage(totalPlanned, planData.amount),
                            getRoundedPercentage(totalActual, actualData?.amount ?: 0f)
                        )

                        _planStatisticDataList.add(statData)
                    //}
                }

                /**
                 * Generate actual pie entries
                 */
                _actualPieEntries = mutableListOf<PieEntry>()
                _dataColorList = mutableListOf<Int>()
                var percentTextColorList: MutableList<Int> = mutableListOf<Int>()

                for(dataIndex in _actualAmountList.indices){
                    if(_actualAmountList[dataIndex].amount != 0f){
                        val plan = if(selectedTransactionType == TransactionType.EXPENSE){
                            expensePlans.find{ it.id == _actualAmountList[dataIndex].id }
                        }else{
                            incomePlans.find{ it.id == _actualAmountList[dataIndex].id }
                        }
                        val category = transactionCategories.find{ it.categoryId == plan?.categoryId }
                        val description = plan?.description ?: appContext.resources.getString(R.string.not_planned)
                        val percentage = getRoundedPercentage(totalActual, _actualAmountList[dataIndex].amount)
                        val color = ContextCompat.getColor(appContext, category?.categoryColorResId ?: R.color.not_planned)

                        _actualPieEntries.add(PieEntry(percentage, description, _actualAmountList[dataIndex].id))
                        _dataColorList.add(color)

                        percentTextColorList.add(getResolvedLabelColor(category?.categoryColorResId))
                    }
                }

                var dataSet = PieDataSet(_actualPieEntries, "")
                dataSet.setColors(_dataColorList)
                dataSet.valueTextSize = 14f
                dataSet.setValueTextColors(percentTextColorList)
                dataSet.setSliceSpace(5f)

                var data = PieData(dataSet)
                data.setValueFormatter(PercentageFormatter())
                _planActualPieChartData.value = data

                /**
                 * Generate planned pie entries
                 */
                _plannedPieEntries = mutableListOf<PieEntry>()
                _dataColorList = mutableListOf<Int>()
                percentTextColorList = mutableListOf<Int>()

                for(dataIndex in _plannedAmountList.indices){
                    if(_plannedAmountList[dataIndex].amount != 0f){
                        val plan = if(selectedTransactionType == TransactionType.EXPENSE){
                            expensePlans.find{ it.id == _plannedAmountList[dataIndex].id }
                        }else{
                            incomePlans.find{ it.id == _plannedAmountList[dataIndex].id }
                        }
                        val category = transactionCategories.find{ it.categoryId == plan?.categoryId }
                        val description = plan?.description ?: appContext.resources.getString(R.string.not_planned)
                        val percentage = getRoundedPercentage(totalPlanned, _plannedAmountList[dataIndex].amount)
                        val color = ContextCompat.getColor(appContext, category?.categoryColorResId ?: R.color.not_planned)

                        _plannedPieEntries.add(PieEntry(percentage, description, _plannedAmountList[dataIndex].id))
                        _dataColorList.add(color)

                        percentTextColorList.add(getResolvedLabelColor(category?.categoryColorResId))
                    }
                }

                dataSet = PieDataSet(_plannedPieEntries, "")
                dataSet.setColors(_dataColorList)
                dataSet.valueTextSize = 14f
                dataSet.setValueTextColors(percentTextColorList)
                dataSet.setSliceSpace(5f)

                data = PieData(dataSet)
                data.setValueFormatter(PercentageFormatter())
                _planPlannedPieChartData.value = data

                _planPageTotalActual.value = totalActual
                _planPageTotalActualIncomeOrExpenseLabel.value = appContext.resources.getString(
                    if(selectedTransactionType == TransactionType.EXPENSE){
                        R.string.total_actual_expenses
                    }else{
                        R.string.total_actual_incomes
                    }
                )

                _planPageTotalPlanned.value = totalPlanned
                _planPageTotalPlannedIncomeOrExpenseLabel.value = appContext.resources.getString(
                    if(selectedTransactionType == TransactionType.EXPENSE){
                        R.string.total_planned_expenses
                    }else{
                        R.string.total_planned_incomes
                    }
                )

                Log.d("GUS", "tP: $totalPlanned")
                Log.d("GUS", "tA: $totalActual")
                Log.d("GUS", "actList: $_actualAmountList")
                Log.d("GUS", "plnList: $_plannedAmountList")

                Log.d("GUS", "sorted: $_planStatisticDataList")
            }
        }
    }

    /**
     * Call this method to get a percentage rounded to whole numbers
     */
    fun getRoundedPercentage(total: Float, fraction: Float): Float{
        var percent = 0f

        if(total != 0f){ /** Avoid division by zero */
            percent = ((fraction / total) * 100).roundToInt().toFloat()
        }

        return percent
    }

    /**
     * Call this method to determine the label color of pie chart
     */
    fun getResolvedLabelColor(sliceColorId: Int?): Int{
        return ContextCompat.getColor(appContext,
            when(sliceColorId){
                R.color.not_planned,
                R.color.category_color8,
                R.color.category_color11,
                R.color.category_color12,
                R.color.category_color13,
                R.color.category_color14-> R.color.black
                else -> R.color.white
            })
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
    fun formatAmount(amount: Float?): String{
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
        return formatAmount(_actualAmountList[idx].amount)
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