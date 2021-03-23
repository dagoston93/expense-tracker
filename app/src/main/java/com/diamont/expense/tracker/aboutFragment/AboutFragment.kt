package com.diamont.expense.tracker.aboutFragment

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
import com.diamont.expense.tracker.databinding.FragmentAboutBinding
import com.diamont.expense.tracker.util.BackPressCallbackFragment


class AboutFragment : Fragment(), BackPressCallbackFragment {
    /** Data binding */
    private lateinit var binding : FragmentAboutBinding

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_about, container, false)
        binding.lifecycleOwner = this

        /** Set up values for activity view model */
        activityViewModel.setTitle(getString(R.string.about))
        activityViewModel.setBottomNavBarVisibility(false)
        activityViewModel.setUpButtonVisibility(true)
        activityViewModel.setDrawerLayoutEnabled(false)

        /** Return the inflated layout */
        return binding.root
    }

    /**
     * This method handles the back button press
     */
    override fun onBackPressed(listener: () -> Unit): Boolean {
        listener()
        return true
    }

}