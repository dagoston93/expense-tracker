package com.diamont.expense.tracker.statisticFragment

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import java.lang.IllegalArgumentException

class StatisticFragmentViewModelFactory (
    private val context: Context,
    private val databaseDao: TransactionDatabaseDao,
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(StatisticFragmentViewModel::class.java)){
            return StatisticFragmentViewModel(
                context,
                databaseDao,
                sharedPreferences
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel given to StatisticFragmentViewModelFactory")
    }
}