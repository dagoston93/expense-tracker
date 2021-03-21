package com.diamont.expense.tracker

/**
 * The only responsibility of this fragment is to navigate to
 * the correct initial screen:
 * - Initial setup screen
 * - The main app
 * - Or to the authentication
 *
 * */
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

class StartupFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /** Navigate */
        (activity as MainActivity).findNavController(R.id.mainNavHostFragment).navigate(determineDestination())

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_startup, container, false)
    }

    /**
     * This method returns the required navigation destination
     */
    private fun determineDestination(): NavDirections {
        /** Is initial setup done? */
        if (!(activity as MainActivity).isInitialSetupDone) {
            return StartupFragmentDirections.actionMainFragmentToInitialSetupFragment()
        }

        /** If initial setup is done check if we need authentication or not */
        return if ((activity as MainActivity).isAuthenticationRequired) {
            StartupFragmentDirections.actionMainFragmentToAuthenticationFragment()
        } else {
            StartupFragmentDirections.actionMainFragmentToMainAppFragment()
        }
    }
}