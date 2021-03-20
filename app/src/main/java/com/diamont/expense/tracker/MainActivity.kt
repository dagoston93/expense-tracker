package com.diamont.expense.tracker

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.PreferenceManager
import androidx.databinding.DataBindingUtil
import com.diamont.expense.tracker.databinding.ActivityMainBinding
import com.diamont.expense.tracker.util.*

class MainActivity : AppCompatActivity() {

    /**
     * Declare some objects we will need later
     */
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences : SharedPreferences

    /**
     * Preferences
     * Encapsulate the data
     */
    private var _isAuthenticationRequired : Boolean = false
    val isAuthenticationRequired : Boolean
        get() = _isAuthenticationRequired

    private var _isFingerprintEnabled : Boolean = false
    val isFingerprintEnabled : Boolean
        get() = _isFingerprintEnabled

    private var _isInitialSetupDone : Boolean = false
    val isInitialSetupDone : Boolean
        get() = _isInitialSetupDone

    private var _pinCode : String = ""
    val pinCode : String
        get() = _pinCode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**  Inflate the layout using data binding */
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        /** Set up toolbar */
        setSupportActionBar(binding.toolbar)

        /** Load shared preferences */
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        /** ONLY FOR TEST*/
        clearPrefsForTest()
        /** ONLY FOR TEST*/

        _isAuthenticationRequired = sharedPreferences.getBoolean(KEY_PREF_AUTHENTICATION_REQUIRED, false)
        _isFingerprintEnabled = sharedPreferences.getBoolean(KEY_PREF_FINGERPRINT_ENABLED, false)
        _isInitialSetupDone = sharedPreferences.getBoolean(KEY_PREF_INITIAL_SETUP_DONE, false)
        _pinCode = sharedPreferences.getString(KEY_PREF_PIN_CODE, "") ?: ""

    }

    private fun clearPrefsForTest(){
        with(sharedPreferences.edit()) {
            putBoolean(KEY_PREF_INITIAL_SETUP_DONE, false)
            putBoolean(KEY_PREF_AUTHENTICATION_REQUIRED, false)
            putBoolean(KEY_PREF_FINGERPRINT_ENABLED, false)
            putString(KEY_PREF_PIN_CODE, pinCode)
            putInt(KEY_PREF_CURRENCY_ID, 0)
            putFloat(KEY_PREF_INITIAL_CARD, 0f)
            putFloat(KEY_PREF_INITIAL_CASH, 0f)

            apply()
        }
    }
}