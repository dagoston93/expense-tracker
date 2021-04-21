package com.diamont.expense.tracker.statisticFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.diamont.expense.tracker.MainActivityViewModel
import com.diamont.expense.tracker.MainActivityViewModelFactory
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.databinding.FragmentStatisticBinding
import com.diamont.expense.tracker.util.*
import com.diamont.expense.tracker.util.database.TransactionDatabase
import com.diamont.expense.tracker.util.enums.TransactionType
import java.text.DecimalFormat


class StatisticFragment : DateRangeSelectorFragment() {
    /** Data binding and view model */
    private lateinit var binding : FragmentStatisticBinding
    override lateinit var baseClassViewModel: DateRangeSelectorFragmentViewModel
    private lateinit var viewModel: StatisticFragmentViewModel

    /** Get the Activity View Model */
    private val activityViewModel : MainActivityViewModel by activityViewModels {
        MainActivityViewModelFactory(
            requireNotNull(this.activity).application
        )
    }

    /** Array adapter for statistic types */
    private lateinit var statisticTypeAdapter : ArrayAdapter<String>

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

        baseClassViewModel = ViewModelProvider(this, viewModelFactory)
            .get(StatisticFragmentViewModel::class.java)

        viewModel = baseClassViewModel as StatisticFragmentViewModel

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

        /**
         * Add text changed listener for the period dropdown
         */
        binding.actvStatisticPeriod.addTextChangedListener {
            val idx = binding.actvStatisticPeriod.getStringListIndexFromText(viewModel.periodStringList.value ?: listOf<String>())

            onDateRangeSelected(idx, binding.actvStatisticPeriod)
        }

        /**
         * Observe period string list
         */
        viewModel.periodStringList.observe(viewLifecycleOwner, Observer {
            periodAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, it)
            binding.actvStatisticPeriod.setAdapter(periodAdapter)
            binding.actvStatisticPeriod.setText(periodAdapter.getItem(0).toString(), false)
        })

        /**
         * Observe statistic type string list
         */
        viewModel.statisticTypeStringList.observe(viewLifecycleOwner, Observer {
            statisticTypeAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, it)
            binding.actvStatisticType.setAdapter(statisticTypeAdapter)
            binding.actvStatisticType.setText(statisticTypeAdapter.getItem(0).toString(), false)
        })

        /** Return the inflated layout */
        return binding.root
    }

    /**
     * We fill the dropdown menus in onResume() so that
     * if device configuration is changed we don't loose the
     * items from the menu
     */
    override fun onResume() {
        binding.actvStatisticPeriod.setAdapter(periodAdapter)
        binding.actvStatisticType.setAdapter(statisticTypeAdapter)

        super.onResume()
    }


}