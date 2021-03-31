package com.diamont.expense.tracker.util.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.diamont.expense.tracker.R

class ColorPicker (context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs){

    private var selectedColor: Int = 0
    private val colorList = listOf<Int>(
        R.color.category_color1,
        R.color.category_color2,
        R.color.category_color3,
        R.color.category_color4,
        R.color.category_color5,
        R.color.category_color6,
        R.color.category_color7,
        R.color.category_color8,
        R.color.category_color9,
        R.color.category_color10,
        R.color.category_color11,
        R.color.category_color12,
        R.color.category_color13,
        R.color.category_color14,
        R.color.category_color15,
        R.color.category_color16,
    )

    private val linearLayoutList : List<LinearLayout>

    /**
     * Constructor
     */
    init{
        /** Inflate the layout */
        val root : View = inflate(context, R.layout.view_color_picker, this)

        /**
         * Find the required layout
         */
        linearLayoutList = listOf(
            root.findViewById<LinearLayout>(R.id.llColorPickerColor1) as LinearLayout,
            root.findViewById<LinearLayout>(R.id.llColorPickerColor2) as LinearLayout,
            root.findViewById<LinearLayout>(R.id.llColorPickerColor3) as LinearLayout,
            root.findViewById<LinearLayout>(R.id.llColorPickerColor4) as LinearLayout,
            root.findViewById<LinearLayout>(R.id.llColorPickerColor5) as LinearLayout,
            root.findViewById<LinearLayout>(R.id.llColorPickerColor6) as LinearLayout,
            root.findViewById<LinearLayout>(R.id.llColorPickerColor7) as LinearLayout,
            root.findViewById<LinearLayout>(R.id.llColorPickerColor8) as LinearLayout,
            root.findViewById<LinearLayout>(R.id.llColorPickerColor9) as LinearLayout,
            root.findViewById<LinearLayout>(R.id.llColorPickerColor10) as LinearLayout,
            root.findViewById<LinearLayout>(R.id.llColorPickerColor11) as LinearLayout,
            root.findViewById<LinearLayout>(R.id.llColorPickerColor12) as LinearLayout,
            root.findViewById<LinearLayout>(R.id.llColorPickerColor13) as LinearLayout,
            root.findViewById<LinearLayout>(R.id.llColorPickerColor14) as LinearLayout,
            root.findViewById<LinearLayout>(R.id.llColorPickerColor15) as LinearLayout,
            root.findViewById<LinearLayout>(R.id.llColorPickerColor16) as LinearLayout
        )

        /**
         * Set onClickListeners for the linear layouts
         */
        for(i in linearLayoutList.indices){
            linearLayoutList[i].setOnClickListener {
                setSelectedColorByIndex(i)
            }
        }

    }

    /**
     * Call this method to set the selected color by an index in the list
     */
    private fun setSelectedColorByIndex(index: Int){
        /** Check if index is valid */
        if(index > colorList.size-1) return

        /** First reset the previously selected border then set the new one */
        linearLayoutList[selectedColor].setBackgroundResource(0)

        selectedColor = index
        linearLayoutList[selectedColor].setBackgroundResource(R.drawable.bg_border)
    }

    /**
     * Call this method to set the background color
     * given by a color resource id
     */
    fun setSelectedColorByResourceId(resId: Int){
        var index = 0

        for(i in colorList.indices){
            if(colorList[i] == resId){
                index = i
                break
            }
        }

        setSelectedColorByIndex(index)
    }
}