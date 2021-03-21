package com.diamont.expense.tracker.authentication

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class  AuthenticationFragmentViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(AuthenticationFragmentViewModel::class.java)){
            return AuthenticationFragmentViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel given to AuthenticationFragmentViewModelFactory")
    }
}