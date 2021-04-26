package com.diamont.expense.tracker.util

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diamont.expense.tracker.R
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.*

/**
 * This abstract class helps us building the same functionality
 * in History fragment and Statistics fragment to select
 * periods
 */
abstract class DateRangeSelectorFragmentViewModel(appContext: Application) : AndroidViewModel(appContext) {

    private val _periodStringList = MutableLiveData<List<String>>(listOf<String>())
    val periodStringList : LiveData<List<String>>
        get() = _periodStringList

    protected var selectedPeriodIndex: Int? = 0
    protected var calendarStartDate: Calendar = Calendar.getInstance()
    protected var calendarEndDate: Calendar = Calendar.getInstance()
    protected var calendars = CurrentCalendars()

    /**
     * The constructor
     */

    init{
        /** Set up the array adapter */
        _periodStringList.value = listOf(
            appContext.resources.getString(R.string.whole_period),
            appContext.resources.getString(R.string.current_month),
            appContext.resources.getString(R.string.last_seven_days),
            appContext.resources.getString(R.string.previous_month),
            appContext.resources.getString(R.string.this_year),
            appContext.resources.getString(R.string.select_period)
        )
    }

    /**
     * Create some constants for the period indexes
     */
    companion object{
        const val IDX_WHOLE_PERIOD: Int = 0
        const val IDX_CURRENT_MONTH: Int = 1
        const val IDX_LAST_SEVEN_DAYS: Int = 2
        const val IDX_PREVIOUS_MONTH: Int = 3
        const val IDX_THIS_YEAR: Int = 4
        const val IDX_DATE_RANGE: Int = 5
    }

    /**
     * This method is called when the selected period changed
     */
    abstract fun filterItems()

    /**
     * This method is called when a whole period is selected
     */
    abstract fun onWholePeriodSelected()

    /**
     * Call this method if user selects a date range
     */
    fun onDateRangeSelected(startDate: Long?, endDate: Long?){
        //Log.d("GUS", "date range picked: $startDate, $endDate")

        if(startDate != null && endDate != null) {
            calendarStartDate.timeInMillis = startDate
            calendarEndDate.timeInMillis = endDate
        }

        selectedPeriodIndex = IDX_DATE_RANGE
        filterItems()
    }

    /**
     * Call this method if user selects an option from the dropdown menu
     */
    fun onPeriodDropdownItemSelected(index: Int?){
        if(index != null){
            selectedPeriodIndex = index

            when(selectedPeriodIndex){
                /**
                 * Whole period
                 */
                IDX_WHOLE_PERIOD -> {
                    onWholePeriodSelected()
                }

                /**
                 * Current month selected
                 */
                IDX_CURRENT_MONTH -> {
                    calendarStartDate.timeInMillis = calendars.calendarStartOfMonth.timeInMillis
                    calendarEndDate.timeInMillis = calendars.calendarEndOfMonth.timeInMillis

                    //Log.d("GUS", "start: ${calendarToString(calendarStartDate)}")
                    //Log.d("GUS", "end: ${calendarToString(calendarEndDate)}")
                }

                /**
                 * This year selected
                 */
                IDX_THIS_YEAR -> {
                    calendarStartDate.timeInMillis = calendars.calendarStartOfYear.timeInMillis
                    calendarEndDate.timeInMillis = calendars.calendarEndOfYear.timeInMillis

                    //Log.d("GUS", "start: ${calendarToString(calendarStartDate)}")
                    //Log.d("GUS", "end: ${calendarToString(calendarEndDate)}")
                }

                /**
                 * Previous month selected
                 */
                IDX_PREVIOUS_MONTH -> {
                    calendarStartDate.timeInMillis = calendars.calendarStartOfMonth.timeInMillis
                    calendarStartDate.add(Calendar.MONTH, -1)

                    calendarEndDate.timeInMillis = calendars.calendarStartOfMonth.timeInMillis
                    calendarEndDate.add(Calendar.DAY_OF_YEAR, - 1)
                    calendarEndDate.set(Calendar.SECOND, 59)
                    calendarEndDate.set(Calendar.MINUTE, 59)
                    calendarEndDate.set(Calendar.HOUR, 23)

                    //Log.d("GUS", "start: ${calendarToString(calendarStartDate)}")
                    //Log.d("GUS", "end: ${calendarToString(calendarEndDate)}")
                }

                /**
                 * Last 7 days selected
                 */
                IDX_LAST_SEVEN_DAYS -> {
                    calendarEndDate.timeInMillis = MaterialDatePicker.todayInUtcMilliseconds()
                    calendarEndDate.set(Calendar.SECOND, 59)
                    calendarEndDate.set(Calendar.MINUTE, 59)
                    calendarEndDate.set(Calendar.HOUR, 23)

                    calendarStartDate.timeInMillis = calendarEndDate.timeInMillis
                    calendarStartDate.add(Calendar.DAY_OF_YEAR, -7)
                    calendarStartDate.set(Calendar.SECOND, 0)
                    calendarStartDate.set(Calendar.MINUTE, 0)
                    calendarStartDate.set(Calendar.HOUR, 0)

                    //Log.d("GUS", "start: ${calendarToString(calendarStartDate)}")
                    //Log.d("GUS", "end: ${calendarToString(calendarEndDate)}")
                }
            }

            filterItems()
        }else{
            selectedPeriodIndex = IDX_DATE_RANGE
        }
    }
}