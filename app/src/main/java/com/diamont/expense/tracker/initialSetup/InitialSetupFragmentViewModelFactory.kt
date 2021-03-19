package com.diamont.expense.tracker.initialSetup

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class InitialSetupFragmentViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(InitialSetupFragmentViewModel::class.java)){
            return InitialSetupFragmentViewModel(
                application
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel given to InitialSetupFragmentViewModelFactory")
    }
}