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
import com.diamont.expense.tracker.databinding.FragmentSetupCompleteBinding
import com.diamont.expense.tracker.util.database.TransactionDatabase


class SetupCompleteFragment : Fragment() {

    /** Data binding */
    private lateinit var binding: FragmentSetupCompleteBinding

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

    /**
     * onCreateView()
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /** Inflate the layout for this fragment */
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setup_complete, container, false)
        binding.lifecycleOwner = this

        /** Set the active page for the dot indicator */
        viewModel.setActivePage(6)

        /** Add onClickListener for the button */
        binding.btnSetupCompleteFinish.setOnClickListener {
            activity?.findNavController(R.id.mainNavHostFragment)?.navigate(
                InitialSetupFragmentDirections.actionInitialSetupFragmentToMainAppFragment()
            )
        }

        /** Return the inflated view */
        return binding.root
    }

}