package com.diamont.expense.tracker.util

import android.widget.AutoCompleteTextView

/**
 * This method checks if the text of an AutoCompleteTextView matches
 * an item of a string list.
 *
 * Returns: if match found: the item index in the list
 *          if no match: null
 */
fun AutoCompleteTextView.getStringListIndexFromText(list : List<String>) : Int?{
    for(i in list.indices){
        if(this.text.toString() == list[i]){
            return i
        }
    }

    return null
}