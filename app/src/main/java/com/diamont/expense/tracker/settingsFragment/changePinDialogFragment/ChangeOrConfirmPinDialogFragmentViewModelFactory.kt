package com.diamont.expense.tracker.settingsFragment.changePinDialogFragment

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class ChangeOrConfirmPinDialogFragmentViewModelFactory (
    private val resources: Resources,
    private val sharedPreferences: SharedPreferences,
    private val isConfirmMode: Boolean
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ChangeOrConfirmPinDialogFragmentViewModel::class.java)){
            return ChangeOrConfirmPinDialogFragmentViewModel(
                resources,
                sharedPreferences,
                isConfirmMode
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel given to ChangePinDialogFragmentViewModelFactory")
    }
}