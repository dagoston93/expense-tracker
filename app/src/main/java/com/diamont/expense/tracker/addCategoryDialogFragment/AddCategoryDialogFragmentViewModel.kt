package com.diamont.expense.tracker.addCategoryDialogFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.database.TransactionCategory
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AddCategoryDialogFragmentViewModel(
    private val appContext: Application,
    private val databaseDao: TransactionDatabaseDao,
    private val editCategoryId: Int?
) : AndroidViewModel(appContext) {

    private var _categories: List<TransactionCategory> = listOf<TransactionCategory>()
    private var categoryToEdit = TransactionCategory()

    private val _currentCategoryName = MutableLiveData<String>("")
    val currentCategoryName: LiveData<String>
        get() = _currentCategoryName

    private val _currentCategoryColorId = MutableLiveData<Int>(-1)
    val currentCategoryColorId: LiveData<Int>
        get() = _currentCategoryColorId

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

        /**
         * If we have a category to edit we load it
         */
        if(editCategoryId != null){
            getCategoryToEdit()
        }
    }

    /**
     * This method validates the user input for category name
     */
    fun validateCategoryName(name: String): String?{
        val result = _categories.find { it.categoryName == name }
        var error: String? = null

        /** Check if an entry with the entered name exists already */
        if(result != null){
            /** If we are in edit mode check if it is the current category name*/
            if(editCategoryId != null){
                if(categoryToEdit.categoryName != name){
                    error = appContext.resources.getString(R.string.category_already_exists)
                }
            }else{
                error = appContext.resources.getString(R.string.category_already_exists)
            }
        }

        /** Check if string is empty */
        if(name.isEmpty()){
            error = appContext.resources.getString(R.string.category_name_empty)
        }

        return error
    }

    /**
     * This method adds a new category
     */
    private fun addCategory(name: String, colorResId: Int){
        val category = TransactionCategory(0, name, colorResId)
        uiScope.launch {
            databaseDao.insertCategorySuspend(category)
        }
    }

    /**
     * This method updates an existing category
     */
    private fun updateCategory(name: String, colorResId: Int){
        categoryToEdit.categoryName = name
        categoryToEdit.categoryColorResId = colorResId
        uiScope.launch {
            databaseDao.updateCategorySuspend(categoryToEdit)
        }
    }

    /**
     * Call this method if user clicks add/save button
     */
    fun onPositiveButtonClick(name: String, colorResId: Int){
        if(editCategoryId == null){
            addCategory(name, colorResId)
        }else{
            updateCategory(name, colorResId)
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

    /**
     * This method retrieves the category to edit
     */
    fun getCategoryToEdit(){
        uiScope.launch {
            if(editCategoryId != null) {
                categoryToEdit = databaseDao.getCategoryByIdSuspend(editCategoryId)
                _currentCategoryName.value = categoryToEdit.categoryName
                _currentCategoryColorId.value = categoryToEdit.categoryColorResId
            }
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