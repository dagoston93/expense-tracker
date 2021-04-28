package com.diamont.expense.tracker.homeFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.diamont.expense.tracker.MainActivityViewModel
import com.diamont.expense.tracker.MainActivityViewModelFactory
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.databinding.FragmentHomeBinding
import com.diamont.expense.tracker.util.database.TransactionDatabase
import com.diamont.expense.tracker.util.enums.TransactionType


class HomeFragment : Fragment() {
    /** Data binding and view model */
    private lateinit var binding : FragmentHomeBinding
    private lateinit var viewModel: HomeFragmentViewModel


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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding.lifecycleOwner = this

        /**
         *  Create the view model using a view model factory
         */
        val application = requireNotNull(this.activity).application
        val databaseDao = TransactionDatabase.getInstance(application).transactionDatabaseDao
        val viewModelFactory = HomeFragmentViewModelFactory(databaseDao, activityViewModel.sharedPreferences)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(HomeFragmentViewModel::class.java)
        binding.viewModel = viewModel

        /** Set up values for activity view model */
        activityViewModel.setTitle(getString(R.string.app_name))
        activityViewModel.setBottomNavBarVisibility(true)
        activityViewModel.setUpButtonVisibility(false)
        activityViewModel.setDrawerLayoutEnabled(true)
        activityViewModel.defaultTransactionType = TransactionType.EXPENSE

        /** Return the inflated layout */
        return binding.root
    }


}