package com.diamont.expense.tracker.initialSetup

import android.app.Application
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel

class InitialSetupFragmentViewModel(private val appContext: Application) : AndroidViewModel(appContext) {

    /** Declare the required variables */
    private var isAuthenticationRequired : Boolean = false
    private var isFingerprintEnabled : Boolean = false
    private var isFingerprintSensorAvailable : Boolean = false
    private var pinCode : String = ""


    /**
     * Call this method from the fragments
     * if the user turns the authentication on
     */
    fun setAuthenticationRequired(){
        isAuthenticationRequired = true
    }
}