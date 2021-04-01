package com.diamont.expense.tracker.manageCategoriesFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diamont.expense.tracker.util.database.TransactionCategory
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ManageCategoriesFragmentViewModel(
    private val appContext: Application,
    private val databaseDao: TransactionDatabaseDao
) : AndroidViewModel(appContext) {
    /**
     * Declare the required variables
     */
    private val _categories = MutableLiveData<List<TransactionCategory>>(listOf<TransactionCategory>())
    val categories: LiveData<List<TransactionCategory>>
        get() = _categories

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
     * This method retrieves the categories
     */
    private fun getCategories(){
        uiScope.launch {
            _categories.value = databaseDao.getCategoriesSuspend()
        }
    }

    /**
     * Call this method if the category list changes (add/edit/delete)
     */
    fun onCategoryListChanged(){
        getCategories()
    }

    /**
     * This method deletes a category
     */
    fun deleteCategory(categoryId: Int){
        uiScope.launch {
            databaseDao.deleteCategorySuspend(categoryId)
            _categories.value = databaseDao.getCategoriesSuspend()
        }
    }

    /**
     * onCleared() is called when view model is destroyed
     * in this case we need to cancel coroutines
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}