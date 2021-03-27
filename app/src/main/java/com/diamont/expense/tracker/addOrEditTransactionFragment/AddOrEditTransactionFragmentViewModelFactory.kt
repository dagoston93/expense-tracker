package com.diamont.expense.tracker.addOrEditTransactionFragment

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import java.lang.IllegalArgumentException

class AddOrEditTransactionFragmentViewModelFactory(
    private val application: Application,
    private val databaseDao: TransactionDatabaseDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(AddOrEditTransactionFragmentViewModel::class.java)){
            return AddOrEditTransactionFragmentViewModel(
                application,
                databaseDao
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel given to AddOrEditTransactionFragmentViewModelFactory")
    }
}