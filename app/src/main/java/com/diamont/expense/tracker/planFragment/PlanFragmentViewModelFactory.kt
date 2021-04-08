package com.diamont.expense.tracker.planFragment

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import java.lang.IllegalArgumentException

class PlanFragmentViewModelFactory (
    private val application: Application,
    private val databaseDao: TransactionDatabaseDao,
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(PlanFragmentViewModel::class.java)){
            return PlanFragmentViewModel(
                application,
                databaseDao,
                sharedPreferences
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel given to PlanFragmentViewModelFactory")
    }
}