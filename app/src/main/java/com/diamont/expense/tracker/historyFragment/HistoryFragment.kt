package com.diamont.expense.tracker.historyFragment

import android.os.Bundle
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
import com.diamont.expense.tracker.databinding.FragmentHistoryBinding
import com.diamont.expense.tracker.util.Currency
import com.diamont.expense.tracker.util.KEY_PREF_CURRENCY_ID
import com.diamont.expense.tracker.util.database.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.DecimalFormat

class HistoryFragment : Fragment() {
    /** Data binding */
    private lateinit var binding : FragmentHistoryBinding
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

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(HistoryFragmentViewModel::class.java)

        /** Set up values for activity view model */
        activityViewModel.setTitle(getString(R.string.history))
        activityViewModel.setBottomNavBarVisibility(true)
        activityViewModel.setUpButtonVisibility(false)
        activityViewModel.setDrawerLayoutEnabled(true)

        /**
         * Set the currency sign
         */
        val currencyId = activityViewModel.sharedPreferences.getInt(KEY_PREF_CURRENCY_ID, 0)
        val decimalFormat = Currency.getDecimalFormat(currencyId) ?: DecimalFormat()

        /** Set up the recycler view with the adapter */
        val adapter = TransactionRecyclerViewAdapter(binding.rvTransactionList,
            decimalFormat,
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

        /** Observe the data and refresh recycler view if it changes */
        viewModel.transactionData.observe(viewLifecycleOwner, Observer {
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
                activityViewModel.eventNavigateToEditFragment.value = it
                viewModel.eventNavigateToEditFragment.value = null
            }
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
                (binding.rvTransactionList.adapter as TransactionDetailViewAdapter<Transaction>).itemDeletedAtPos(position)
            }
            .show()
    }

}