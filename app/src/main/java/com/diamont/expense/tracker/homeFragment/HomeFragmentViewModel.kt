package com.diamont.expense.tracker.homeFragment

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.diamont.expense.tracker.util.Currency
import com.diamont.expense.tracker.util.KEY_PREF_CURRENCY_ID
import com.diamont.expense.tracker.util.KEY_PREF_INITIAL_CARD
import com.diamont.expense.tracker.util.KEY_PREF_INITIAL_CASH
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
            transactionData = databaseDao.getAllTransactionsExcludePlansSuspend()
            calculateBalance()
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
        initialCard = sharedPreferences.getFloat(KEY_PREF_INITIAL_CARD, 0f)
        initialCash = sharedPreferences.getFloat(KEY_PREF_INITIAL_CASH, 0f)
    }

    /**
     * Call this method to calculate the balance
     */
    private fun calculateBalance() {
        /**
         * If no transactions added yet, we simply set every value to 0
         */
        if(transactionData.isEmpty()){
            _totalBalance.value = 0f
            _totalCash.value = 0f
            _totalCard.value = 0f
        }else{
            /** The required variables */
            var total: Float = initialCard + initialCash
            var cash: Float = initialCash
            var card: Float = initialCard

            /** Iterate through all transactions */
            for(transaction in transactionData){
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
            _totalBalance.value = total
            _totalCard.value = card
            _totalCash.value = cash
        }
    }
}
