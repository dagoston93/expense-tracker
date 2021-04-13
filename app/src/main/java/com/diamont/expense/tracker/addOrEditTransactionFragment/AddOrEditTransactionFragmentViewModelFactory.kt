package com.diamont.expense.tracker.addOrEditTransactionFragment

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import com.diamont.expense.tracker.util.enums.TransactionType
import java.lang.IllegalArgumentException

class AddOrEditTransactionFragmentViewModelFactory(
    private val application: Application,
    private val databaseDao: TransactionDatabaseDao,
    private val transactionIdToEdit: Int?,
    private val isTransactionToEdit: Boolean,
    private val defaultTransactionType: TransactionType
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(AddOrEditTransactionFragmentViewModel::class.java)){
            return AddOrEditTransactionFragmentViewModel(
                application,
                databaseDao,
                transactionIdToEdit,
                isTransactionToEdit,
                defaultTransactionType
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel given to AddOrEditTransactionFragmentViewModelFactory")
    }
}