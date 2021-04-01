package com.diamont.expense.tracker.settingsFragment

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diamont.expense.tracker.util.KEY_PREF_AUTHENTICATION_REQUIRED
import com.diamont.expense.tracker.util.KEY_PREF_FINGERPRINT_ENABLED

class SettingsFragmentViewModel(
    private val appContext: Application,
    private val sharedPreferences: SharedPreferences
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

    /**
     * Constructor
     */
    init{
        _isAuthenticationRequired.value = sharedPreferences.getBoolean(KEY_PREF_AUTHENTICATION_REQUIRED, false)
        _isFingerprintEnabled.value = sharedPreferences.getBoolean(KEY_PREF_FINGERPRINT_ENABLED, false)
    }

    /**
     * Call this method if user switches the authentication switch
     */
    fun onAuthenticationSwitchClicked(isTurnedOn: Boolean){
        /** Save the new state */
        with(sharedPreferences.edit()){
            putBoolean(KEY_PREF_AUTHENTICATION_REQUIRED, isTurnedOn)
            apply()
        }
    }

    /**
     * Call this method if user switches the fingerprint switch
     */
    fun onFingerprintSwitchClicked(isTurnedOn: Boolean){
        /** Save the new state */
        with(sharedPreferences.edit()){
            putBoolean(KEY_PREF_FINGERPRINT_ENABLED, isTurnedOn)
            apply()
        }
    }

}