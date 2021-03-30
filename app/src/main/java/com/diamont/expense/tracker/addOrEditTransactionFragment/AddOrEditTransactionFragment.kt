package com.diamont.expense.tracker.addOrEditTransactionFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.diamont.expense.tracker.MainActivityViewModel
import com.diamont.expense.tracker.MainActivityViewModelFactory
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.databinding.FragmentAddOrEditTransactionBinding
import com.diamont.expense.tracker.util.KEY_BUNDLE_SET_PLAN_AS_DEFAULT
import com.diamont.expense.tracker.util.KEY_BUNDLE_TRANSACTION_ID
import com.diamont.expense.tracker.util.arrayAdapters.TransactionCategoryAdapter
import com.diamont.expense.tracker.util.database.TransactionCategory
import com.diamont.expense.tracker.util.database.TransactionDatabase
import com.diamont.expense.tracker.util.enums.PaymentMethod
import com.diamont.expense.tracker.util.enums.TransactionFrequency
import com.diamont.expense.tracker.util.enums.TransactionType
import com.diamont.expense.tracker.util.getStringListIndexFromText
import com.diamont.expense.tracker.util.interfaces.BackPressCallbackFragment
import com.google.android.material.datepicker.MaterialDatePicker

class AddOrEditTransactionFragment : Fragment(), BackPressCallbackFragment {
    /** Data binding and view model */
    private lateinit var binding : FragmentAddOrEditTransactionBinding
    private lateinit var viewModel : AddOrEditTransactionFragmentViewModel

    /** Get the Activity View Model */
    private val activityViewModel : MainActivityViewModel by activityViewModels {
        MainActivityViewModelFactory(
            requireNotNull(this.activity).application
        )
    }

    /** Create adapters and string lists for exposed dropdown menus */
    private lateinit var transactionTypeAdapter : ArrayAdapter<String>
    private lateinit var paymentMethodAdapter : ArrayAdapter<String>
    private lateinit var frequencyAdapter : ArrayAdapter<String>
    private lateinit var transactionCategoryAdapter: TransactionCategoryAdapter
    private lateinit var venueAdapter: ArrayAdapter<String>
    private lateinit var planAdapter: ArrayAdapter<String>

    private lateinit var transactionTypeStringList : List<String>
    private lateinit var planStringList : List<String>
    private lateinit var paymentMethodStringList : List<String>
    private lateinit var frequencyStringList : List<String>



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

        /**
         *  Create the view model using a view model factory
         */
        val application = requireNotNull(this.activity).application
        val databaseDao = TransactionDatabase.getInstance(application).transactionDatabaseDao

        /**
         * Receive arguments
         */
        val id: Int? = if(arguments != null){
            this.arguments?.getInt(KEY_BUNDLE_TRANSACTION_ID)
        }else{
            null
        }

        val setPlanAsDefault : Boolean = if(arguments != null){
            this.arguments?.getBoolean(KEY_BUNDLE_SET_PLAN_AS_DEFAULT) ?: false
        }else{
            false
        }

        val viewModelFactory = AddOrEditTransactionFragmentViewModelFactory(application, databaseDao, id, setPlanAsDefault)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(AddOrEditTransactionFragmentViewModel::class.java)
        binding.viewModel = viewModel

        /**
         * Set up values for activity view model
         */
        activityViewModel.setBottomNavBarVisibility(false)
        activityViewModel.setUpButtonVisibility(true)
        activityViewModel.setDrawerLayoutEnabled(false)

        /**
         * Set up the Exposed Dropdown Menus
         */
        transactionTypeStringList = TransactionType.getValuesAsStringList(requireContext())
        paymentMethodStringList = PaymentMethod.getValuesAsStringList(requireContext())
        frequencyStringList = TransactionFrequency.getValuesAsStringList(requireContext())

        transactionTypeAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, transactionTypeStringList)
        paymentMethodAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, paymentMethodStringList)
        frequencyAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, frequencyStringList)

        transactionCategoryAdapter = TransactionCategoryAdapter(requireContext(), listOf<TransactionCategory>())
        venueAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, listOf<String>())
        planAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, listOf<String>())

        binding.actvAddTransactionType.setText(transactionTypeAdapter.getItem(0), false)
        binding.actvAddPaymentMethod.setText(paymentMethodAdapter.getItem(0), false)
        binding.actvAddFrequency.setText(frequencyAdapter.getItem(0), false)



        /**
         * textChanged listener for Transaction Type dropdown menu
         */
        binding.actvAddTransactionType.addTextChangedListener{
            val idx = binding.actvAddTransactionType.getStringListIndexFromText(transactionTypeStringList)
            viewModel.onTransactionTypeChanged(idx)
        }

        /**
         * text changed listener for the Plan dropdown menu
         */
        binding.actvAddIsPlanned.addTextChangedListener {
            val idx = binding.actvAddIsPlanned.getStringListIndexFromText(planStringList)
            viewModel.onSelectedPlanChanged(idx)
        }

        /**
         * text changed listener for the Frequency dropdown menu
         */
        binding.actvAddFrequency.addTextChangedListener {
            val idx = binding.actvAddFrequency.getStringListIndexFromText(frequencyStringList)
            viewModel.onSelectedTransactionFrequencyChanged(idx)
        }

        /**
         * Add text changed listener for the description field
         */
        binding.etAddDescription.addTextChangedListener {
            viewModel.onEnteredDescriptionChanged(binding.etAddDescription.text.toString())
        }

        /**
         * Add text changed listener for the amount field
         */
        binding.etAddAmount.addTextChangedListener {
            viewModel.onEnteredAmountChanged(binding.etAddAmount.text.toString().toFloatOrNull())
        }

        /**
         * Add text changed listener for the category dropdown
         */
        binding.actvAddCategory.addTextChangedListener {
            viewModel.onSelectedCategoryChanged(binding.actvAddCategory.text.toString())
        }

        /**
         * Add text changed listener for the venue field
         */
        binding.actvAddRecipientOrVenue.addTextChangedListener {
            viewModel.onEnteredRecipientOrVenueChanged(binding.actvAddRecipientOrVenue.text.toString())
        }

        /**
         * Add text changed listener for the payment method dropdown
         */
        binding.actvAddPaymentMethod.addTextChangedListener {
            val idx = binding.actvAddPaymentMethod.getStringListIndexFromText(paymentMethodStringList)
            viewModel.onSelectedPaymentMethodChanged(idx)
        }

        /**
         * OnCLickListener for date text view
         */
        binding.etAddDate.setOnClickListener {
            /**
             * Create the date picker
             */
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText(context?.resources?.getString(R.string.select_date))
                    .setSelection(viewModel.date.time)
                    .build()

            /**
             * OnClickListener for the date picker OK button
             */
            datePicker.addOnPositiveButtonClickListener {
                viewModel.onSelectedDateChanged(it)
            }

            datePicker.show(childFragmentManager, "")
        }

        /**
         * Observe Live data
         *
         * App title
         */
        viewModel.titleString.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            activityViewModel.setTitle(it)
        })

        /**
         * If the list of categories received from database we update the adapter for the dropdown menu
         */
        viewModel.categories.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if(it.isNotEmpty()){
                transactionCategoryAdapter = TransactionCategoryAdapter(requireContext(), it)
                binding.actvAddCategory.setAdapter(transactionCategoryAdapter)
                binding.actvAddCategory.setText(transactionCategoryAdapter.getItem(0).toString(), false)
            }
        })

        /**
         * Update the list of plans (expense/income) if needed
         */
        viewModel.currentPlanList.observe(viewLifecycleOwner, Observer {
            if(it.isNotEmpty()){
                planStringList = it
                planAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, it)
                binding.actvAddIsPlanned.setAdapter(planAdapter)
                binding.actvAddIsPlanned.setText(planAdapter.getItem(0).toString(), false)
            }
        })

        /**
         * When the venues are retrieved we set the adapter for the autocomplete text view
         */
        viewModel.venues.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if(it.isNotEmpty()){
                venueAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, it)
                binding.actvAddRecipientOrVenue.setAdapter(venueAdapter)
            }
        })

        /**
         * Observe selected selected category index
         */
        viewModel.selectedCategoryIndex.observe(viewLifecycleOwner, Observer {
            if(!transactionCategoryAdapter.isEmpty){
                binding.actvAddCategory.setText(transactionCategoryAdapter.getItem(it).toString(), false)
            }
        })

        /**
         * Observe selected payment method index
         */
        viewModel.selectedPaymentMethodIndex.observe(viewLifecycleOwner, Observer {
            if(!paymentMethodAdapter.isEmpty){
                binding.actvAddPaymentMethod.setText(paymentMethodAdapter.getItem(it).toString(), false)
            }
        })

        /**
         * Observe selected plan index
         */
        viewModel.selectedPlanIndex.observe(viewLifecycleOwner, Observer {
            if(!planAdapter.isEmpty){
                binding.actvAddIsPlanned.setText(planAdapter.getItem(it).toString(), false)
            }
        })

        /**
         * Observe selected transaction type index
         */
        viewModel.selectedTransactionTypeIndex.observe(viewLifecycleOwner, Observer {
            if(!transactionTypeAdapter.isEmpty){
                binding.actvAddTransactionType.setText(transactionTypeAdapter.getItem(it).toString(), false)
            }
        })

        /**
         * Observe the selected transaction frequency index
         */
        viewModel.selectedFrequencyIndex.observe(viewLifecycleOwner, Observer {
            if(!frequencyAdapter.isEmpty){
                binding.actvAddFrequency.setText(frequencyAdapter.getItem(it).toString(), false)
            }
        })

        /**
         * Observe description error message
         */
        viewModel.descriptionErrorMessage.observe(viewLifecycleOwner, Observer {
            binding.tilAddDescription.error = it
        })









        /**
         * If operation complete we navigate back
         */
        viewModel.isOperationComplete.observe(viewLifecycleOwner, Observer {
            if(it){
                activity?.onBackPressed()
            }
        })

        /**
         * Set add button on click listener
         */
        binding.btnAddTransaction.setOnClickListener {
            viewModel.onAddButtonCLicked()
        }



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
        binding.actvAddIsPlanned.setAdapter(planAdapter)
        binding.actvAddPaymentMethod.setAdapter(paymentMethodAdapter)
        binding.actvAddFrequency.setAdapter(frequencyAdapter)
        binding.actvAddCategory.setAdapter(transactionCategoryAdapter)
        binding.actvAddRecipientOrVenue.setAdapter(venueAdapter)

        super.onResume()
    }

}