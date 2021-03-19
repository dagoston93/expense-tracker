package com.diamont.expense.tracker.initialSetup

import android.app.Application
import android.content.Context.FINGERPRINT_SERVICE
import android.hardware.fingerprint.FingerprintManager
import android.opengl.Visibility
import android.os.Build
import android.util.Log
import android.view.View
import androidx.biometric.BiometricManager
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.*
import com.diamont.expense.tracker.R

class InitialSetupFragmentViewModel(private val appContext: Application) : AndroidViewModel(appContext) {

    /** Declare the required variables */
    private var isAuthenticationRequired : Boolean = false
    private var isFingerprintEnabled : Boolean = false
    private var pinCode : String = ""
    private var isFirstEntryAccepted : Boolean = false

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

    private var _isPinEntryErrorMessageVisible = MutableLiveData<Boolean>(false)
    val isPinEntryErrorMessageVisible = Transformations.map(_isPinEntryErrorMessageVisible){
        if(it){
            View.VISIBLE
        }else{
            View.INVISIBLE
        }
    }

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
     * Call this method if the  Set Pin Code / Confirm Pin Code button is clicked
     *
     * @param code - The value the user has entered in the PinCodeInputView
     */
    fun setPinButtonClicked(code : String){
        /** Check if we should Set or Confirm?*/
        if(isFirstEntryAccepted){
            /** Confirm so check if the two code matches */
            if(code == pinCode){
                /** Codes match, we are ready to continue */
                _isPinCodeSaved.value = true
            }else{
                /** Codes don't match, we set the error message and restart process */
                isFirstEntryAccepted = false
                _setOrConfirmPinStr.value = appContext.getString(R.string.set_pin_code)
                _isPinEntryErrorMessageVisible.value = true
            }
        }else{
            /** Set so we save the first entry and change text*/
            pinCode = code
            isFirstEntryAccepted = true
            _setOrConfirmPinStr.value = appContext.getString(R.string.confirm_pin_code)
            _isPinEntryErrorMessageVisible.value = false
        }
    }
}