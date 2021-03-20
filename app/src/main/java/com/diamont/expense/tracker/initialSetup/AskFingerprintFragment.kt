package com.diamont.expense.tracker.initialSetup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.databinding.FragmentAskFingerprintBinding
import com.diamont.expense.tracker.databinding.FragmentCreatePinBinding


class AskFingerprintFragment : Fragment() {

    /** Data binding */
    private lateinit var binding: FragmentAskFingerprintBinding

    /** Get our View Model */
    private val viewModel: InitialSetupFragmentViewModel by activityViewModels {
        InitialSetupFragmentViewModelFactory(
            requireNotNull(this.activity).application
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /** Inflate the layout for this fragment */
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ask_fingerprint, container, false)
        binding.lifecycleOwner = this

        /** Set the active page for the dot indicator */
        viewModel.setActivePage(3)

        /**
         * Set onClickListeners for the buttons
         */
        binding.btnAskFingerprintYes.setOnClickListener {
            viewModel.enableFingerprintSensor()
            navigateToNext()
        }

        binding.btnAskFingerprintNo.setOnClickListener {
            navigateToNext()
        }

        /** Return the inflated view */
        return binding.root
    }

    /**
     * Call this method to navigate to next page
     */
    fun navigateToNext(){
        binding.root.findNavController().navigate(
            AskFingerprintFragmentDirections.actionAskFingerprintFragmentToChooseCurrencyFragment()
        )
    }

}