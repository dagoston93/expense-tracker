package com.diamont.expense.tracker.util

import android.view.View

fun boolToVisibility(boolean: Boolean) : Int {
    return if(boolean){
        View.VISIBLE
    }else{
        View.GONE
    }
}