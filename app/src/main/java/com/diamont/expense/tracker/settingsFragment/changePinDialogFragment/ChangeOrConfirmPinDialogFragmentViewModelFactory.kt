package com.diamont.expense.tracker.settingsFragment.changePinDialogFragment

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class ChangeOrConfirmPinDialogFragmentViewModelFactory (
    private val application: Application,
    private val sharedPreferences: SharedPreferences,
    private val isConfirmMode: Boolean
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ChangeOrConfirmPinDialogFragmentViewModel::class.java)){
            return ChangeOrConfirmPinDialogFragmentViewModel(
                application,
                sharedPreferences,
                isConfirmMode
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel given to ChangePinDialogFragmentViewModelFactory")
    }
}