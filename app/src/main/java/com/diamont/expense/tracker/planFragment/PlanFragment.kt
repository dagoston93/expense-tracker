package com.diamont.expense.tracker.planFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.diamont.expense.tracker.MainActivityViewModel
import com.diamont.expense.tracker.MainActivityViewModelFactory
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.databinding.FragmentPlanBinding

class PlanFragment : Fragment() {
    /** Data binding */
    private lateinit var binding : FragmentPlanBinding

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

        /** Return the inflated layout */
        return binding.root
    }
}