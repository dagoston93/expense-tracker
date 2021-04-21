package com.diamont.expense.tracker.statisticFragment

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
import com.diamont.expense.tracker.databinding.FragmentStatisticBinding
import com.diamont.expense.tracker.util.Currency
import com.diamont.expense.tracker.util.DateRangeSelectorFragment
import com.diamont.expense.tracker.util.DateRangeSelectorFragmentViewModel
import com.diamont.expense.tracker.util.KEY_PREF_CURRENCY_ID
import com.diamont.expense.tracker.util.database.TransactionDatabase
import com.diamont.expense.tracker.util.enums.TransactionType
import java.text.DecimalFormat


class StatisticFragment : Fragment(), DateRangeSelectorFragment {
    /** Data binding and view model */
    private lateinit var binding : FragmentStatisticBinding
    override lateinit var interfaceViewModel: DateRangeSelectorFragmentViewModel
    private lateinit var viewModel: StatisticFragmentViewModel

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_statistic, container, false)
        binding.lifecycleOwner = this

        /**
         *  Create the view model using a view model factory
         */
        val application = requireNotNull(this.activity).application
        val databaseDao = TransactionDatabase.getInstance(application).transactionDatabaseDao
        val viewModelFactory = StatisticFragmentViewModelFactory(application, databaseDao)

        interfaceViewModel = ViewModelProvider(this, viewModelFactory)
            .get(StatisticFragmentViewModel::class.java)

        viewModel = interfaceViewModel as StatisticFragmentViewModel

        /** Set up values for activity view model */
        activityViewModel.setTitle(getString(R.string.statistics))
        activityViewModel.setBottomNavBarVisibility(true)
        activityViewModel.setUpButtonVisibility(false)
        activityViewModel.setDrawerLayoutEnabled(true)
        activityViewModel.defaultTransactionType = TransactionType.EXPENSE

        /**
         * Get the decimal format of the currency
         */
        val currencyId = activityViewModel.sharedPreferences.getInt(KEY_PREF_CURRENCY_ID, 0)
        val decimalFormat = Currency.getDecimalFormat(currencyId) ?: DecimalFormat()

        /** Return the inflated layout */
        return binding.root
    }


}