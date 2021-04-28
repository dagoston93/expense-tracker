package com.diamont.expense.tracker.authentication

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class  AuthenticationFragmentViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(AuthenticationFragmentViewModel::class.java)){
            return AuthenticationFragmentViewModel(
                context,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel given to AuthenticationFragmentViewModelFactory")
    }
}