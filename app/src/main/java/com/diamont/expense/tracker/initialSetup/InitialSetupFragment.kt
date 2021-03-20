package com.diamont.expense.tracker.initialSetup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.databinding.FragmentInitialSetupBinding
import com.diamont.expense.tracker.databinding.FragmentWelcomeBinding

class InitialSetupFragment : Fragment() {

    /** Data binding */
    private lateinit var binding : FragmentInitialSetupBinding

    /** Get our View Model */
    private val viewModel : InitialSetupFragmentViewModel by activityViewModels {
        InitialSetupFragmentViewModelFactory(
            requireNotNull(this.activity).application
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /** Inflate the layout for this fragment */
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_initial_setup, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        /** Return the inflated layout */
        return binding.root
    }

}