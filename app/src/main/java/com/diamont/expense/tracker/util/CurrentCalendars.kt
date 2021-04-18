package com.diamont.expense.tracker.util

import java.util.*

class CurrentCalendars {

    /**
     * Get some calendar instances
     */
    private val _calendarStartOfMonth = Calendar.getInstance()
    val calendarStartOfMonth: Calendar
        get() = _calendarStartOfMonth

    private val _calendarEndOfMonth = Calendar.getInstance()
    val calendarEndOfMonth: Calendar
        get() = _calendarEndOfMonth

    private val _calendarStartOfYear = Calendar.getInstance()
    val calendarStartOfYear: Calendar
        get() = _calendarStartOfYear

    private val _calendarEndOfYear = Calendar.getInstance()
    val calendarEndOfYear: Calendar
        get() = _calendarEndOfYear

    /**
     * The constructor
     */
    init {
        /**
         * Find first and last day of month
         */
        _calendarStartOfMonth.set(Calendar.HOUR, 0)
        _calendarStartOfMonth.set(Calendar.MINUTE, 0)
        _calendarStartOfMonth.set(Calendar.SECOND, 0)
        _calendarStartOfMonth.set(Calendar.DAY_OF_MONTH, 1)

        _calendarEndOfMonth.set(Calendar.HOUR, 23)
        _calendarEndOfMonth.set(Calendar.MINUTE, 59)
        _calendarEndOfMonth.set(Calendar.SECOND, 59)
        _calendarEndOfMonth.set(
            Calendar.DAY_OF_MONTH,
            _calendarEndOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        )

        /**
         * Find first and last day of year
         */
        _calendarStartOfYear.set(Calendar.HOUR, 0)
        _calendarStartOfYear.set(Calendar.MINUTE, 0)
        _calendarStartOfYear.set(Calendar.SECOND, 0)
        _calendarStartOfYear.set(Calendar.DAY_OF_MONTH, 1)
        _calendarStartOfYear.set(Calendar.MONTH, Calendar.JANUARY)

        _calendarEndOfYear.set(Calendar.HOUR, 23)
        _calendarEndOfYear.set(Calendar.MINUTE, 59)
        _calendarEndOfYear.set(Calendar.SECOND, 59)
        _calendarEndOfYear.set(
            Calendar.DAY_OF_YEAR,
            _calendarEndOfYear.getActualMaximum(Calendar.DAY_OF_YEAR)
        )
    }
}