package com.diamont.expense.tracker.initialSetup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.diamont.expense.tracker.MainActivityViewModel
import com.diamont.expense.tracker.MainActivityViewModelFactory
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.databinding.FragmentInitialSetupBinding
import com.diamont.expense.tracker.util.database.TransactionDatabase
import com.diamont.expense.tracker.util.interfaces.BackPressHandlerFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class InitialSetupFragment : Fragment(), BackPressHandlerFragment {

    /** Data binding */
    private lateinit var binding : FragmentInitialSetupBinding

    /** Get the Activity View Model */
    private val activityViewModel : MainActivityViewModel by activityViewModels {
        MainActivityViewModelFactory(
            requireNotNull(this.activity).application
        )
    }

    /** Get our View Model */
    private val viewModel : InitialSetupFragmentViewModel by activityViewModels {
        InitialSetupFragmentViewModelFactory(
            requireNotNull(this.activity).application,
            activityViewModel.sharedPreferences,
            TransactionDatabase.getInstance(requireNotNull(this.activity).application).transactionDatabaseDao
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /** Inflate the layout for this fragment */
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_initial_setup, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        /** Return the inflated layout */
        return binding.root
    }

    /** Handle the back button press */
    override fun onBackPressed(): Boolean {
        /** If we are not done we show the dialog */
        if(!viewModel.isSetupProcessComplete)
        {
            showConfirmationDialog()
        }else{
            /** If we are done we navigate to the main screen of the app */
            activity?.findNavController(R.id.mainNavHostFragment)?.navigate(
                InitialSetupFragmentDirections.actionInitialSetupFragmentToMainAppFragment()
            )
        }
        return true
    }

    /**
     * This method shows a dialog to confirm that
     * user wants to abandon initial setup process
     */
    private fun showConfirmationDialog(){
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle("Are you sure?")
                .setMessage("You need to complete these steps before start using the app. Are you sure you want to exit?")
                .setNegativeButton("Continue") { _,_ -> }
                .setPositiveButton("Exit") { _,_ -> this.activity?.finishAndRemoveTask()}
                .show()
        }
    }
}