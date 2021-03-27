package com.diamont.expense.tracker.util.interfaces
/**
 * If a fragment wants to handle the back button
 * and receive a callback it needs to implement this interface.
 *
 * onBackPressed() should return true if the button press is handled by the fragment.
 * If it return false, the activity will handle it.
 *
 * The lambda passed to onBackPressed() has no parameters and has to return nothing.
 */
interface BackPressCallbackFragment {
    fun onBackPressed(listener : () -> Unit) : Boolean
}





