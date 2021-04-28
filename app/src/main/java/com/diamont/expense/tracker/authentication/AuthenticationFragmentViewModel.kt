package com.diamont.expense.tracker.authentication

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.biometric.BiometricManager
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.diamont.expense.tracker.util.KEY_PREF_FINGERPRINT_ENABLED
import com.diamont.expense.tracker.util.KEY_PREF_PIN_CODE

class AuthenticationFragmentViewModel(
    private val context: Context
) : ViewModel(){
    /** We need shared prefs */
    private var sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private var isFingerprintSensorAvailable : Boolean = false

    /** Declare some variables */
    private var pinCode : String = ""
    private var isFingerprintEnabled : Boolean = false

    /** Some live data for the UI */
    private val _isErrorMessageVisible = MutableLiveData<Boolean>(false)
    val isErrorMessageVisible = Transformations.map(_isErrorMessageVisible){
        if(it){
            VISIBLE
        }else{
            INVISIBLE
        }
    }

    private val _isAuthenticationSuccessful = MutableLiveData<Boolean>(false)
    val isAuthenticationSuccessful : LiveData<Boolean>
        get() = _isAuthenticationSuccessful

    private var _showBiometricPrompt : Boolean = false
    val showBiometricPrompt : Boolean
        get() = _showBiometricPrompt

    /**
     * Constructor
     */
    init {
        /** Load pin code */
        pinCode = sharedPreferences.getString(KEY_PREF_PIN_CODE, "") ?: ""
        isFingerprintEnabled = sharedPreferences.getBoolean(KEY_PREF_FINGERPRINT_ENABLED, false)

        /** Check if biometric authentication is available */
        val biometricManager = BiometricManager.from(context)

        if(biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            == BiometricManager.BIOMETRIC_SUCCESS){
            isFingerprintSensorAvailable = true
        }

        /** Determine if we should show the biometric prompt */
        if(isFingerprintEnabled && isFingerprintSensorAvailable){
            _showBiometricPrompt = true
        }
    }

    /**
     * Call this method if user enters all 4 digits of the pin
     */
    fun onPinCodeEntered(codeEntered : String){
        /** If code is correct authentication is successful otherwise display error message */
        if(codeEntered == pinCode){
            _isAuthenticationSuccessful.value = true
        }else{
            _isAuthenticationSuccessful.value = false // Update always so the fragment will reset() the PinCodeInputView()
            _isErrorMessageVisible.value = true
        }
    }
}