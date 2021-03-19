package com.diamont.expense.tracker.initialSetup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.databinding.FragmentChooseCurrencyBinding
import com.diamont.expense.tracker.databinding.FragmentCreatePinBinding


class ChooseCurrencyFragment : Fragment() {

    /** Data binding */
    private lateinit var binding: FragmentChooseCurrencyBinding

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_choose_currency, container, false)
        binding.lifecycleOwner = this

        /** Return the inflated view*/
        return binding.root
    }

}