package com.diamont.expense.tracker.planFragment

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
import com.diamont.expense.tracker.databinding.FragmentPlanBinding
import com.diamont.expense.tracker.util.database.*
import com.diamont.expense.tracker.util.enums.TransactionType
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import java.text.DecimalFormat

class PlanFragment : Fragment() {
    /** Data binding and view model */
    private lateinit var binding : FragmentPlanBinding
    private lateinit var viewModel: PlanFragmentViewModel

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_plan, container, false)
        binding.lifecycleOwner = this

        /** Set up values for activity view model */
        activityViewModel.setTitle(getString(R.string.plan))
        activityViewModel.setBottomNavBarVisibility(true)
        activityViewModel.setUpButtonVisibility(false)
        activityViewModel.setDrawerLayoutEnabled(true)
        activityViewModel.defaultTransactionType = TransactionType.PLAN_EXPENSE

        /**
         *  Create the view model using a view model factory
         */
        val application = requireNotNull(this.activity).application
        val databaseDao = TransactionDatabase.getInstance(application).transactionDatabaseDao
        val viewModelFactory = PlanFragmentViewModelFactory(application, databaseDao, activityViewModel.sharedPreferences)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(PlanFragmentViewModel::class.java)
        binding.viewModel = viewModel

        /**
         * Add tabChangeListener
         */
        binding.tlPlanTabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab != null){
                    viewModel.onSelectedTabChanged(tab.position)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        /** Set up the recycler view with the adapter */
        val adapter = PlanRecyclerViewAdapter(binding.rvPlanList,
            viewModel.decimalFormat ?: DecimalFormat(),
            binding.tvPlansNoTransactions,
            { id ->
               viewModel.eventNavigateToEditFragment.value = id
            },
            {id, description, typeStringId, dateLabel, date, position ->
                confirmDeletePlan(id, description, typeStringId, dateLabel, date, position)
            },
            { id, description, typeStringId, date, position ->
                confirmCancelPlan(id, description, typeStringId, date, position)
            }
        )

        binding.rvPlanList.adapter = adapter

        /** Turn the blinking animation on item change off */
        binding.rvPlanList.itemAnimator = null

        /** Observe the data and refresh recycler view if it changes */
        viewModel.plansToDisplay.observe(viewLifecycleOwner, Observer {
            it?.let{
                adapter.categories = viewModel.categories.value ?: listOf<TransactionCategory>()
                adapter.items = it.toMutableList()
            }
        })

        /**
         * Observe the selected plan type
         */
        viewModel.selectedPlanType.observe(viewLifecycleOwner, Observer {
            activityViewModel.defaultTransactionType = it
        })

        /**
         * Observe the edit navigation event
         */
        viewModel.eventNavigateToEditFragment.observe(viewLifecycleOwner, Observer {
            if(it != null){
                activityViewModel.isTransactionToEdit = false
                activityViewModel.eventNavigateToEditFragment.value = it
                viewModel.eventNavigateToEditFragment.value = null
            }
        })

        /** Return the inflated layout */
        return binding.root
    }

    /**
     * This method shows the confirmation dialog and deletes a plan
     */
    private fun confirmDeletePlan(id: Int, description: String, typeStringId: Int, dateLabel: String, date: String, position: Int){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.confirm_delete_dialog_title))
            .setMessage(resources.getString(R.string.confirm_delete_plan_dialog_text,
                description,
                resources.getString(typeStringId),
                dateLabel,
                date
            ))
            .setNegativeButton(resources.getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(resources.getString(R.string.delete)) { _, _ ->
                viewModel.deletePlan(id, position)
                (binding.rvPlanList.adapter as PlanRecyclerViewAdapter).itemDeletedAtPos(position)
            }
            .show()
    }

    /**
     * This method shows the confirmation dialog and cancels a plan
     */
    private fun confirmCancelPlan(id: Int, description: String, typeStringId: Int, date: String, position: Int){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.confirm_delete_dialog_title))
            .setMessage(resources.getString(R.string.confirm_cancel_plan_dialog_text,
                description,
                resources.getString(typeStringId),
                date
            ))
            .setNegativeButton(resources.getString(R.string.no)) { _, _ -> }
            .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                viewModel.cancelPlan(id, position)
                //(binding.rvPlanList.adapter as PlanRecyclerViewAdapter).itemCancelledAtPos(position)
            }
            .show()
    }

}