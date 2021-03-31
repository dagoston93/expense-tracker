package com.diamont.expense.tracker.addCategoryDialogFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.database.TransactionCategory
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AddCategoryDialogFragmentViewModel(
    private val appContext: Application,
    private val databaseDao: TransactionDatabaseDao
) : AndroidViewModel(appContext) {

    private var _categories: List<TransactionCategory> = listOf<TransactionCategory>()

    /**
     * Set up coroutine job and the scope
     */
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Constructor
     */
    init{
        getCategories()
    }

    /**
     * This method validates the user input for category name
     */
    fun validateCategoryName(name: String): String?{
        val result = _categories.find { it.categoryName == name }
        var error: String? = null

        if(result != null){
            error = appContext.resources.getString(R.string.category_already_exists)
        }

        if(name.isEmpty()){
            error = appContext.resources.getString(R.string.category_name_empty)
        }

        return error
    }

    /**
     * This method adds a new category
     */
    fun addCategory(name: String, colorResId: Int){
        val category = TransactionCategory(0, name, colorResId)
        uiScope.launch {
            databaseDao.insertCategorySuspend(category)
        }
    }

    /**
     * This method retrieves categories from database
     */
    private fun getCategories(){
        uiScope.launch {
            _categories = databaseDao.getCategoriesSuspend()
        }
    }

}