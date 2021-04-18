package com.diamont.expense.tracker.homeFragment

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.diamont.expense.tracker.util.*
import com.diamont.expense.tracker.util.database.Transaction
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import com.diamont.expense.tracker.util.enums.PaymentMethod
import com.diamont.expense.tracker.util.enums.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.DecimalFormat


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
        if(_totalBalance.value == null || decimalFormat == null){
            ""
        }else{
            decimalFormat!!.format(_totalBalance.value)
        }
    }

    private val _totalCash = MutableLiveData<Float?>(null)
    val totalCash: LiveData<String> = Transformations.map(_totalCash){
        if(_totalCash.value == null || decimalFormat == null){
            ""
        }else{
            decimalFormat!!.format(_totalCash.value)
        }
    }

    private val _totalCard = MutableLiveData<Float?>(null)
    val totalCard: LiveData<String> = Transformations.map(_totalCard){
        if(_totalCard.value == null || decimalFormat == null){
            ""
        }else{
            decimalFormat!!.format(_totalCard.value)
        }
    }

    /**
     * Set up some variables
     */
    private var transactionData = listOf<Transaction>()
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
        getTransactionData()
    }

    /**
     * This method retrieves transaction data
     */
    private fun getTransactionData(){
        uiScope.launch {
            transactionData = databaseDao.getAllTransactionsSuspend()
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
        _totalCard.value = transactionCalculator.totalCard
        _totalCash.value = transactionCalculator.totalCash
        _totalBalance.value = transactionCalculator.totalBalance
    }
}
