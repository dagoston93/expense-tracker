package com.diamont.expense.tracker.manageCategoriesFragment

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import java.lang.IllegalArgumentException

class ManageCategoriesFragmentViewModelFactory(
    private val databaseDao: TransactionDatabaseDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ManageCategoriesFragmentViewModel::class.java)){
            return ManageCategoriesFragmentViewModel(
                databaseDao
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel given to ManageCategoriesFragmentViewModelFactory")
    }
}