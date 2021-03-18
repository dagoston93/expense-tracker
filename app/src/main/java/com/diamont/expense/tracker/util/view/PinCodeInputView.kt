package com.diamont.expense.tracker.util.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.diamont.expense.tracker.R

class PinCodeInputView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    init{
        val root : View = inflate(context, R.layout.view_pin_code_input, this)
    }
}