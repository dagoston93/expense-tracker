package com.diamont.expense.tracker.util

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

@SuppressLint("ClickableViewAccessibility")
fun addHideKeyboardOnTouchListenerToView(view : View, activity : Activity, editTextArray : Array<EditText>? = null){
    /** Add onTouchListeners only to non editText views */
    if(view !is EditText){
        view.setOnTouchListener { _, _ ->
            hideSoftKeyboard(activity)
            /** If it is not an editText field, we remove the focus of the given editText fields */
            if(editTextArray != null){
                for(editText in editTextArray){
                    editText.isFocusable = false
                }

                for(editText in editTextArray){
                    editText.clearFocus()
                }

                for(editText in editTextArray){
                    editText.isFocusable = true
                    editText.isFocusableInTouchMode = true
                }
            }

            false
        }
    }

    if(view is ViewGroup){
        for(i in 0 until (view as ViewGroup).childCount){
            val innerView = ((view as ViewGroup).getChildAt(i)) as View
            addHideKeyboardOnTouchListenerToView(innerView, activity)
        }
    }
}

fun hideSoftKeyboard(activity : Activity){
    val inputMethodManager : InputMethodManager =
        (activity.getSystemService(Activity.INPUT_METHOD_SERVICE)) as InputMethodManager

    inputMethodManager.hideSoftInputFromWindow(
        activity.currentFocus?.windowToken,
        0
    )
}