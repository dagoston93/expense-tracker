package com.diamont.expense.tracker.manageCategoriesFragment

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
import com.diamont.expense.tracker.addCategoryDialogFragment.AddCategoryDialogFragment
import com.diamont.expense.tracker.databinding.FragmentManageCategoriesBinding
import com.diamont.expense.tracker.historyFragment.HistoryFragmentViewModel
import com.diamont.expense.tracker.historyFragment.HistoryFragmentViewModelFactory
import com.diamont.expense.tracker.util.database.TransactionCategory
import com.diamont.expense.tracker.util.database.TransactionCategoryRecyclerViewAdapter
import com.diamont.expense.tracker.util.database.TransactionDatabase
import com.diamont.expense.tracker.util.database.TransactionRecyclerViewAdapter

class ManageCategoriesFragment : Fragment() {
    /** Data binding and view model */
    private lateinit var binding : FragmentManageCategoriesBinding
    private lateinit var viewModel: ManageCategoriesFragmentViewModel

    /** Get the Activity View Model */
    private val activityViewModel : MainActivityViewModel by activityViewModels {
        MainActivityViewModelFactory(
            requireNotNull(this.activity).application
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /** Inflate the layout for this fragment */
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_manage_categories, container, false)
        binding.lifecycleOwner = this

        /**
         *  Create the view model using a view model factory
         */
        val application = requireNotNull(this.activity).application
        val databaseDao = TransactionDatabase.getInstance(application).transactionDatabaseDao
        val viewModelFactory = ManageCategoriesFragmentViewModelFactory(application, databaseDao)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(ManageCategoriesFragmentViewModel::class.java)

        /** Set up values for activity view model */
        activityViewModel.setTitle(getString(R.string.manage_categories))
        activityViewModel.setBottomNavBarVisibility(false)
        activityViewModel.setUpButtonVisibility(true)
        activityViewModel.setDrawerLayoutEnabled(false)

        /** Set up the recycler view with the adapter */
        val adapter = TransactionCategoryRecyclerViewAdapter(
            { id, position -> AddCategoryDialogFragment(id) {
                (binding.rvTransactionCategoryList.adapter as TransactionCategoryRecyclerViewAdapter).notifyItemChanged(position)
                //TODO doesn't update recycler view
            }.show(childFragmentManager, AddCategoryDialogFragment.TAG)},
            {id, name, position -> }
        )

        binding.rvTransactionCategoryList.adapter = adapter

        /** Observe the data and refresh recycler view if it changes */
        viewModel.categories.observe(viewLifecycleOwner, Observer {
            it?.let{
                adapter.categories = it.toMutableList()
            }
        })

        /** Return the inflated layout */
        return binding.root
    }
}