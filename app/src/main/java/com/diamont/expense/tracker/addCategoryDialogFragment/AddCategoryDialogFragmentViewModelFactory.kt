package com.diamont.expense.tracker.addCategoryDialogFragment

import android.app.Application
import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import java.lang.IllegalArgumentException

class AddCategoryDialogFragmentViewModelFactory (
    private val resources: Resources,
    private val databaseDao: TransactionDatabaseDao,
    private val editCategoryId: Int?,
    private val categoryListChangeCallBack: () -> Unit
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(AddCategoryDialogFragmentViewModel::class.java)){
            return AddCategoryDialogFragmentViewModel(
                resources,
                databaseDao,
                editCategoryId,
                categoryListChangeCallBack
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel given to AddCategoryDialogFragmentViewModelFactory")
    }
}