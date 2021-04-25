package com.diamont.expense.tracker.settingsFragment

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diamont.expense.tracker.util.*
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SettingsFragmentViewModel(
    private val appContext: Application,
    private val sharedPreferences: SharedPreferences,
    private val databaseDao: TransactionDatabaseDao
) : AndroidViewModel(appContext) {

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
     * Call this method if user confirms to delete all data
     */
    fun resetApp(onSuccess: () -> Unit){
        Log.d("GUS", "Let's clear the data...")

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