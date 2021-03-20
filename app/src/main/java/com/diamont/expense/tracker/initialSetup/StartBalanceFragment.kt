package com.diamont.expense.tracker.initialSetup

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.databinding.FragmentStartBalanceBinding


class StartBalanceFragment : Fragment() {

    /** Data binding */
    private lateinit var binding: FragmentStartBalanceBinding

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
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_start_balance, container, false)
        binding.lifecycleOwner = this

        binding.tilStartBalanceCash.suffixText = viewModel.selectedCurrencySign
        binding.tilStartBalanceCard.suffixText = viewModel.selectedCurrencySign

        /** Set onClickListener for the button */
        binding.btnSetStartBalance.setOnClickListener {
            viewModel.setInitialBalance(
                binding.tilStartBalanceCash.editText?.text.toString().toFloatOrNull(),
                binding.tilStartBalanceCard.editText?.text.toString().toFloatOrNull()
            )
            it.findNavController().navigate(
                StartBalanceFragmentDirections.actionStartBalanceFragmentToSetupCompleteFragment()
            )
        }

        /** Return the inflated view */
        return binding.root
    }


}