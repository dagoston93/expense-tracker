package com.diamont.expense.tracker.statisticFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.diamont.expense.tracker.util.DateRangeSelectorFragmentViewModel
import com.diamont.expense.tracker.util.database.TransactionDatabaseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class StatisticFragmentViewModel (
    private val appContext: Application,
    private val databaseDao: TransactionDatabaseDao
) : AndroidViewModel(appContext), DateRangeSelectorFragmentViewModel {
    /**
     * Set up some live data
     */

    /**
     * Declare some variables
     */

    /**
     * Set up coroutine job and the scope
     */
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)


    /**
     * onCleared() is called when view model is destroyed
     * in this case we need to cancel coroutines
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}