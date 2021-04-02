package com.diamont.expense.tracker.settingsFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.TextViewCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.diamont.expense.tracker.MainActivityViewModel
import com.diamont.expense.tracker.MainActivityViewModelFactory
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.databinding.FragmentSettingsBinding
import com.diamont.expense.tracker.settingsFragment.changePinDialogFragment.ChangeOrConfirmPinDialogFragment
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

    /**
     * onCreateView()
     */
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
        binding.viewModel = viewModel


        /** Set up values for activity view model */
        activityViewModel.setTitle(getString(R.string.settings))
        activityViewModel.setBottomNavBarVisibility(false)
        activityViewModel.setUpButtonVisibility(true)
        activityViewModel.setDrawerLayoutEnabled(false)

        /**
         * Add onClickListeners for the switches
         */
        binding.swSettingsAuthentication.setOnClickListener {
            /** We only turn on authentication after user enters the pin */
            if(binding.swSettingsAuthentication.isChecked){
                binding.swSettingsAuthentication.isChecked = false
                ChangeOrConfirmPinDialogFragment(
                    activityViewModel.sharedPreferences,
                    true
                ){
                    viewModel.onAuthenticationSwitchClicked(true)
                    binding.swSettingsAuthentication.isChecked = true
                }.show(childFragmentManager, ChangeOrConfirmPinDialogFragment.TAG)
            }else{
                /** If turning off we don't need to confirm pin */
                viewModel.onAuthenticationSwitchClicked(false)
            }
        }

        binding.swSettingsFingerprint.setOnClickListener {
            viewModel.onFingerprintSwitchClicked((binding.swSettingsFingerprint.isChecked))
        }

        /**
         * Observe the settings
         */
        viewModel.isAuthenticationRequired.observe(viewLifecycleOwner, Observer {
            if(it != null){
                /** First change state of the switch */
                if(binding.swSettingsAuthentication.isChecked != it) {
                    binding.swSettingsAuthentication.isChecked = it
                }

                /**
                 * Change the text style of the fingerprint and change pin options
                 * and enable/disable the switch
                 * */
                if(it){
                    TextViewCompat.setTextAppearance(binding.tvSettingsFingerprintTitle, R.style.Theme_ExpenseTracker_TextAppearance_SettingsTitle)
                    TextViewCompat.setTextAppearance(binding.tvSettingsFingerprintDescription, R.style.Theme_ExpenseTracker_TextAppearance_SettingsDescription)
                    TextViewCompat.setTextAppearance(binding.tvSettingsChangePinTitle, R.style.Theme_ExpenseTracker_TextAppearance_SettingsTitle)
                    TextViewCompat.setTextAppearance(binding.tvSettingsChangePinDescription, R.style.Theme_ExpenseTracker_TextAppearance_SettingsDescription)

                    binding.swSettingsFingerprint.isEnabled = true
                }else{
                    TextViewCompat.setTextAppearance(binding.tvSettingsFingerprintTitle, R.style.Theme_ExpenseTracker_TextAppearance_SettingsTitleDisabled)
                    TextViewCompat.setTextAppearance(binding.tvSettingsFingerprintDescription, R.style.Theme_ExpenseTracker_TextAppearance_SettingsDescriptionDisabled)
                    TextViewCompat.setTextAppearance(binding.tvSettingsChangePinTitle, R.style.Theme_ExpenseTracker_TextAppearance_SettingsTitleDisabled)
                    TextViewCompat.setTextAppearance(binding.tvSettingsChangePinDescription, R.style.Theme_ExpenseTracker_TextAppearance_SettingsDescriptionDisabled)

                    binding.swSettingsFingerprint.isEnabled = false
                }
            }
        })

        viewModel.isFingerprintEnabled.observe(viewLifecycleOwner, Observer {
            if(it != null){
                /** Change state of the switch */
                if(binding.swSettingsFingerprint.isChecked != it) {
                    binding.swSettingsFingerprint.isChecked = it
                }
            }
        })

        /** Set the onClickListener for the Change Pin option */
        binding.clSettingsChangePin.setOnClickListener {
            /** Only execute if enabled */
            if(binding.swSettingsAuthentication.isChecked){
                ChangeOrConfirmPinDialogFragment(activityViewModel.sharedPreferences).show(childFragmentManager, ChangeOrConfirmPinDialogFragment.TAG)
            }
        }

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