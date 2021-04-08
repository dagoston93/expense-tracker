package com.diamont.expense.tracker.planFragment

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.Currency
import com.diamont.expense.tracker.util.KEY_PREF_CURRENCY_ID
import com.diamont.expense.tracker.util.database.Transaction
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class PlanFragmentViewModel (
    private val appContext: Application,
    private val databaseDao: TransactionDatabaseDao,
    private val sharedPreferences: SharedPreferences
) : AndroidViewModel(appContext) {
    /**
     * Set up some live data
     */
    private val _cardViewTitle = MutableLiveData<String>(appContext.resources.getString(R.string.total_planned_expenses))
    val cardViewTitle: LiveData<String>
        get() = _cardViewTitle


    /**
     * Set up some variables
     */
    private var plans = listOf<Transaction>()
    private var currencyInUse: Currency? = null
    private var decimalFormat: DecimalFormat? = null

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
        getPlanData()
    }

    /**
     * This method retrieves plan data from database
     */
    private fun getPlanData(){
        uiScope.launch {
            plans = databaseDao.getAllTransactionsExcludePlansSuspend()
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
     * Call this method if tabs are changed
     *
     * @param tabPosition:  0 if Expense tab selected
     *                      1 id Income tab selected
     */
    fun selectedTabChanged(tabPosition: Int){
        if(tabPosition == 0){
            /**
             * Expense tab selected
             */
            _cardViewTitle.value = appContext.resources.getString(R.string.total_planned_expenses)
        }else{
            /**
             * Income tab selected
             */
            _cardViewTitle.value = appContext.resources.getString(R.string.total_planned_incomes)
        }
    }
}