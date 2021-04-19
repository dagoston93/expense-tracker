package com.diamont.expense.tracker.initialSetup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.diamont.expense.tracker.MainActivityViewModel
import com.diamont.expense.tracker.MainActivityViewModelFactory
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.databinding.FragmentCreatePinBinding
import com.diamont.expense.tracker.util.database.TransactionDatabase

class CreatePinFragment : Fragment() {
    /** Data binding */
    private lateinit var binding: FragmentCreatePinBinding

    /** Get the Activity View Model */
    private val activityViewModel : MainActivityViewModel by activityViewModels {
        MainActivityViewModelFactory(
            requireNotNull(this.activity).application
        )
    }

    /** Get our View Model */
    private val viewModel : InitialSetupFragmentViewModel by activityViewModels {
        InitialSetupFragmentViewModelFactory(
            requireNotNull(this.activity).application,
            activityViewModel.sharedPreferences,
            TransactionDatabase.getInstance(requireNotNull(this.activity).application).transactionDatabaseDao
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /** Inflate the layout for this fragment */
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_pin, container, false)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        /** Set the active page for the dot indicator */
        viewModel.setActivePage(2)

        /** Enable/disable button if enough/not enough digits entered */
        binding.pciCreatePin.isInputComplete.observe(viewLifecycleOwner, Observer {
            binding.btnCreatePin.isEnabled = it
        })

        /** Set onClickListener for our button */
        binding.btnCreatePin.setOnClickListener {
            viewModel.setPinButtonClicked(binding.pciCreatePin.pinCodeEntered)
            binding.pciCreatePin.reset()
        }

        /** If pin code is saved we navigate to next tab */
        viewModel.isPinCodeSaved.observe(viewLifecycleOwner, Observer {
            if(it) {
                val direction = if (viewModel.isFingerprintSensorAvailable) {
                    CreatePinFragmentDirections.actionCreatePinFragmentToAskFingerprintFragment()
                } else {
                    CreatePinFragmentDirections.actionCreatePinFragmentToChooseCurrencyFragment()
                }

                binding.root.findNavController().navigate(direction)
            }
        })
        
        /** Return the inflated view*/
        return binding.root
    }


}