package com.diamont.expense.tracker.util

/**
 * If a fragment wants to handle the back button
 * it needs to implement this interface.
 *
 * onBackPressed() should return true if the button press is handled by the fragment.
 * If it return false, the activity will handle it.
 */

interface BackPressHandlerFragment {
    fun onBackPressed() : Boolean
}