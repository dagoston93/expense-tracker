package com.diamont.expense.tracker

/**
 * The only responsibility of this fragment is to navigate to
 * the correct initial screen:
 * - Initial setup screen
 * - The main app
 * - Or to the authentication
 *
 * */
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import com.diamont.expense.tracker.util.*

class StartupFragment : Fragment() {

    /** We need the shared preferences to determine the initial nav destination */
    private lateinit var sharedPreferences: SharedPreferences

    /** Declaring some variables */
    private var isAuthenticationRequired: Boolean = false
    private var isFingerprintEnabled: Boolean = false
    private var isInitialSetupDone: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /** Load shared preferences */
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)

        //clearPrefsForTest()

        isAuthenticationRequired = sharedPreferences.getBoolean(KEY_PREF_AUTHENTICATION_REQUIRED, false)
        isFingerprintEnabled = sharedPreferences.getBoolean(KEY_PREF_FINGERPRINT_ENABLED, false)
        isInitialSetupDone = sharedPreferences.getBoolean(KEY_PREF_INITIAL_SETUP_DONE, false)

        /** Navigate */
        (activity as MainActivity).findNavController(R.id.mainNavHostFragment).navigate(determineDestination())

        // Inflate the layout for this fragment just because we have to
        return inflater.inflate(R.layout.fragment_startup, container, false)
    }

    /**
     * This method returns the required navigation destination
     */
    private fun determineDestination(): NavDirections {
        /** Is initial setup done? */
        if (!isInitialSetupDone) {
            return StartupFragmentDirections.actionMainFragmentToInitialSetupFragment()
        }

        /** If initial setup is done check if we need authentication or not */
        return if (isAuthenticationRequired) {
            StartupFragmentDirections.actionMainFragmentToAuthenticationFragment()
        } else {
            StartupFragmentDirections.actionMainFragmentToMainAppFragment()
        }
    }

    /** TEST METHODS */
    private fun clearPrefsForTest() {
        with(sharedPreferences.edit()) {
            //putBoolean(KEY_PREF_INITIAL_SETUP_DONE, false)
            putBoolean(KEY_PREF_AUTHENTICATION_REQUIRED, false)
            //putBoolean(KEY_PREF_FINGERPRINT_ENABLED, false)
            //putString(KEY_PREF_PIN_CODE, "")
            //putInt(KEY_PREF_CURRENCY_ID, 0)
            //putFloat(KEY_PREF_INITIAL_CARD, 0f)
            //putFloat(KEY_PREF_INITIAL_CASH, 0f)

            apply()
        }
    }
}