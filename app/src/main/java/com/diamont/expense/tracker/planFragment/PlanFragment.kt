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
                    viewModel.selectedTabChanged(tab.position)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        /** Set up the recycler view with the adapter */
        val adapter = PlanRecyclerViewAdapter(binding.rvPlanList,
            viewModel.decimalFormat ?: DecimalFormat(),
            { id ->
                //viewModel.eventNavigateToEditFragment.value = id
            },
            {id, description, typeStringId, date, position ->
                //confirmDeleteTransaction(id, description, typeStringId, date, position)
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

        /** Return the inflated layout */
        return binding.root
    }
}