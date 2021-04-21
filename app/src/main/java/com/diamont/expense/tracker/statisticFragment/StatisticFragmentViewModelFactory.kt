package com.diamont.expense.tracker.statisticFragment

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import java.lang.IllegalArgumentException

class StatisticFragmentViewModelFactory (
    private val application: Application,
    private val databaseDao: TransactionDatabaseDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(StatisticFragmentViewModel::class.java)){
            return StatisticFragmentViewModel(
                application,
                databaseDao
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel given to StatisticFragmentViewModelFactory")
    }
}