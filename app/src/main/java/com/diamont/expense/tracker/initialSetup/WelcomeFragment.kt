package com.diamont.expense.tracker.initialSetup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {

    /** Data binding */
    private lateinit var binding : FragmentWelcomeBinding

    /** Get our View Model */
    private val viewModel : InitialSetupFragmentViewModel by activityViewModels {
        InitialSetupFragmentViewModelFactory(
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_welcome, container, false)
        binding.lifecycleOwner = this

        /** Set onClickListener for the start button */
        binding.btnStartInitialSetup.setOnClickListener {
            //it.findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToAskAuthenticationFragment())
            binding.diDotIndicator.setDotIndicatorActiveDot(3)
        }

        /** Return the inflated layout */
        return binding.root
    }
}