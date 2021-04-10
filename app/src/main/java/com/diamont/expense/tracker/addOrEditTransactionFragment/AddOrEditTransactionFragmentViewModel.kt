package com.diamont.expense.tracker.addOrEditTransactionFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.boolToVisibility
import com.diamont.expense.tracker.util.database.*
import com.diamont.expense.tracker.util.enums.PaymentMethod
import com.diamont.expense.tracker.util.enums.TransactionFrequency
import com.diamont.expense.tracker.util.enums.TransactionPlanned
import com.diamont.expense.tracker.util.enums.TransactionType
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.*
import java.util.*

class AddOrEditTransactionFragmentViewModel(
    private val appContext: Application,
    private val databaseDao: TransactionDatabaseDao,
    private val idToEdit: Int?,
    private val isTransactionToEdit: Boolean,
    private val setPlanAsDefaultType: Boolean
) : AndroidViewModel(appContext) {

    /**
     * Declare required variables
     */
    private var _date : Date = Date(MaterialDatePicker.todayInUtcMilliseconds())
    val date : Date
        get() = _date

    private val dateFormat = android.text.format.DateFormat.getDateFormat(appContext)

    private var selectedTransactionType : TransactionType = TransactionType.EXPENSE
    private var selectedDate: Long = 0
    private var selectedPlanId: Int = -1
    private var enteredDescription: String = ""
    private var enteredAmount: Float = 0f
    private var selectedCategoryId: Int = 0
    private var enteredRecipientOrSource: String = ""
    private var selectedPaymentMethod: PaymentMethod = PaymentMethod.CASH
    private var selectedTransactionFrequency: TransactionFrequency = TransactionFrequency.ONE_TIME

    private var originalPlan: Plan? = null
    private var lastSelectedPlan: Plan = Plan()

    private var currentTransaction = Transaction()
    private var currentPlan = Plan()

    private var isEditMode: Boolean = false
    private var isInitialSetupDone: Boolean = false

    private var incomePlans: MutableList<Plan> = mutableListOf()
    private var expensePlans: MutableList<Plan> = mutableListOf()
    private var incomePlanStringList: MutableList<String>  = mutableListOf()
    private var expensePlanStringList: MutableList<String>  = mutableListOf()
    /**
     * Live data for the string values
     */
    private val _titleString = MutableLiveData<String>("")
    val titleString : LiveData<String>
        get() = _titleString

    private val _buttonText = MutableLiveData<String>("")
    val buttonText : LiveData<String>
        get() = _buttonText

    private val _descriptionString = MutableLiveData<String>("")
    val descriptionString : LiveData<String>
        get() = _descriptionString

    private val _amountString = MutableLiveData<String>("")
    val amountString : LiveData<String>
        get() = _amountString

    private val _recipientOrVenueString = MutableLiveData<String>("")
    val recipientOrVenueString : LiveData<String>
        get() = _recipientOrVenueString

    private val _dateString = MutableLiveData<String>("")
    val dateString : LiveData<String>
        get() = _dateString

    /**
     * Live data for hint strings
     */
    private val _recipientOrVenueHint = MutableLiveData<String>(appContext.resources.getString(R.string.recipient_venue))
    val recipientOrVenueHint: LiveData<String>
        get() = _recipientOrVenueHint

    private val _paymentMethodHint = MutableLiveData<String>(appContext.resources.getString(R.string.payment_method))
    val paymentMethodHint: LiveData<String>
        get() = _paymentMethodHint

    private val _dateHint = MutableLiveData<String>(appContext.resources.getString(R.string.date))
    val dateHint: LiveData<String>
        get() = _dateHint

    private val _currencySign = MutableLiveData<String>("")
    val currencySign: LiveData<String>
        get() = _currencySign

    /**
     * Live data for error the string
     */
    private val _descriptionErrorMessage = MutableLiveData<String?>(null)
    val descriptionErrorMessage: LiveData<String?>
        get() = _descriptionErrorMessage

    val isErrorEnabled: LiveData<Boolean> = Transformations.map(_descriptionErrorMessage){
        it != null
    }

    /**
     * Live data for data from database
     */
    private val _categories = MutableLiveData<List<TransactionCategory>>(listOf<TransactionCategory>())
    val categories : LiveData<List<TransactionCategory>>
        get() = _categories

    private val _currentPlanList = MutableLiveData<List<String>>(listOf<String>())
    val currentPlanList : LiveData<List<String>>
        get() = _currentPlanList

    private val _venueOrSourceList = MutableLiveData<List<String>>(listOf<String>())
    val venueOrSourceList : LiveData<List<String>>
        get() = _venueOrSourceList

    private var sources = listOf<String>()
    private var venues = listOf<String>()

    /**
     * Live data for field visibility
     */
    private val _isIsPlannedFieldVisible = MutableLiveData<Boolean>(true)
    val isIsPlannedFieldVisible = Transformations.map(_isIsPlannedFieldVisible){
        boolToVisibility(it)
    }

    private val _isCategoryFieldVisible = MutableLiveData<Boolean>(true)
    val isCategoryFieldVisible = Transformations.map(_isCategoryFieldVisible){
        boolToVisibility(it)
    }

    private val _isRecipientOrVenueFieldVisible = MutableLiveData<Boolean>(true)
    val isRecipientOrVenueFieldVisible = Transformations.map(_isRecipientOrVenueFieldVisible){
        boolToVisibility(it)
    }

    private val _isPaymentMethodFieldVisible = MutableLiveData<Boolean>(true)
    val isPaymentMethodFieldVisible = Transformations.map(_isPaymentMethodFieldVisible){
        boolToVisibility(it)
    }

    private val _isFrequencyFieldVisible = MutableLiveData<Boolean>(false)
    val isFrequencyFieldVisible = Transformations.map(_isFrequencyFieldVisible){
        boolToVisibility(it)
    }

    /**
     * Live data for dropdown menu selected indexes
     */
    private val _selectedTransactionTypeIndex = MutableLiveData<Int>(0)
    val selectedTransactionTypeIndex : LiveData<Int>
        get() = _selectedTransactionTypeIndex

    private val _selectedCategoryIndex = MutableLiveData<Int>(0)
    val selectedCategoryIndex : LiveData<Int>
        get() = _selectedCategoryIndex

    private val _selectedPlanIndex = MutableLiveData<Int>(0)
    val selectedPlanIndex : LiveData<Int>
        get() = _selectedPlanIndex

    private val _selectedPaymentMethodIndex = MutableLiveData<Int>(0)
    val selectedPaymentMethodIndex : LiveData<Int>
        get() = _selectedPaymentMethodIndex

    private val _selectedFrequencyIndex = MutableLiveData<Int>(0)
    val selectedFrequencyIndex : LiveData<Int>
        get() = _selectedFrequencyIndex

    /**
     * Other live data
     */
    private val _isInputValid = MutableLiveData<Boolean>(false)
    val isInputValid : LiveData<Boolean>
        get() = _isInputValid

    private val _isCategorySelectEnabled = MutableLiveData<Boolean>(true)
    val isCategorySelectEnabled : LiveData<Boolean>
        get() = _isCategorySelectEnabled

    private val _isTransactionTypeSelectEnabled = MutableLiveData<Boolean>(true)
    val isTransactionTypeSelectEnabled : LiveData<Boolean>
        get() = _isTransactionTypeSelectEnabled

    private val _isOperationComplete = MutableLiveData<Boolean>(false)
    val isOperationComplete : LiveData<Boolean>
        get() = _isOperationComplete

    /**
     * Set up coroutine job and the scope
     */
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Constructor
     */
    init{
        /**
         * Check if we need to edit a transaction or create a new one
         */
        if(idToEdit != null) {
            isEditMode = true
            _buttonText.value = appContext.resources.getString(R.string.save)
        }else{
            _buttonText.value = appContext.resources.getString(R.string.add)
            /** Get the date for today */
            _dateString.value = dateFormat.format(_date)
            selectedDate = _date.time
        }

        getDataFromDatabase()
    }

    /**
     * Call this method when the selected date changes
     */
    fun onSelectedDateChanged(newDate: Long){
        if(!isInitialSetupDone) return /** Only process after initial setup done */

        _date.time = newDate
        _dateString.value = dateFormat.format(_date)

        selectedDate = newDate
    }

    /**
     * Call this method whenever the selected
     * transaction type changes
     *
     * @param index: The index in the enum class
     * of the selected transaction type.
     * (Same as the index in the array adapter.)
     */
    fun onTransactionTypeChanged(index: Int?){
        //if(!isInitialSetupDone) return /** Only process after initial setup done */
        if(index == null) return

        selectedTransactionType = TransactionType.getEnumValueFromIndex(index)
        currentTransaction.transactionType = selectedTransactionType

        if(selectedTransactionType == TransactionType.INCOME)
        {
            /** Set up the title */
            if(!isEditMode){
                _titleString.value = appContext.resources.getString(R.string.add_income)
            }

            /** Set the field visibilities */
            _isIsPlannedFieldVisible.value = true
            _isCategoryFieldVisible.value = true
            _isRecipientOrVenueFieldVisible.value = true
            _isPaymentMethodFieldVisible.value = true
            _isFrequencyFieldVisible.value = false

            /** Reset description */
            if(_descriptionString.value != ""){
                _descriptionString.value = ""
            }

            /** Set up the hints */
            _recipientOrVenueHint.value = appContext.resources.getString(R.string.source)
            _paymentMethodHint.value = appContext.resources.getString(R.string.received_by)
            _dateHint.value = appContext.resources.getString(R.string.date)

            /** Set up the plan list */
            _currentPlanList.value = incomePlanStringList

            /** Reset error string */
            _descriptionErrorMessage.value = null

            /** Enable category select */
            _isCategorySelectEnabled.value = true

            /** Set planId to 0 in the beginning if not in edit mode */
            if(!isEditMode){
                currentTransaction.planId = 0
                _venueOrSourceList.value = sources
            }

        }else if(selectedTransactionType == TransactionType.EXPENSE){
            /** Set up the title */
            if(!isEditMode) {
                _titleString.value = appContext.resources.getString(R.string.add_expense)
            }

            /** Set the field visibilities */
            _isIsPlannedFieldVisible.value = true
            _isCategoryFieldVisible.value = true
            _isRecipientOrVenueFieldVisible.value = true
            _isPaymentMethodFieldVisible.value = true
            _isFrequencyFieldVisible.value = false

            /** Reset description */
            if(_descriptionString.value != ""){
                _descriptionString.value = ""
            }

            /** Set up the hints */
            _recipientOrVenueHint.value = appContext.resources.getString(R.string.recipient_venue)
            _paymentMethodHint.value = appContext.resources.getString(R.string.payment_method)
            _dateHint.value = appContext.resources.getString(R.string.date)

            /** Set up the plan list */
            _currentPlanList.value = expensePlanStringList

            /** Reset error string */
            _descriptionErrorMessage.value = null

            /** Enable category select */
            _isCategorySelectEnabled.value = true

            /** Set planId to 0 in the beginning if not in edit mode */
            if(!isEditMode){
                currentTransaction.planId = 0
                _venueOrSourceList.value = venues
            }

        }else if(selectedTransactionType == TransactionType.DEPOSIT){
            /** Set up the title */
            if(!isEditMode) {
                _titleString.value = appContext.resources.getString(R.string.add_deposit)
            }

            /** Set the field visibilities */
            _isIsPlannedFieldVisible.value = false
            _isCategoryFieldVisible.value = false
            _isRecipientOrVenueFieldVisible.value = false
            _isPaymentMethodFieldVisible.value = false
            _isFrequencyFieldVisible.value = false

            /** Clear fields */
            _amountString.value = ""

            /** Set description */
            _descriptionString.value = appContext.resources.getString(R.string.deposit)

            /** Set up the hints */
            _dateHint.value = appContext.resources.getString(R.string.date)

            /** Reset error string */
            _descriptionErrorMessage.value = null

            /** Enable category select */
            _isCategorySelectEnabled.value = true

            /** reset isPlanned property of current transaction */
            currentTransaction.planned = TransactionPlanned.NOT_PLANNED
            currentTransaction.planId = 0

        }else if(selectedTransactionType == TransactionType.WITHDRAW){
            /** Set up the title */
            if(!isEditMode) {
                _titleString.value = appContext.resources.getString(R.string.add_withdrawal)
            }

            /** Set the field visibilities */
            _isIsPlannedFieldVisible.value = false
            _isCategoryFieldVisible.value = false
            _isRecipientOrVenueFieldVisible.value = false
            _isPaymentMethodFieldVisible.value = false
            _isFrequencyFieldVisible.value = false

            /** Clear fields */
            _amountString.value = ""

            /** Set description */
            _descriptionString.value = appContext.resources.getString(R.string.withdraw)

            /** Set up the hints */
            _dateHint.value = appContext.resources.getString(R.string.date)

            /** Reset error string */
            _descriptionErrorMessage.value = null

            /** Enable category select */
            _isCategorySelectEnabled.value = true

            /** reset isPlanned property of current transaction */
            currentTransaction.planned = TransactionPlanned.NOT_PLANNED
            currentTransaction.planId = 0

        }else if(selectedTransactionType == TransactionType.PLAN_EXPENSE){
            /** Set up the title */
            if(!isEditMode) {
                _titleString.value = appContext.resources.getString(R.string.add_plan_expense)
            }

            /** Set the field visibilities */
            _isIsPlannedFieldVisible.value = false
            _isCategoryFieldVisible.value = true
            _isRecipientOrVenueFieldVisible.value = true
            _isPaymentMethodFieldVisible.value = true
            _isFrequencyFieldVisible.value = true

            /** Reset description */
            if(_descriptionString.value != ""){
                _descriptionString.value = ""
            }

            /** Set up the hints */
            _recipientOrVenueHint.value = appContext.resources.getString(R.string.recipient_venue)
            _paymentMethodHint.value = appContext.resources.getString(R.string.payment_method)
            _dateHint.value = appContext.resources.getString(R.string.expected_date)

            /** Validate description if already entered */
            validateDescription()

            /** Enable category select */
            _isCategorySelectEnabled.value = true

            /** reset isPlanned property of current transaction */
            currentTransaction.planned = TransactionPlanned.NOT_PLANNED
            currentTransaction.planId = 1

            /** Set venue/source list for autocomplete text view */
            if(!isEditMode){
                _venueOrSourceList.value = venues
            }

        }else if(selectedTransactionType == TransactionType.PLAN_INCOME){
            /** Set up the title */
            if(!isEditMode) {
                _titleString.value = appContext.resources.getString(R.string.add_plan_income)
            }

            /** Set the field visibilities */
            _isIsPlannedFieldVisible.value = false
            _isCategoryFieldVisible.value = true
            _isRecipientOrVenueFieldVisible.value = true
            _isPaymentMethodFieldVisible.value = true
            _isFrequencyFieldVisible.value = true

            /** Reset description */
            if(_descriptionString.value != ""){
                _descriptionString.value = ""
            }

            /** Set up the hints */
            _recipientOrVenueHint.value = appContext.resources.getString(R.string.source)
            _paymentMethodHint.value = appContext.resources.getString(R.string.receive_by)
            _dateHint.value = appContext.resources.getString(R.string.expected_date)

            /** Validate description if already entered */
            validateDescription()

            /** Enable category select */
            _isCategorySelectEnabled.value = true

            /** reset isPlanned property of current transaction */
            currentTransaction.planned = TransactionPlanned.NOT_PLANNED
            currentTransaction.planId = 1

            /** Set venue/source list for autocomplete text view */
            if(!isEditMode){
                _venueOrSourceList.value = sources
            }
        }
    }

    /**
     * Call this method whenever the selected plan has changed
     *
     * @param index: The index of the selected item in the string list
     * of plans given to the dropdown menu. The index in the Transaction
     * list is always one less because of the 'not planned' item.
     * If it is 0 then the 'not planned' item is selected.
     */
    fun onSelectedPlanChanged(index: Int?){
        if(!isInitialSetupDone) return /** Only process after initial setup done */
        if(index == null) return    // Do a quick null check

        /** If Not planned is selected we need to clear the selections */
        if(index == 0){
            _selectedCategoryIndex.value = 0
            selectedPlanId = -1

            /** Enable category select */
            _isCategorySelectEnabled.value = true
        }else{
            /** Get the correct plan */
            lastSelectedPlan = if(selectedTransactionType == TransactionType.EXPENSE){
                expensePlans[index-1]
            }else{
                incomePlans[index-1]
            }

            selectedPlanId = lastSelectedPlan.id
            currentTransaction.planned = TransactionPlanned.PLANNED

            /** Enable category select */
            _isCategorySelectEnabled.value = false

            /** If the plan is a SUM type, we don't set description and amount, otherwise we do. */
            if(lastSelectedPlan.frequency == TransactionFrequency.MONTHLY_SUM
                || lastSelectedPlan.frequency == TransactionFrequency.FORTNIGHTLY_SUM
                || lastSelectedPlan.frequency == TransactionFrequency.WEEKLY_SUM
                || lastSelectedPlan.frequency == TransactionFrequency.YEARLY_SUM){
                _descriptionString.value = ""
                _amountString.value = ""
            }else{
                _descriptionString.value = lastSelectedPlan.description
                _amountString.value = lastSelectedPlan.amount.toString()
            }
            _recipientOrVenueString.value = lastSelectedPlan.sourceOrRecipient

            _selectedCategoryIndex.value=findCategoryIndex(lastSelectedPlan.categoryId)

            _selectedPaymentMethodIndex.value = PaymentMethod.getIndex(lastSelectedPlan.method)
        }
    }

    /**
     * Call this method to find the index of the category in the array
     */
    private fun findCategoryIndex(categoryId: Int) : Int {
        var index = 0
        /** Find the index of the category in the list */
        if (_categories.value != null){
            for (i in _categories.value!!.indices) {
                if (_categories.value!![i].categoryId == categoryId) {
                    index = i
                }
            }
        }

        return index
    }

    /**
     * Call this method when the chosen transaction frequency changes
     */
    fun onSelectedTransactionFrequencyChanged(selectedIndex: Int?){
        if(!isInitialSetupDone) return /** Only process after initial setup done */
        if(selectedIndex == null) return /** A quick null check */

        val selectedFrequency = TransactionFrequency.getFromIndex(selectedIndex)
        selectedTransactionFrequency = selectedFrequency

        /**
         * If sum type, the label will be 'first date' otherwise 'expected date'
         */
        if(selectedFrequency == TransactionFrequency.MONTHLY_SUM
            || selectedFrequency == TransactionFrequency.FORTNIGHTLY_SUM
            || selectedFrequency == TransactionFrequency.WEEKLY_SUM
            || selectedFrequency == TransactionFrequency.YEARLY_SUM){

            _dateHint.value = appContext.resources.getString(R.string.first_date)
        }else{
            _dateHint.value = appContext.resources.getString(R.string.expected_date)
        }

    }

    /**
     * Call this method if the entered description changed
     */
    fun onEnteredDescriptionChanged(newDescription: String){
        if(!isInitialSetupDone) return /** Only process after initial setup done */
        enteredDescription = newDescription
        validateDescription()
    }

    /**
     * Call this method whenever the entered amount is set
     */
    fun onEnteredAmountChanged(amount: Float?){
        if(!isInitialSetupDone) return /** Only process after initial setup done */
        enteredAmount = amount ?: 0f
        validate()
    }

    /**
     *  Call this method whenever the selected category changes
     */
    fun onSelectedCategoryChanged(categoryName: String){
        if(!isInitialSetupDone) return /** Only process after initial setup done */
        val category = _categories.value?.find { it.categoryName == categoryName }

        if(category != null){
            selectedCategoryId = category.categoryId
        }
    }

    /**
     * Call this method whenever the entered recipient/venue/source is changed
     */
    fun onEnteredRecipientOrVenueChanged(newRecipientOrVenue: String?){
        if(!isInitialSetupDone) return /** Only process after initial setup done */
        enteredRecipientOrSource = newRecipientOrVenue ?: ""
    }

    /**
     * Call this method whenever the selected payment method changes
     */
    fun onSelectedPaymentMethodChanged(index: Int?){
        if(!isInitialSetupDone) return /** Only process after initial setup done */
        if(index != null) {
            selectedPaymentMethod = PaymentMethod.getFromIndex(index)
        }
    }

    /**
     * Call this method whenever the add button is clicked
     */
    fun onAddButtonCLicked(){
        validate()

        /** If category id is 0 we set it to id of first category (Unspecified) */
        if(selectedCategoryId == 0){
            selectedCategoryId = _categories.value?.get(0)!!.categoryId
        }

        /** Save the user input to the plan or transaction */
        if(selectedTransactionType == TransactionType.PLAN_EXPENSE || selectedTransactionType == TransactionType.PLAN_INCOME){
            /**
             * Plan
             */
            currentPlan.transactionType = selectedTransactionType
            currentPlan.firstExpectedDate = selectedDate
            currentPlan.description = enteredDescription
            currentPlan.amount = enteredAmount
            currentPlan.categoryId = selectedCategoryId
            currentPlan.sourceOrRecipient = enteredRecipientOrSource
            currentPlan.method = selectedPaymentMethod
            currentPlan.frequency = selectedTransactionFrequency
        }else{
            /**
             * Transaction
             */
            currentTransaction.transactionType = selectedTransactionType
            currentTransaction.date = selectedDate
            currentTransaction.description = enteredDescription
            currentTransaction.planId = selectedPlanId
            currentTransaction.amount = enteredAmount
            currentTransaction.categoryId = selectedCategoryId
            currentTransaction.secondParty = enteredRecipientOrSource
            currentTransaction.method = selectedPaymentMethod
            currentTransaction.frequency = selectedTransactionFrequency
        }

        /** Update or insert category or plan */
        if(_isInputValid.value == true){
            if(isEditMode){
                if(selectedTransactionType == TransactionType.PLAN_EXPENSE || selectedTransactionType == TransactionType.PLAN_INCOME){
                    updatePlan()
                }else{
                    /** Check if originally the transaction was planned */
                    if(originalPlan != null){
                        /** Check if new and original plan match */
                        if(originalPlan != lastSelectedPlan){
                            /** If different plan is selected check if original plan was one time */
                            if(originalPlan!!.frequency == TransactionFrequency.ONE_TIME){
                                /** If yes, we set it to active and save it */
                                originalPlan!!.isStatusActive = true
                                updateSelectedPlan(originalPlan!!)
                            }
                        }
                    }

                    /** And now regardless the original plan we check if transaction was planned and update plan if needed */
                    if(currentTransaction.planId != -1){
                        /** If it is planned, if plan is one time, we update plan status as not active*/
                        if(lastSelectedPlan.frequency == TransactionFrequency.ONE_TIME){
                            lastSelectedPlan.isStatusActive = false
                            updateSelectedPlan(lastSelectedPlan)
                        }
                    }

                    /** And finally update the transaction (because it navigates away) */
                    updateTransaction()
                }
            }else{
                if(selectedTransactionType == TransactionType.PLAN_EXPENSE || selectedTransactionType == TransactionType.PLAN_INCOME){
                    insertPlan()
                }else {
                    /** Check if it is planned */
                    if(currentTransaction.planId != -1){
                        /** If it is planned, if plan is one time, we update plan status as not active*/
                        if(lastSelectedPlan.frequency == TransactionFrequency.ONE_TIME){
                            lastSelectedPlan.isStatusActive = false
                            updateSelectedPlan(lastSelectedPlan)
                        }
                    }

                    /** And finally insert the transaction (because it navigates away) */
                    insertTransaction()
                }
            }
        }
    }

    /**
     * Call this method when user adds a new category
     */
    fun onCategoryAdded(){
        uiScope.launch {
            _categories.value = databaseDao.getCategoriesSuspend()
            _selectedCategoryIndex.value = _categories.value?.size!!-1
        }
    }

    /**
     * Call this method to validate the entered description
     */
    private fun validateDescription() {
        /**
         * If we are in plan mode we need to make sure that
         * each plan has unique description otherwise we won't be able
         * to select them from the exposed dropdown menus...
         */
        if (selectedTransactionType == TransactionType.PLAN_EXPENSE
            || selectedTransactionType == TransactionType.PLAN_INCOME
        ) {

            val plans = if (selectedTransactionType == TransactionType.PLAN_EXPENSE) {
                expensePlans
            } else {
                incomePlans
            }

            val result = plans.find { it.description == currentTransaction.description }

//TODO need to accept original name if editing plan
            if (result != null) {
                _descriptionErrorMessage.value =
                    appContext.resources.getString(R.string.description_error_message)
            } else {
                _descriptionErrorMessage.value = null
            }
        }

        validate()
    }

    /**
     * Call this method to validate the user input
     */
    private fun validate(){
        var isValid = true

        /** Check if there is an amount entered */
        if(enteredAmount == 0f){
            isValid = false
        }

        /** Check if there is any description entered and is there any error */
        if(_descriptionErrorMessage.value != null || enteredDescription.isEmpty()){
            isValid = false
        }

        _isInputValid.value = isValid
    }

    /**
     * Call this method after transaction to edit
     * has been retrieved from database to set the form up
     */
    private fun setUpEditTransaction(){

        _isTransactionTypeSelectEnabled.value = false

        if(isTransactionToEdit) {
            /**
             * Set transaction type
             */
            _selectedTransactionTypeIndex.value =
                TransactionType.getIndex(currentTransaction.transactionType)

            /** Set the title */
            setTitle(currentTransaction.transactionType)

            /** Set the editable text field values **/
            _descriptionString.value = currentTransaction.description
            _amountString.value = currentTransaction.amount.toString()
            _recipientOrVenueString.value = currentTransaction.secondParty

            enteredDescription = currentTransaction.description
            enteredAmount = currentTransaction.amount
            enteredRecipientOrSource = currentTransaction.secondParty

            /** Set the selected category */
            _selectedCategoryIndex.value = findCategoryIndex(currentTransaction.categoryId)
            selectedCategoryId = currentTransaction.categoryId

            /** Set the plan */
            if (currentTransaction.planId == -1) {
                _selectedPlanIndex.value = 0
            } else {
                val planList = if (currentTransaction.transactionType == TransactionType.EXPENSE) {
                    expensePlans
                } else {
                    incomePlans
                }

                var index = 0
                for (i in planList.indices) {
                    if (planList[i].id == currentTransaction.planId) {
                        index = i
                    }
                }
                _selectedPlanIndex.value = index + 1
            }
            selectedPlanId = currentTransaction.planId

            /** If planned transaction, disable category choice */
            if (currentTransaction.planned == TransactionPlanned.PLANNED) {
                _isCategorySelectEnabled.value = false
            }

            /** Set payment method */
            _selectedPaymentMethodIndex.value = PaymentMethod.getIndex(currentTransaction.method)
            selectedPaymentMethod = currentTransaction.method

            /** Set the frequency **/
            _selectedFrequencyIndex.value =
                TransactionFrequency.getIndex((currentTransaction.frequency))
            selectedTransactionFrequency = currentTransaction.frequency

            /** Set the date */
            _date.time = currentTransaction.date
            _dateString.value = dateFormat.format(_date)
            selectedDate = currentTransaction.date

            /** Set initial value for venue or source list for autocomplete text view */
            _venueOrSourceList.value =
                if (currentTransaction.transactionType == TransactionType.EXPENSE) {
                    venues
                } else {
                    sources
                }
        }else{
            /**
             * Set transaction type
             */
            _selectedTransactionTypeIndex.value =
                TransactionType.getIndex(currentPlan.transactionType)

            /** Set the title */
            setTitle(currentPlan.transactionType)

            /** Set the editable text field values **/
            _descriptionString.value = currentPlan.description
            _amountString.value = currentPlan.amount.toString()
            _recipientOrVenueString.value = currentPlan.sourceOrRecipient

            enteredDescription = currentPlan.description
            enteredAmount = currentPlan.amount
            enteredRecipientOrSource = currentPlan.sourceOrRecipient

            /** Set the selected category */
            _selectedCategoryIndex.value = findCategoryIndex(currentPlan.categoryId)
            selectedCategoryId = currentPlan.categoryId

            /** Set payment method */
            _selectedPaymentMethodIndex.value = PaymentMethod.getIndex(currentPlan.method)
            selectedPaymentMethod = currentPlan.method

            /** Set the frequency **/
            _selectedFrequencyIndex.value =
                TransactionFrequency.getIndex((currentPlan.frequency))
            selectedTransactionFrequency = currentPlan.frequency

            /** Set the date */
            _date.time = currentPlan.firstExpectedDate
            _dateString.value = dateFormat.format(_date)
            selectedDate = currentPlan.firstExpectedDate

            /** Set initial value for venue or source list for autocomplete text view */
            _venueOrSourceList.value =
                if (currentTransaction.transactionType == TransactionType.PLAN_EXPENSE) {
                    venues
                } else {
                    sources
                }
        }
    }

    private fun setTitle(transactionType: TransactionType) {
        /** Set the title */
        val titleStringRes = when (transactionType) {
            TransactionType.EXPENSE -> R.string.edit_expense
            TransactionType.INCOME -> R.string.edit_income
            TransactionType.DEPOSIT -> R.string.edit_deposit
            TransactionType.WITHDRAW -> R.string.edit_withdrawal
            TransactionType.PLAN_EXPENSE -> R.string.edit_plan_expense
            TransactionType.PLAN_INCOME -> R.string.edit_plan_income
        }

        _titleString.value = appContext.resources.getString(titleStringRes)
    }

    /**
     * Call this method when initial data from database received
     */
    private fun onInitialDataReceived(){
        if(isEditMode){
            setUpEditTransaction()
        }else{
            /** Set initial value for venue or source list for autocomplete text view */
            _venueOrSourceList.value = venues
        }
        isInitialSetupDone = true
    }

    /**
     * Call this method when currency sign retrieved from shared prefs
     */
    fun setCurrencySign(sign: String){
        _currencySign.value = sign
    }

    /**
     * This method retrieves the required data from the database
     */
    private fun getDataFromDatabase(){
        uiScope.launch {
            _categories.value = databaseDao.getCategoriesSuspend()
            venues = databaseDao.getVenuesSuspend()
            sources = databaseDao.getSourcesSuspend()
            incomePlans = databaseDao.getActiveIncomePlansSuspend().toMutableList()
            expensePlans = databaseDao.getActiveExpensePlansSuspend().toMutableList()

            /** Check if we are in edit mode or not */
            if(isEditMode && idToEdit != null){
                if(isTransactionToEdit) {
                    currentTransaction = databaseDao.getTransactionByIdSuspend(idToEdit)

                    /** If income or expense we load the original plan if it is planned */
                    if(currentTransaction.transactionType == TransactionType.INCOME ||
                        currentTransaction.transactionType == TransactionType.EXPENSE) {

                        if (currentTransaction.planId != -1) {
                            originalPlan = databaseDao.getPlanByIdSuspend(currentTransaction.planId)
                            lastSelectedPlan = originalPlan!!

                            /** If plan is not active we need to add it to the correct plan list */
                            if(!originalPlan!!.isStatusActive){
                                if(currentTransaction.transactionType == TransactionType.EXPENSE){
                                    expensePlans.add(originalPlan!!)
                                }else{
                                    incomePlans.add(originalPlan!!)
                                }
                            }
                        }
                    }
                }else{
                    currentPlan = databaseDao.getPlanByIdSuspend(idToEdit)
                }
            }

            createPlanStringLists()
            _currentPlanList.value = expensePlanStringList

            /** Now we can do the set up for which we needed the data */
            onInitialDataReceived()
        }
    }

    /**
     * This method inserts the transaction to the database
     */
    private fun insertTransaction(){
        uiScope.launch {
            saveSecondPartyIfNew()
            databaseDao.insertTransactionSuspend(currentTransaction)
            _isOperationComplete.value = true
        }
    }

    /**
     * This method inserts the plan to the database
     */
    private fun insertPlan(){
        uiScope.launch {
            saveSecondPartyIfNew()
            databaseDao.insertPlanSuspend(currentPlan)
            _isOperationComplete.value = true
        }
    }

    /**
     * Update last selected plan status
     */
    private fun updateSelectedPlan(plan: Plan){
        uiScope.launch {
            saveSecondPartyIfNew()
            databaseDao.updatePlanSuspend(plan)
        }
    }

    /**
     * This method updates the transaction being edited
     */
    private fun updateTransaction(){
        uiScope.launch {
            saveSecondPartyIfNew()
            databaseDao.updateTransactionSuspend(currentTransaction)
            _isOperationComplete.value = true
        }
    }

    /**
     * This method updates the transaction being edited
     */
    private fun updatePlan(){
        uiScope.launch {
            saveSecondPartyIfNew()
            databaseDao.updatePlanSuspend(currentPlan)
            _isOperationComplete.value = true
        }
    }

    /**
     * This method saves the second party
     * if it is not saved yet in the database
     */
    private suspend fun saveSecondPartyIfNew(){
        /** We don't have second party field at withdraw/deposit so return if type is one of them */
        if(currentTransaction.transactionType == TransactionType.WITHDRAW ||
            currentTransaction.transactionType == TransactionType.DEPOSIT) return

        /** If the recipient/source is an empty string we are done */
        if(currentTransaction.secondParty.isEmpty()) return

        /** Choose which list to check */
        var list = listOf<String>()
        var isRecipient = false

        if(currentTransaction.transactionType == TransactionType.EXPENSE ||
            currentTransaction.transactionType == TransactionType.PLAN_EXPENSE){
            list = venues
            isRecipient = true
        }else{
            list = sources
        }

        /** Check if list contains the new entry */
        val item = list.find { currentTransaction.secondParty == it}

        /** If it does not we save it */
        if(item == null){
            val secondPartyData = SecondPartyData(0, currentTransaction.secondParty, isRecipient)
            databaseDao.insertSecondPartyDataSuspend(secondPartyData)
        }
    }

    /**
     * This method converts the Transaction lists of the
     * expense and income plans to string lists for the
     * dropdown menus
     */
    private fun createPlanStringLists(){
        incomePlanStringList.add(appContext.getString(R.string.not_planned))
        for(plan in incomePlans){
            incomePlanStringList.add(plan.description)
        }

        expensePlanStringList.add(appContext.getString(R.string.not_planned))
        for(plan in expensePlans){
            expensePlanStringList.add(plan.description)
        }
    }

    /**
     * onCleared() is called when view model is destroyed
     * in this case we need to cancel coroutines
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}