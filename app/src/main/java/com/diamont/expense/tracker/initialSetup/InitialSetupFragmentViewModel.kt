package com.diamont.expense.tracker.initialSetup

import android.app.Application
import android.content.SharedPreferences
import android.view.View
import androidx.biometric.BiometricManager
import androidx.lifecycle.*
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.*
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class InitialSetupFragmentViewModel(
    private val appContext: Application,
    private val sharedPreferences: SharedPreferences,
    private val databaseDao: TransactionDatabaseDao
) : AndroidViewModel(appContext) {

    /** Declare the required variables */
    private var isAuthenticationRequired : Boolean = false
    private var isFingerprintEnabled : Boolean = false
    private var pinCode : String = ""
    private var isFirstPinEntryAccepted : Boolean = false
    private var selectedCurrency = Currency(0,"","", "")
    val selectedCurrencySign : String
        get() = selectedCurrency.sign

    private var initialBalanceCash : Float? = 0f
    private var initialBalanceCard : Float? = 0f

    private var _isFingerprintSensorAvailable : Boolean = false
    val isFingerprintSensorAvailable : Boolean
        get() = _isFingerprintSensorAvailable

    /** Create some live data for ui to observe */
    private var _setOrConfirmPinStr = MutableLiveData<String>(appContext.getString(R.string.set_pin_code))
    val setOrConfirmPinStr : LiveData<String>
        get() = _setOrConfirmPinStr

    private var _isPinCodeSaved = MutableLiveData<Boolean>(false)
    val isPinCodeSaved : LiveData<Boolean>
        get() = _isPinCodeSaved

    private var _activePage = MutableLiveData<Int>(0)
    val activePage : LiveData<Int>
        get() = _activePage

    private var _isSetupProcessComplete : Boolean = false
    val isSetupProcessComplete : Boolean
        get() = _isSetupProcessComplete

    private var _isPinEntryErrorMessageVisible = MutableLiveData<Boolean>(false)
    val isPinEntryErrorMessageVisible = Transformations.map(_isPinEntryErrorMessageVisible){
        if(it){
            View.VISIBLE
        }else{
            View.INVISIBLE
        }
    }

    /**
     * Set up coroutine job and the scope
     */
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Constructor
     */
    init{
        /** Check if biometric authentication is available */
        val biometricManager = BiometricManager.from(appContext)

        if(biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
         == BiometricManager.BIOMETRIC_SUCCESS){
            _isFingerprintSensorAvailable = true
        }
    }

    /**
     * Call this method from the fragments
     * if the user turns the authentication on
     */
    fun setAuthenticationRequired(){
        isAuthenticationRequired = true
    }

    /**
     * Call this method from the fragments
     * if user enables fingerprint sensor
     */
    fun enableFingerprintSensor(){
        isFingerprintEnabled = true
    }

    /**
     * Call this method to set the id of the selected currency
     *
     * @param currency The id of the currency set int the list
     * in Currency.availableCurrencies
     */
    fun setSelectedCurrencyId(currency : Currency){
        selectedCurrency = currency
    }

    /**
     * Call this method to set the initial balance details
     */
    fun setInitialBalance(cash : Float?, card: Float?){
        initialBalanceCash = cash
        initialBalanceCard = card

        /** We set it in the last step so save the values */
        saveInitialValues()
    }

    /**
     * Call this method to set the active page.
     * This is used for the dot indicator.
     * Each fragment should call it with its index
     * as in the initial setup flow.
     *
     * @param page The index of the fragment in the flow.
     */
    fun setActivePage(page : Int){
        _activePage.value = page
    }

    /**
     * Save the initial setup values
     */
    private fun saveInitialValues(){
        with(sharedPreferences.edit()) {
            putBoolean(KEY_PREF_INITIAL_SETUP_DONE, true)
            putBoolean(KEY_PREF_AUTHENTICATION_REQUIRED, isAuthenticationRequired)
            putBoolean(KEY_PREF_FINGERPRINT_ENABLED, isFingerprintEnabled)
            //putBoolean(KEY_PREF_DARK_THEME_ENABLED, false)
            putString(KEY_PREF_PIN_CODE, pinCode)
            putInt(KEY_PREF_CURRENCY_ID, selectedCurrency.id)
            putFloat(KEY_PREF_INITIAL_CARD, initialBalanceCard ?: 0f)
            putFloat(KEY_PREF_INITIAL_CASH, initialBalanceCash ?: 0f)

            apply()
        }

        addDefaultCategories()
    }

    /**
     * Call this method to add the default categories to the database
     */
    private fun addDefaultCategories(){
        uiScope.launch {
            databaseDao.addDefaultCategoriesSuspend(appContext)
            _isSetupProcessComplete = true
        }
    }

    /**
     * Call this method if the  Set Pin Code / Confirm Pin Code button is clicked
     *
     * @param code - The value the user has entered in the PinCodeInputView
     */
    fun setPinButtonClicked(code : String){
        /** Check if we should Set or Confirm?*/
        if(isFirstPinEntryAccepted){
            /** Confirm so check if the two code matches */
            if(code == pinCode){
                /** Codes match, we are ready to continue */
                _isPinCodeSaved.value = true
            }else{
                /** Codes don't match, we set the error message and restart process */
                isFirstPinEntryAccepted = false
                _setOrConfirmPinStr.value = appContext.getString(R.string.set_pin_code)
                _isPinEntryErrorMessageVisible.value = true
            }
        }else{
            /** Set so we save the first entry and change text*/
            pinCode = code
            isFirstPinEntryAccepted = true
            _setOrConfirmPinStr.value = appContext.getString(R.string.confirm_pin_code)
            _isPinEntryErrorMessageVisible.value = false
        }
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