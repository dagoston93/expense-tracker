package com.diamont.expense.tracker.historyFragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
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
import com.diamont.expense.tracker.util.Currency
import com.diamont.expense.tracker.util.KEY_PREF_CURRENCY_ID
import com.diamont.expense.tracker.util.database.*
import com.diamont.expense.tracker.util.enums.TransactionType
import com.diamont.expense.tracker.util.getStringListIndexFromText
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.DecimalFormat

class HistoryFragment : Fragment() {
    /** Data binding */
    private lateinit var binding : FragmentHistoryBinding
    private lateinit var viewModel: HistoryFragmentViewModel

    /** Array adapter for period list */
    private lateinit var periodAdapter : ArrayAdapter<String>
    private var previousSelectedIndex: Int? = 0
    private var previousSelectedStartDate: Long = 0
    private var previousSelectedEndDate: Long = 0


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

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(HistoryFragmentViewModel::class.java)

        /** Set up values for activity view model */
        activityViewModel.setTitle(getString(R.string.history))
        activityViewModel.setBottomNavBarVisibility(true)
        activityViewModel.setUpButtonVisibility(false)
        activityViewModel.setDrawerLayoutEnabled(true)
        activityViewModel.defaultTransactionType = TransactionType.EXPENSE

        /**
         * Get the decimal format the currency
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
        binding.actvHistoryInterval.addTextChangedListener {
            val idx = binding.actvHistoryInterval.getStringListIndexFromText(viewModel.periodStringList.value ?: listOf<String>())

            if(idx != previousSelectedIndex) {

                if (idx == viewModel.periodStringList.value?.size!! - 1) {
                    /**
                     * Create the date picker
                     */
                    val datePickerBuilder =
                        MaterialDatePicker.Builder.dateRangePicker()
                            .setTitleText(resources.getString(R.string.select_period))

                    /**
                     * If user has selected a date range before, we remember the selection
                     */
                    if(previousSelectedStartDate != 0L && previousSelectedEndDate != 0L){
                        datePickerBuilder.setSelection(
                            Pair(
                                previousSelectedStartDate,
                                previousSelectedEndDate
                            )
                        )
                    }

                    val datePicker = datePickerBuilder.build()

                    /**
                     * OnClickListener for the date picker OK button
                     */
                    datePicker.addOnPositiveButtonClickListener {
                        viewModel.onDateRangeSelected(it.first, it.second)
                        previousSelectedStartDate = it.first ?: 0
                        previousSelectedEndDate = it.second ?: 0

                        val rangeString = "${formatDate(previousSelectedStartDate)} - ${formatDate(previousSelectedEndDate)}"
                        binding.actvHistoryInterval.setText(rangeString, false)
                    }

                    datePicker.addOnCancelListener {
                        resetSelection()
                    }

                    datePicker.addOnNegativeButtonClickListener {
                        resetSelection()
                    }

                    datePicker.show(childFragmentManager, "")

                    Log.d("GUS", "Show date range picker...")
                } else {
                    previousSelectedIndex = idx
                    viewModel.onPeriodDropdownItemSelected(idx)
                }
            }
        }

        /** Observe the data and refresh recycler view if it changes */
        viewModel.transactionDataToDisplay.observe(viewLifecycleOwner, Observer {
            it?.let{
                adapter.categories = viewModel.categories.value ?: listOf<TransactionCategory>()
                adapter.plans = viewModel.plans.value ?: listOf<Plan>()
                adapter.items = it.toMutableList()
                Log.d("GUS", "new list...")
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
            binding.actvHistoryInterval.setAdapter(periodAdapter)
            binding.actvHistoryInterval.setText(periodAdapter.getItem(0).toString(), false)
        })

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
     * Call this method if material date picker was cancelled
     */
    private fun resetSelection(){
        /** If previous index is not null we need to select that item*/
        if(previousSelectedIndex!= null) {
            binding.actvHistoryInterval.setText(periodAdapter.getItem(previousSelectedIndex!!).toString(), false)
        }else{
            /** If it is null, we need to set the text to the prev. date range */
            val rangeString = "${formatDate(previousSelectedStartDate)} - ${formatDate(previousSelectedEndDate)}"
            binding.actvHistoryInterval.setText(rangeString, false)
        }
    }

    /**
     * Call this method to format a date
     */
    private fun formatDate(date: Long): String{
        val dateFormat = android.text.format.DateFormat.getDateFormat(context)
        return dateFormat.format(date)
    }

    /**
     * We fill the dropdown menus in onResume() so that
     * if device configuration is changed we don't loose the
     * items from the menu
     */
    override fun onResume() {
        binding.actvHistoryInterval.setAdapter(periodAdapter)

        super.onResume()
    }

}