package com.diamont.expense.tracker.initialSetup

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao


class InitialSetupFragmentViewModelFactory(
    private val application: Application,
    private val sharedPreferences: SharedPreferences,
    private val databaseDao: TransactionDatabaseDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(InitialSetupFragmentViewModel::class.java)){
            return InitialSetupFragmentViewModel(
                application,
                sharedPreferences,
                databaseDao
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel given to InitialSetupFragmentViewModelFactory")
    }
}