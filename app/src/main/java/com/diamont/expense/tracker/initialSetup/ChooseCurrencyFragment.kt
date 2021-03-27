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
import com.diamont.expense.tracker.databinding.FragmentChooseCurrencyBinding
import com.diamont.expense.tracker.util.Currency
import com.diamont.expense.tracker.util.arrayAdapters.CurrencyArrayAdapter


class ChooseCurrencyFragment : Fragment() {

    /** Data binding */
    private lateinit var binding: FragmentChooseCurrencyBinding

    /** Get our View Model */
    private val viewModel: InitialSetupFragmentViewModel by activityViewModels {
        InitialSetupFragmentViewModelFactory(
            requireNotNull(this.activity).application
        )
    }

    /** Create adapter for currency exposed dropdown menu */
    private lateinit var adapter : CurrencyArrayAdapter

    /**
     * onCreateView()
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /** Inflate the layout for this fragment */
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_choose_currency, container, false)
        binding.lifecycleOwner = this

        /** Set the active page for the dot indicator */
        viewModel.setActivePage(4)

        /** Set up the Exposed Dropdown Menu */
        adapter = CurrencyArrayAdapter(requireContext(), Currency.availableCurrencies)
        binding.actvCurrencyList.setText(adapter.getItem(0).toString(), false)

        /** Set onClickListener for the button */
        binding.btnChooseCurrency.setOnClickListener {
            viewModel.setSelectedCurrencyId(getSelectedCurrency())
            it.findNavController().navigate(
                ChooseCurrencyFragmentDirections.actionChooseCurrencyFragmentToStartBalanceFragment()
            )
        }



        /** Return the inflated view */
        return binding.root
    }

    /**
     * We fill the dropdown menu in onResume() so that
     * if device configuration is changed we don't loose the
     * items from the menu
     */
    override fun onResume() {
        binding.actvCurrencyList.setAdapter(adapter)
        super.onResume()
    }

    /**
     * This method retrieves the id of the selected currency
     */
    private fun getSelectedCurrency() : Currency {
        var currency = Currency.availableCurrencies[0]

        for(i in Currency.availableCurrencies.indices)
        {
            if(binding.actvCurrencyList.text.toString() ==  Currency.availableCurrencies[i].toString()){
                currency =  Currency.availableCurrencies[i]
            }
        }

        return currency
    }

}