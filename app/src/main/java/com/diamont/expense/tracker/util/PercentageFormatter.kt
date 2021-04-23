package com.diamont.expense.tracker.util

import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

class PercentageFormatter: ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return "%.0f%%".format(value)
    }
}