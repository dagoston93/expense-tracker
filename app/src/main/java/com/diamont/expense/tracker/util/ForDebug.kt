package com.diamont.expense.tracker.util

import java.util.*

fun calendarToString(calendar: Calendar): String{
    return "${calendar.get(Calendar.YEAR)}. ${months[calendar.get(Calendar.MONTH)]}. ${calendar.get(Calendar.DAY_OF_MONTH)} - " +
            "${calendar.get(Calendar.HOUR)}:${calendar.get(Calendar.MINUTE)}:${calendar.get(Calendar.SECOND)}"
}

val months = arrayOf("January", "February", "March", "April", "May", "June", "July",
    "August", "September", "October", "November", "December")