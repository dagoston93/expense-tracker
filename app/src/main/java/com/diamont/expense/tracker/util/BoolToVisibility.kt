package com.diamont.expense.tracker.util

import android.view.View

fun boolToVisibility(boolean: Boolean, useGone: Boolean = true) : Int {
    return if(boolean){
        View.VISIBLE
    }else{
        if(useGone){
            View.GONE
        }else{
            View.INVISIBLE
        }
    }
}