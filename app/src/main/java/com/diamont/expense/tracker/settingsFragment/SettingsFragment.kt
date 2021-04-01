package com.diamont.expense.tracker.settingsFragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.diamont.expense.tracker.MainActivityViewModel
import com.diamont.expense.tracker.MainActivityViewModelFactory
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.addCategoryDialogFragment.AddCategoryDialogFragmentViewModel
import com.diamont.expense.tracker.addCategoryDialogFragment.AddCategoryDialogFragmentViewModelFactory
import com.diamont.expense.tracker.databinding.FragmentSettingsBinding
import com.diamont.expense.tracker.util.database.TransactionDatabase
import com.diamont.expense.tracker.util.interfaces.BackPressCallbackFragment


class SettingsFragment: Fragment(), BackPressCallbackFragment {
    /** Data binding and view model*/
    private lateinit var binding : FragmentSettingsBinding
    private lateinit var viewModel: SettingsFragmentViewModel

    /** Get the Activity View Model */
    private val activityViewModel : MainActivityViewModel by activityViewModels {
        MainActivityViewModelFactory(
            requireNotNull(this.activity).application
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /** Inflate the layout for this fragment */
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        binding.lifecycleOwner = this

        /**
         *  Create the view model using a view model factory
         */
        val application = requireNotNull(this.activity).application
        val viewModelFactory = SettingsFragmentViewModelFactory(application, activityViewModel.sharedPreferences)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(SettingsFragmentViewModel::class.java)

        /** Set up values for activity view model */
        activityViewModel.setTitle(getString(R.string.settings))
        activityViewModel.setBottomNavBarVisibility(false)
        activityViewModel.setUpButtonVisibility(true)
        activityViewModel.setDrawerLayoutEnabled(false)

        /**
         * Add onClickListeners for the switches
         */
        binding.swSettingsAuthentication.setOnClickListener {
            viewModel.onAuthenticationSwitchClicked(binding.swSettingsAuthentication.isChecked)
        }

        binding.swSettingsFingerprint.setOnClickListener {
            viewModel.onFingerprintSwitchClicked((binding.swSettingsFingerprint.isChecked))
        }

        /**
         * Observe the settings
         */
        viewModel.isAuthenticationRequired.observe(viewLifecycleOwner, Observer {
            if(it != null){
                binding.swSettingsAuthentication.isChecked = it
            }
        })

        viewModel.isFingerprintEnabled.observe(viewLifecycleOwner, Observer {
            if(it != null){
                binding.swSettingsFingerprint.isChecked = it
            }
        })

        /** Return the inflated layout */
        return binding.root
    }

    /**
     * This method handles the back button press
     */
    override fun onBackPressed(listener: () -> Unit): Boolean {
        listener()
        return true
    }

}