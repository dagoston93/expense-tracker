package com.diamont.expense.tracker.addCategoryDialogFragment

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import java.lang.IllegalArgumentException

class AddCategoryDialogFragmentViewModelFactory (
    private val application: Application,
    private val databaseDao: TransactionDatabaseDao,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(AddCategoryDialogFragmentViewModel::class.java)){
            return AddCategoryDialogFragmentViewModel(
                application,
                databaseDao
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel given to AddCategoryDialogFragmentViewModelFactory")
    }
}