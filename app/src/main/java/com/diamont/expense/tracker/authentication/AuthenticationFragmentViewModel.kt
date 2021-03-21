package com.diamont.expense.tracker.authentication

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.preference.PreferenceManager
import com.diamont.expense.tracker.util.KEY_PREF_PIN_CODE

class AuthenticationFragmentViewModel(appContext: Application) : AndroidViewModel(appContext){
    /** We need shared prefs */
    private var sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext)

    /** Declare some variables */
    private var pinCode : String = ""

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

    /**
     * Constructor
     */
    init {
        /** Load pin code */
        pinCode = sharedPreferences.getString(KEY_PREF_PIN_CODE, "") ?: ""
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