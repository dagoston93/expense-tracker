package com.diamont.expense.tracker

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.diamont.expense.tracker.util.enums.TransactionType

/**
 * This view model is responsible for handling actions
 * that needs to be accessed from different fragments,
 * such as changing the title of the action bar,
 * showing the up button, or hiding the bottom navigation.
 */
class MainActivityViewModel(context: Context) : ViewModel(){
    /** Declare some variables */
    private val _actionbarTitle = MutableLiveData<String>(context.getString(R.string.app_name))
    val actionbarTitle : LiveData<String>
        get() = _actionbarTitle

    private val _isUpButtonVisible = MutableLiveData<Boolean>(false)
    val isUpButtonVisible : LiveData<Boolean>
        get() = _isUpButtonVisible

    private val _isBottomNavBarVisible = MutableLiveData<Boolean?>(null)
    val isBottomNavBarVisible : LiveData<Boolean?>
        get() = _isBottomNavBarVisible

    private val _isDrawerEnabled = MutableLiveData<Boolean>(false)
    val isDrawerEnabled : LiveData<Boolean>
        get() = _isDrawerEnabled

    /** We need the shared preferences */
    private var _sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    val sharedPreferences: SharedPreferences
        get() = _sharedPreferences

    /** Default transaction type when user clicks the add FAB */
    var defaultTransactionType: TransactionType = TransactionType.EXPENSE


    /**
     * Trigger this event when user clicks on an edit icon
     * by setting the transaction/plan id as the value.
     *
     * After navigating reset it to null.
     */
    val eventNavigateToEditFragment = MutableLiveData<Int?>(null)

    /**
     * Set this variable if editing plan before triggering eventNavigateToEditFragment
     */
    var isTransactionToEdit: Boolean = true

    /**
     * Call this method to set the title
     */
    fun setTitle(title : String){
        _actionbarTitle.value = title
    }

    /**
     * Call this method to show or hide the bottom nav bar
     */
    fun setBottomNavBarVisibility(isVisible : Boolean){
        _isBottomNavBarVisible.value = isVisible
    }

    /**
     * Call this method to show or hide the up button
     */
    fun setUpButtonVisibility(isVisible: Boolean){
        _isUpButtonVisible.value = isVisible
    }

    /**
     * Call this method to enable/disable the drawer layout
     */
    fun setDrawerLayoutEnabled(isEnabled : Boolean){
        _isDrawerEnabled.value = isEnabled
    }
}