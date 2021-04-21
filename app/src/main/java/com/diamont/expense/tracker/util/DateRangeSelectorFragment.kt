package com.diamont.expense.tracker.util

import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import com.diamont.expense.tracker.R
import com.google.android.material.datepicker.MaterialDatePicker

abstract class DateRangeSelectorFragment: Fragment() {
    protected abstract var baseClassViewModel: DateRangeSelectorFragmentViewModel

    /** Array adapter for period list */
    protected lateinit var periodAdapter : ArrayAdapter<String>

    /** Variables for the selected date range */
    private var previousSelectedPeriodIndex: Int? = 0
    private var previousSelectedStartDate: Long = 0
    private var previousSelectedEndDate: Long = 0

    protected fun onDateRangeSelected(idx: Int?, actvPeriod: AutoCompleteTextView)
    {
        if(idx != previousSelectedPeriodIndex) {

            if (idx == baseClassViewModel.periodStringList.value?.size!! - 1) {
                /**
                 * Create the date picker
                 */
                val datePickerBuilder =
                    MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText(resources.getString(R.string.select_period))

                /**
                 * If user has selected a date range before, we remember the selection
                 */
                if(previousSelectedStartDate != 0L && previousSelectedEndDate != 0L){
                    datePickerBuilder.setSelection(
                        Pair(
                            previousSelectedStartDate,
                            previousSelectedEndDate
                        )
                    )
                }

                val datePicker = datePickerBuilder.build()

                /**
                 * OnClickListener for the date picker OK button
                 */
                datePicker.addOnPositiveButtonClickListener {
                    baseClassViewModel.onDateRangeSelected(it.first, it.second)
                    previousSelectedStartDate = it.first ?: 0
                    previousSelectedEndDate = it.second ?: 0

                    val rangeString = "${formatDate(previousSelectedStartDate)} - ${formatDate(previousSelectedEndDate)}"
                    actvPeriod.setText(rangeString, false)
                }

                datePicker.addOnCancelListener {
                    resetSelection(actvPeriod)
                }

                datePicker.addOnNegativeButtonClickListener {
                    resetSelection(actvPeriod)
                }

                datePicker.show(childFragmentManager, "")

                Log.d("GUS", "Show date range picker...")
            } else {
                previousSelectedPeriodIndex = idx
                baseClassViewModel.onPeriodDropdownItemSelected(idx)
            }
        }
    }

    /**
     * Call this method to format a date
     */
    private fun formatDate(date: Long): String{
        val dateFormat = android.text.format.DateFormat.getDateFormat(context)
        return dateFormat.format(date)
    }

    /**
     * Call this method if material date picker was cancelled
     */
    private fun resetSelection(actvPeriod: AutoCompleteTextView){
        /** If previous index is not null we need to select that item*/
        if(previousSelectedPeriodIndex!= null) {
            actvPeriod.setText(periodAdapter.getItem(previousSelectedPeriodIndex!!).toString(), false)
        }else{
            /** If it is null, we need to set the text to the prev. date range */
            val rangeString = "${formatDate(previousSelectedStartDate)} - ${formatDate(previousSelectedEndDate)}"
            actvPeriod.setText(rangeString, false)
        }
    }
}