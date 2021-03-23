package com.diamont.expense.tracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * This view model is responsible for handling actions
 * that needs to be accessed from different fragments,
 * such as changing the title of the action bar,
 * showing the up button, or hiding the bottom navigation.
 */
class MainActivityViewModel (appContext: Application) : AndroidViewModel(appContext){
    /** Declare some variables */
    private val _actionbarTitle = MutableLiveData<String>(appContext.getString(R.string.app_name))
    val actionbarTitle : LiveData<String>
        get() = _actionbarTitle

    private val _isUpButtonVisible = MutableLiveData<Boolean>(false)
    val isUpButtonVisible : LiveData<Boolean>
        get() = _isUpButtonVisible

    private val _isBottomNavBarVisible = MutableLiveData<Boolean>(false)
    val isBottomNavBarVisible : LiveData<Boolean>
        get() = _isBottomNavBarVisible

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
}