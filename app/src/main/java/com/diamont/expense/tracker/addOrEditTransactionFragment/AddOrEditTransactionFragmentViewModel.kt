package com.diamont.expense.tracker.addOrEditTransactionFragment

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.boolToVisibility
import com.diamont.expense.tracker.util.database.Transaction
import com.diamont.expense.tracker.util.database.TransactionCategory
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import com.diamont.expense.tracker.util.enums.PaymentMethod
import com.diamont.expense.tracker.util.enums.TransactionFrequency
import com.diamont.expense.tracker.util.enums.TransactionPlanned
import com.diamont.expense.tracker.util.enums.TransactionType
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.*
import java.util.*

class AddOrEditTransactionFragmentViewModel(
    private val appContext: Application,
    private val databaseDao: TransactionDatabaseDao
) : AndroidViewModel(appContext) {
    /**
     * Declare required variables
     */
    private var date : Date = Date(MaterialDatePicker.todayInUtcMilliseconds())
    private val dateFormat = android.text.format.DateFormat.getDateFormat(appContext)
    private var selectedTransactionType : TransactionType = TransactionType.EXPENSE

    private var incomePlans: List<Transaction> = listOf()
    private var expensePlans: List<Transaction> = listOf()
    private var incomePlanStringList: MutableList<String>  = mutableListOf()
    private var expensePlanStringList: MutableList<String>  = mutableListOf()

    private var currentTransaction = Transaction()

    /**
     * Set up some live data
     */
    private val _titleString = MutableLiveData<String>(appContext.resources.getString(R.string.add_expense))
    val titleString : LiveData<String>
        get() = _titleString

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

    private val _categories = MutableLiveData<List<TransactionCategory>>(listOf<TransactionCategory>())
    val categories : LiveData<List<TransactionCategory>>
        get() = _categories

    private val _currentPlanList = MutableLiveData<List<String>>(listOf<String>())
    val currentPlanList : LiveData<List<String>>
        get() = _currentPlanList

    private val _venues = MutableLiveData<List<String>>(listOf<String>())
    val venues : LiveData<List<String>>
        get() = _venues

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

    private val _planCategoryIndex = MutableLiveData<Int>(0)
    val planCategoryIndex : LiveData<Int>
        get() = _planCategoryIndex

    private val _planPaymentMethodIndex = MutableLiveData<Int>(0)
    val planPaymentMethodIndex : LiveData<Int>
        get() = _planPaymentMethodIndex

    private val _recipientOrVenueHint = MutableLiveData<String>(appContext.resources.getString(R.string.recipient_venue))
    val recipientOrVenueHint: LiveData<String>
        get() = _recipientOrVenueHint

    private val _paymentMethodHint = MutableLiveData<String>(appContext.resources.getString(R.string.payment_method))
    val paymentMethodHint: LiveData<String>
        get() = _paymentMethodHint

    private val _dateHint = MutableLiveData<String>(appContext.resources.getString(R.string.date))
    val dateHint: LiveData<String>
        get() = _dateHint

    private val _descriptionErrorMessage = MutableLiveData<String?>(null)
    val descriptionErrorMessage: LiveData<String?>
        get() = _descriptionErrorMessage

    val isErrorEnabled :LiveData<Boolean> = Transformations.map(_descriptionErrorMessage){
        it != null
    }

    private val _isInputValid = MutableLiveData<Boolean>(false)
    val isInputValid : LiveData<Boolean>
        get() = _isInputValid

    private val _isCategorySelectEnabled = MutableLiveData<Boolean>(true)
    val isCategorySelectEnabled : LiveData<Boolean>
        get() = _isCategorySelectEnabled

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
        _dateString.value = dateFormat.format(date)
        currentTransaction.date = date.time
        getDataFromDatabase()
    }

    /**
     * Call this method when the selected date changes
     */
    fun onSelectedDateChanged(newDate: Long){
        date.time=newDate
        _dateString.value = dateFormat.format(date)

        currentTransaction.date = newDate
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
        if(index == null) return

        selectedTransactionType = TransactionType.getEnumValueFromIndex(index)
        currentTransaction.transactionType = selectedTransactionType

        if(selectedTransactionType == TransactionType.INCOME)
        {
            /** Set up the title */
            _titleString.value = appContext.resources.getString(R.string.add_income)

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

        }else if(selectedTransactionType == TransactionType.EXPENSE){
            /** Set up the title */
            _titleString.value = appContext.resources.getString(R.string.add_expense)

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

        }else if(selectedTransactionType == TransactionType.DEPOSIT){
            /** Set up the title */
            _titleString.value = appContext.resources.getString(R.string.add_deposit)

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

        }else if(selectedTransactionType == TransactionType.WITHDRAW){
            /** Set up the title */
            _titleString.value = appContext.resources.getString(R.string.add_withdrawal)

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

        }else if(selectedTransactionType == TransactionType.PLAN_EXPENSE){
            /** Set up the title */
            _titleString.value = appContext.resources.getString(R.string.add_plan_expense)

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

        }else if(selectedTransactionType == TransactionType.PLAN_INCOME){
            /** Set up the title */
            _titleString.value = appContext.resources.getString(R.string.add_plan_income)

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
        if(index == null) return    // Do a quick null check

        /** If Not planned is selected we need to clear the selections */
        if(index == 0){

            _descriptionString.value = ""
            _amountString.value = ""
            _recipientOrVenueString.value = ""
            _planCategoryIndex.value = 0

            currentTransaction.planned = TransactionPlanned.NOT_PLANNED

            /** Enable category select */
            _isCategorySelectEnabled.value = true

        }else{
            /** Get the correct plan */
            var plan: Transaction = if(selectedTransactionType == TransactionType.EXPENSE){
                expensePlans[index-1]
            }else{
                incomePlans[index-1]
            }

            currentTransaction.planIdOrIsActive = plan.transactionId
            currentTransaction.planned = TransactionPlanned.PLANNED

            /** Enable category select */
            _isCategorySelectEnabled.value = false

            /** If the plan is a SUM type, we don't set description and amount, otherwise we do. */
            if(plan.frequency == TransactionFrequency.MONTHLY_SUM
                || plan.frequency == TransactionFrequency.FORTNIGHTLY_SUM
                || plan.frequency == TransactionFrequency.WEEKLY_SUM
                || plan.frequency == TransactionFrequency.YEARLY_SUM){
                _descriptionString.value = ""
                _amountString.value = ""
            }else{
                _descriptionString.value = plan.description
                _amountString.value = plan.amount.toString()
            }
            _recipientOrVenueString.value = plan.secondParty

            /** Find the index of the category in the list */
            if(_categories.value!= null)
            for(i in _categories.value!!.indices){
                if(_categories.value!![i].categoryId == plan.categoryId){
                    _planCategoryIndex.value = i
                }
            }

            /** Find the index of the selected payment method */
            for(i in PaymentMethod.values().indices){
                if(plan.method == PaymentMethod.values()[i]){
                    _planPaymentMethodIndex.value = i
                }
            }
        }
    }

    /**
     * Call this method when the chosen transaction frequency changes
     */
    fun onSelectedTransactionFrequencyChanged(selectedIndex: Int?){
        if(selectedIndex == null) return /** A quick null check */

        val selectedFrequency = TransactionFrequency.getFromIndex(selectedIndex)
        currentTransaction.frequency = selectedFrequency

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
        currentTransaction.description = newDescription
        validateDescription()
    }

    /**
     * Call this method whenever the entered amount is set
     */
    fun onEnteredAmountChanged(amount: Float?){
        currentTransaction.amount = amount ?: 0f
        validate()
    }

    /**
     *  Call this method whenever the selected category changes
     */
    fun onSelectedCategoryChanged(categoryName: String){
        val category = _categories.value?.find { it.categoryName == categoryName }

        if(category != null){
            currentTransaction.categoryId = category.categoryId
        }
    }

    /**
     * Call this method whenever the entered recipient/venue/source is changed
     */
    fun onEnteredRecipientOrVenueChanged(newRecipientOrVenue: String?){
        currentTransaction.secondParty = newRecipientOrVenue ?: ""
    }

    /**
     * Call this method whenever the selected payment method changes
     */
    fun onSelectedPaymentMethodChanged(index: Int?){
        if(index != null) {
            currentTransaction.method = PaymentMethod.getFromIndex(index)
        }
    }

    /**
     * Call this method whenever the add button is clicked
     */
    fun onAddButtonCLicked(){
        validate()
        if(_isInputValid.value == true){
            insertTransaction()
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
        if(currentTransaction.amount == 0f){
            isValid = false
        }

        /** Check if there is any description entered */
        if(_descriptionErrorMessage.value != null){
            isValid = false
        }

        _isInputValid.value = isValid
    }

    /**
     * Call this method if a transaction is passed as an argument
     * to edit it.
     */
    fun setEditTransactionId(id: Int){
        //getTransactionToEdit(id)
        //Log.d("GUS", "$currentTransaction")
    }

    /**
     * This method retrieves the required data from the database
     */
    private fun getDataFromDatabase(){
        uiScope.launch {
            _categories.value = databaseDao.getCategoriesSuspend()
            _venues.value = databaseDao.getVenuesSuspend()
            incomePlans = databaseDao.getIncomePlansSuspend()
            expensePlans = databaseDao.getExpensePlansSuspend()
            createPlanStringLists()
            _currentPlanList.value = expensePlanStringList
        }
    }

    /**
     * This method inserts the transaction to the database
     */
    private fun insertTransaction(){
        uiScope.launch {
            databaseDao.insertTransactionSuspend(currentTransaction)
            _isOperationComplete.value = true
        }
    }

    /**
     * This method retrieves the transaction that needs to be editet
     */
    private fun getTransactionToEdit(id : Int){
        uiScope.launch {
            currentTransaction = databaseDao.getTransactionById(id)
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


}