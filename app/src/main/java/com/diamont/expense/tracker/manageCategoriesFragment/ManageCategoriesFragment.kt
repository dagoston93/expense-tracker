package com.diamont.expense.tracker.manageCategoriesFragment

import android.os.Bundle
import android.util.Log
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
import com.diamont.expense.tracker.util.database.TransactionCategoryListAdapter
import com.diamont.expense.tracker.util.database.TransactionDatabase
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
        val adapter = TransactionCategoryListAdapter(
            { id -> AddCategoryDialogFragment(id) {
                viewModel.getCategories()
                Log.d("GUS", "its called")
            }.show(childFragmentManager, AddCategoryDialogFragment.TAG)},
            {id, name, position ->
                confirmDeleteCategory(id, name, position)
            }
        )

        binding.rvTransactionCategoryList.adapter = adapter

        /** Observe the data and refresh recycler view if it changes */
        viewModel.categories.observe(viewLifecycleOwner, Observer {
            it?.let{
                adapter.submitList(it)
            }
        })

        /** Return the inflated layout */
        return binding.root
    }

    /**
     * Call this method to show the confirmation dialog for deleting a category
     */
    private fun confirmDeleteCategory(id: Int, name: String, position: Int){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.confirm_delete_dialog_title))
            .setMessage(resources.getString(R.string.confirm_delete_category_dialog_text, name))
            .setNegativeButton(resources.getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(resources.getString(R.string.delete)) { _, _ ->
                viewModel.deleteCategory(id)
                //(binding.rvTransactionCategoryList.adapter as TransactionCategoryRecyclerViewAdapter).itemDeletedAtPos(position)
            }
            .show()
    }
}