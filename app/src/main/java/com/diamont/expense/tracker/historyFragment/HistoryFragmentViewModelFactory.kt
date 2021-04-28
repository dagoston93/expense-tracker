package com.diamont.expense.tracker.historyFragment

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import java.lang.IllegalArgumentException

class HistoryFragmentViewModelFactory (
    private val context: Context,
    private val databaseDao: TransactionDatabaseDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(HistoryFragmentViewModel::class.java)){
            return HistoryFragmentViewModel(
                context,
                databaseDao
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel given to HistoryFragmentViewModelFactory")
    }
}