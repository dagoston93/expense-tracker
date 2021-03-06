package com.diamont.expense.tracker.settingsFragment

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.*
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SettingsFragmentViewModel(
    private val resources: Resources,
    private val sharedPreferences: SharedPreferences,
    private val databaseDao: TransactionDatabaseDao
) : ViewModel() {

    /**
     * Create some live data
     */
    private var _isAuthenticationRequired= MutableLiveData<Boolean?>(null)
    val isAuthenticationRequired: LiveData<Boolean?>
        get() = _isAuthenticationRequired

    private var _isFingerprintEnabled = MutableLiveData<Boolean?>(null)
    val isFingerprintEnabled: LiveData<Boolean?>
        get() = _isFingerprintEnabled

    private var _isDarkThemeEnabled = MutableLiveData<Boolean?>(null)
    val isDarkThemeEnabled: LiveData<Boolean?>
        get() = _isDarkThemeEnabled

    private var _selectedLanguageString = MutableLiveData<String>("")
    val selectedLanguageString: LiveData<String>
        get() = _selectedLanguageString

    private var _selectedLocale: String = ""
    val selectedLocale: String
        get() = _selectedLocale

    /**
     * Set up coroutine job and the scope
     */
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Constructor
     */
    init{
        _isAuthenticationRequired.value = sharedPreferences.getBoolean(KEY_PREF_AUTHENTICATION_REQUIRED, false)
        _isFingerprintEnabled.value = sharedPreferences.getBoolean(KEY_PREF_FINGERPRINT_ENABLED, false)
        _isDarkThemeEnabled.value = sharedPreferences.getBoolean(KEY_PREF_DARK_THEME_ENABLED, false)
        _selectedLocale = sharedPreferences.getString(KEY_PREF_LOCALE, "") ?: ""

        updateLanguageString()
    }

    /**
     * Call this method if user switches the authentication switch
     */
    fun onAuthenticationSwitchClicked(isTurnedOn: Boolean){
        /** Save state to live data */
        _isAuthenticationRequired.value = isTurnedOn

        /** Save the new state in shared prefs*/
        with(sharedPreferences.edit()){
            putBoolean(KEY_PREF_AUTHENTICATION_REQUIRED, isTurnedOn)
            apply()
        }
    }

    /**
     * Call this method if user switches the fingerprint switch
     */
    fun onFingerprintSwitchClicked(isTurnedOn: Boolean){
        /** Save state to live data */
        _isFingerprintEnabled.value = isTurnedOn

        /** Save the new state in shared prefs*/
        with(sharedPreferences.edit()){
            putBoolean(KEY_PREF_FINGERPRINT_ENABLED, isTurnedOn)
            apply()
        }
    }

    /**
     * Call this method if user switches the fingerprint switch
     */
    fun onDarkThemeSwitchClicked(isTurnedOn: Boolean){
        /** Save state to live data */
        _isDarkThemeEnabled.value = isTurnedOn

        /** Save the new state in shared prefs*/
        with(sharedPreferences.edit()){
            putBoolean(KEY_PREF_DARK_THEME_ENABLED, isTurnedOn)
            apply()
        }
    }

    /**
     * Call this method if user selects a language
     */
    fun onLanguageSelected(localeString: String){
        /** Only act if a different language is selected */
        if(_selectedLocale != localeString) {
            /** Save state */
            _selectedLocale = localeString

            updateLanguageString()

            /** Save the new state in shared prefs*/
            with(sharedPreferences.edit()) {
                putString(KEY_PREF_LOCALE, _selectedLocale)
                commit()
            }
        }
    }

    /**
     * This method updates the language live data
     */
    private fun updateLanguageString() {
        /** Update string live data */
        val appLocale = LocaleUtil.supportedLocales.find { it.localeString == _selectedLocale }

        _selectedLanguageString.value = resources.getString(
            if (appLocale == null) {
                R.string.default_language
            } else {
                appLocale.stringResId
            }
        )
    }

    /**
     * Call this method if user confirms to delete all data
     */
    fun resetApp(onSuccess: () -> Unit){

        /** Reset shared preferences */
        with(sharedPreferences.edit()) {
            putBoolean(KEY_PREF_INITIAL_SETUP_DONE, false)
            putBoolean(KEY_PREF_AUTHENTICATION_REQUIRED, false)
            putBoolean(KEY_PREF_FINGERPRINT_ENABLED, false)
            putString(KEY_PREF_PIN_CODE, "")
            putInt(KEY_PREF_CURRENCY_ID, 0)
            putFloat(KEY_PREF_INITIAL_CARD, 0f)
            putFloat(KEY_PREF_INITIAL_CASH, 0f)

            apply()
        }

        /** Delete data from database and call the onSuccess lambda */
        uiScope.launch {
            databaseDao.clearDatabaseSuspend()
            onSuccess()
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