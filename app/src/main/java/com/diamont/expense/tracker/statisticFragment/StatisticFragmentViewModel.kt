package com.diamont.expense.tracker.statisticFragment

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.DateRangeSelectorFragmentViewModel
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class StatisticFragmentViewModel (
    private val appContext: Application,
    private val databaseDao: TransactionDatabaseDao
) : DateRangeSelectorFragmentViewModel(appContext) {

    /**
     * Set up some live data
     */
    private val _statisticTypeStringList = MutableLiveData<List<String>>(listOf<String>())
    val statisticTypeStringList : LiveData<List<String>>
        get() = _statisticTypeStringList

    /**
     * Declare some variables
     */

    /**
     * Set up coroutine job and the scope
     */
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)


    /**
     * Constructor
     */
    init{
        _statisticTypeStringList.value = listOf(
            appContext.resources.getString(R.string.incomes_and_expenses),
            appContext.resources.getString(R.string.by_categories),
            appContext.resources.getString(R.string.plans)
        )
    }

    override fun filterItems() {
        Log.d("GUS","FILLY...")
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