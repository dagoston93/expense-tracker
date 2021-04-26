package com.diamont.expense.tracker.historyFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.util.Pair
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.diamont.expense.tracker.MainActivityViewModel
import com.diamont.expense.tracker.MainActivityViewModelFactory
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.databinding.FragmentHistoryBinding
import com.diamont.expense.tracker.historyFragment.filterDialogFragment.FilterDialogFragment
import com.diamont.expense.tracker.util.*
import com.diamont.expense.tracker.util.database.*
import com.diamont.expense.tracker.util.enums.TransactionType
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.DecimalFormat

class HistoryFragment : DateRangeSelectorFragment() {
    /** Data binding and view model */
    private lateinit var binding : FragmentHistoryBinding
    override lateinit var baseClassViewModel: DateRangeSelectorFragmentViewModel
    private lateinit var viewModel: HistoryFragmentViewModel

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_history, container, false)
        binding.lifecycleOwner = this

        /**
         *  Create the view model using a view model factory
         */
        val application = requireNotNull(this.activity).application
        val databaseDao = TransactionDatabase.getInstance(application).transactionDatabaseDao
        val viewModelFactory = HistoryFragmentViewModelFactory(application, databaseDao)

        baseClassViewModel = ViewModelProvider(this, viewModelFactory)
            .get(HistoryFragmentViewModel::class.java)

        viewModel = baseClassViewModel as HistoryFragmentViewModel

        /** Set up values for activity view model */
        activityViewModel.setTitle(getString(R.string.history))
        activityViewModel.setBottomNavBarVisibility(true)
        activityViewModel.setUpButtonVisibility(false)
        activityViewModel.setDrawerLayoutEnabled(true)
        activityViewModel.defaultTransactionType = TransactionType.EXPENSE

        /**
         * Get the decimal format of the currency
         */
        val currencyId = activityViewModel.sharedPreferences.getInt(KEY_PREF_CURRENCY_ID, 0)
        val decimalFormat = Currency.getDecimalFormat(currencyId) ?: DecimalFormat()

        /** Set up the recycler view with the adapter */
        val adapter = TransactionRecyclerViewAdapter(binding.rvTransactionList,
            decimalFormat,
            binding.tvHistoryNoTransactions,
            { id ->
                viewModel.eventNavigateToEditFragment.value = id
            },
            {id, description, typeStringId, date, position ->
                confirmDeleteTransaction(id, description, typeStringId, date, position)
            }
        )

        binding.rvTransactionList.adapter = adapter

        /** Turn the blinking animation on item change off */
        binding.rvTransactionList.itemAnimator = null

        /**
         * Add text changed listener for the period dropdown
         */
        binding.actvHistoryPeriod.addTextChangedListener {
            val idx = binding.actvHistoryPeriod.getStringListIndexFromText(viewModel.periodStringList.value ?: listOf<String>())

            onDateRangeSelected(idx, binding.actvHistoryPeriod)
        }

        /** Observe the data and refresh recycler view if it changes */
        viewModel.transactionDataToDisplay.observe(viewLifecycleOwner, Observer {
            it?.let{
                adapter.categories = viewModel.categories.value ?: listOf<TransactionCategory>()
                adapter.plans = viewModel.plans.value ?: listOf<Plan>()
                adapter.items = it.toMutableList()
            }
        })

        /**
         * Observe the edit navigation event
         */
        viewModel.eventNavigateToEditFragment.observe(viewLifecycleOwner, Observer {
            if(it != null){
                activityViewModel.isTransactionToEdit = true
                activityViewModel.eventNavigateToEditFragment.value = it
                viewModel.eventNavigateToEditFragment.value = null
            }
        })

        /**
         * Observe period string list
         */
        viewModel.periodStringList.observe(viewLifecycleOwner, Observer {
            periodAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, it)
            binding.actvHistoryPeriod.setAdapter(periodAdapter)
            binding.actvHistoryPeriod.setText(periodAdapter.getItem(0).toString(), false)
        })

        /**
         * Add onClickListener to filter icon
         */
        binding.ivHistoryFilter.setOnClickListener {
            FilterDialogFragment(
                viewModel.categories.value!!,
                viewModel.filterTransactionTypes,
                viewModel.filterCategoryIds
            ){filteredTransactionTypes, filteredCategoryIds, isFilterApplied ->
                /** Pass filters to view model */
                viewModel.onFiltersSelected(filteredTransactionTypes, filteredCategoryIds)

                /** Change image view resource */
                binding.ivHistoryFilter.setImageResource(
                    if(isFilterApplied){
                        R.drawable.ic_filter_filled
                    }else{
                        R.drawable.ic_filter_outline
                    }
                )
            }.show(childFragmentManager, FilterDialogFragment.TAG)
        }

        /** Return the inflated layout */
        return binding.root
    }

    /**
     * Call this method to show the confirm delete dialog
     */
    private fun confirmDeleteTransaction(transactionId: Int, description: String, typeStringId: Int, date: String, position: Int){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.confirm_delete_dialog_title))
            .setMessage(resources.getString(R.string.confirm_delete_transaction_dialog_text,
                description,
                resources.getString(typeStringId),
                date
            ))
            .setNegativeButton(resources.getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(resources.getString(R.string.delete)) { _, _ ->
                viewModel.deleteTransaction(transactionId)
                (binding.rvTransactionList.adapter as TransactionRecyclerViewAdapter).itemDeletedAtPos(position)
            }
            .show()
    }

    /**
     * We fill the dropdown menus in onResume() so that
     * if device configuration is changed we don't loose the
     * items from the menu
     */
    override fun onResume() {
        binding.actvHistoryPeriod.setAdapter(periodAdapter)

        super.onResume()
    }

}