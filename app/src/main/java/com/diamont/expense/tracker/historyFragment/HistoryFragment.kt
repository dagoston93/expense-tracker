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
import com.diamont.expense.tracker.util.database.Transaction
import com.diamont.expense.tracker.util.database.TransactionAdapter
import com.diamont.expense.tracker.util.database.TransactionCategory
import com.diamont.expense.tracker.util.database.TransactionDatabase
import com.diamont.expense.tracker.util.enums.PaymentMethod
import com.diamont.expense.tracker.util.enums.TransactionFrequency
import com.diamont.expense.tracker.util.enums.TransactionPlanned
import com.diamont.expense.tracker.util.enums.TransactionType

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

        /** Set up the recycler view with the adapter */
        val adapter = TransactionAdapter(binding.rvTransactionList)
        binding.rvTransactionList.adapter = adapter

        /** Turn the blinking animation on item change off*/
        binding.rvTransactionList.itemAnimator = null

        /** Observe the data and refresh recycler view if it changes */
        viewModel.transactionData.observe(viewLifecycleOwner, Observer {
            it?.let{
                adapter.categories = viewModel.categories.value ?: listOf<TransactionCategory>()
                adapter.transactions = it
            }
        })

        /** Return the inflated layout */
        return binding.root
    }
    
}