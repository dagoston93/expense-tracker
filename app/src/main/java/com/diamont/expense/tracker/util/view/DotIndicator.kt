package com.diamont.expense.tracker.util.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import com.diamont.expense.tracker.R

/** TODO: ANIMATION!!! */
/**
 * DotIndicator
 *
 * This class is a custom view
 * that displays dots which represent
 * the page the user is on within a flow.
 */
class DotIndicator(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    /** Variables for received attributes */
    private var activeDotColorVal: Int = 0
    private var inactiveDotColorVal: Int = 0
    private var completedDotColorVal: Int = 0
    private var activeDotSize: Int = 0
    private var inactiveDotSize: Int = 0
    private var completedDotSize: Int = 0
    private var dotSpacing: Int = 0
    private var numOfDots: Int = 0
    private var prevActiveDot: Int = 0
    private var activeDot: Int = 0

    /** The required views */
    private var llContainer : LinearLayout

    /**
     * Constructor
     */
    init {
        /** Receive the attributes */
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.DotIndicator,
            R.attr.dotIndicatorStyle,
            R.style.Theme_ExpenseTracker_Widget_DotIndicator
        ).apply {
            try {
                activeDotColorVal = getColor(R.styleable.DotIndicator_dotIndicatorActiveDotColor, 0)
                inactiveDotColorVal = getColor(R.styleable.DotIndicator_dotIndicatorInactiveDotColor, 0)
                completedDotColorVal = getColor(R.styleable.DotIndicator_dotIndicatorCompletedDotColor, 0)
                activeDotSize = getDimensionPixelSize(R.styleable.DotIndicator_dotIndicatorActiveDotSize, 16)
                inactiveDotSize = getDimensionPixelSize(R.styleable.DotIndicator_dotIndicatorInactiveDotSize, 16)
                completedDotSize = getDimensionPixelSize(R.styleable.DotIndicator_dotIndicatorCompletedDotSize, 16)
                dotSpacing = getDimensionPixelSize(R.styleable.DotIndicator_dotIndicatorDotSpacing, 16)
                numOfDots = getInt(R.styleable.DotIndicator_dotIndicatorNumberOfDots, 1)
                activeDot = getInt(R.styleable.DotIndicator_dotIndicatorActiveDot, 0)

            } finally {
                recycle()
            }
        }

        /** Inflate the layout */
        val root : View = inflate(context, R.layout.view_dot_indicator, this)

        /** Get the container */
        llContainer = root.findViewById(R.id.llDotIndicatorContainer)

        val p = llContainer.layoutParams
        //p.width = activeDotSize
        p.height = activeDotSize
        llContainer.layoutParams = p

        Log.d("GUSTI", "${llContainer.layoutParams.height}")

        /** Set up the layoutParams */
        val params = LinearLayout.LayoutParams(inactiveDotSize, inactiveDotSize)
        //params.width = inactiveDotSize
        //params.height = inactiveDotSize
        params.marginStart = dotSpacing/2
        params.marginEnd = dotSpacing/2

        /** Add the dots */
        for(i in 0 until numOfDots){
            /** Create the view for the dots */
            val llDot = LinearLayout(context)
            llDot.layoutParams = params

            /** Set background and attach to container */
            llDot.setBackgroundResource(R.drawable.dot_indicator_empty_dot)
            llContainer.addView(llDot)
        }

        //setDotIndicatorActiveDot(activeDot)
    }

    /**
     * Call this method to set the active dot.
     * Because of the naming, it is compatible
     * with LiveData.
     *
     * @param dotIndex: the index of the active dot
     * If out of range, we return
     */
    fun setDotIndicatorActiveDot(dotIndex : Int){
        /** Check if within range */
        if(dotIndex > numOfDots - 1) return

        /** Set the previous dots as completed */
        /*for(i in prevActiveDot until dotIndex){
            val dot = (llContainer.getChildAt(i) as LinearLayout)

            /** Change the size */
            val params = dot.layoutParams
            params.width = inactiveDotSize
            params.height = inactiveDotSize
            dot.layoutParams = params

            /** Change background and color */
            dot.setBackgroundResource(R.drawable.dot_indicator_full_dot)

            dot.invalidate()
            dot.requestLayout()
        }*/

        /** Get the new active dot */
        val dot2 = (llContainer.getChildAt(dotIndex) as LinearLayout)

        /** Change the size */
        val params = dot2.layoutParams
        params.width=activeDotSize
        params.height=activeDotSize
        dot2.layoutParams = params

        //dot2.invalidate()
        dot2.requestLayout()

        /** Change background and color */
        dot2.setBackgroundResource(R.drawable.dot_indicator_full_dot)

        /** Save the current and previous active dot */
        prevActiveDot = activeDot
        activeDot = dotIndex

    }
}
