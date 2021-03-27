package com.diamont.expense.tracker.addOrEditTransactionFragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.diamont.expense.tracker.MainActivityViewModel
import com.diamont.expense.tracker.MainActivityViewModelFactory
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.databinding.FragmentAddOrEditTransactionBinding
import com.diamont.expense.tracker.util.arrayAdapters.StringArrayAdapter
import com.diamont.expense.tracker.util.enums.PaymentMethod
import com.diamont.expense.tracker.util.enums.TransactionPlanned
import com.diamont.expense.tracker.util.enums.TransactionType
import com.diamont.expense.tracker.util.getStringListIndexFromText
import com.diamont.expense.tracker.util.interfaces.BackPressCallbackFragment
import com.google.android.material.datepicker.MaterialDatePicker

class AddOrEditTransactionFragment : Fragment(), BackPressCallbackFragment {
    /** Data binding */
    private lateinit var binding : FragmentAddOrEditTransactionBinding

    /** Get the Activity View Model */
    private val activityViewModel : MainActivityViewModel by activityViewModels {
        MainActivityViewModelFactory(
            requireNotNull(this.activity).application
        )
    }

    /** Create adapters and string lists for exposed dropdown menus */
    private lateinit var transactionTypeAdapter : StringArrayAdapter
    private lateinit var transactionPlannedAdapter : StringArrayAdapter
    private lateinit var paymentMethodAdapter : StringArrayAdapter

    private lateinit var transactionTypeStringList : List<String>
    private lateinit var transactionPlannedStringList : List<String>
    private lateinit var paymentMethodStringList : List<String>


    /**
     * onCreateView()
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /** Inflate the layout for this fragment */
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_or_edit_transaction, container, false)
        binding.lifecycleOwner = this

        /** Set up values for activity view model */
        activityViewModel.setTitle("Add")
        activityViewModel.setBottomNavBarVisibility(false)
        activityViewModel.setUpButtonVisibility(true)
        activityViewModel.setDrawerLayoutEnabled(false)

        /** Set up the Exposed Dropdown Menus */
        transactionTypeStringList = TransactionType.getValuesAsStringList(requireContext())
        transactionPlannedStringList = TransactionPlanned.getValuesAsStringList(requireContext())
        paymentMethodStringList = PaymentMethod.getValuesAsStringList(requireContext())

        transactionTypeAdapter = StringArrayAdapter(requireContext(), transactionTypeStringList)
        transactionPlannedAdapter = StringArrayAdapter(requireContext(), transactionPlannedStringList)
        paymentMethodAdapter = StringArrayAdapter(requireContext(), paymentMethodStringList)

        binding.actvAddTransactionType.setText(transactionTypeAdapter.getItem(0), false)
        binding.actvAddIsPlanned.setText(transactionPlannedAdapter.getItem(0), false)
        binding.actvAddPaymentMethod.setText(paymentMethodAdapter.getItem(0), false)


        /**
         * OnCLickListener for date
         */
        binding.etAddDate.setOnClickListener {
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()

            datePicker.show(activity?.supportFragmentManager!!, "")
        }

        /**
         * Set add button on click listener
         */
        //binding.btnAddTransaction.setOnClickListener {
//            val idx = binding.actvAddTransactionType.getStringListIndexFromText(transactionTypeStringList)
//            if(idx != null){
//                val id = TransactionType.getIdFromIndex(idx)
//                Log.d("GUS", "idx: $idx id: $id")
//            }
        //}

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

    /**
     * We fill the dropdown menus in onResume() so that
     * if device configuration is changed we don't loose the
     * items from the menu
     */
    override fun onResume() {
        binding.actvAddTransactionType.setAdapter(transactionTypeAdapter)
        binding.actvAddIsPlanned.setAdapter(transactionPlannedAdapter)
        binding.actvAddPaymentMethod.setAdapter(paymentMethodAdapter)
        super.onResume()
    }

}