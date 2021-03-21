package com.diamont.expense.tracker.util.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
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
    private var dotSpacing: Int = 0
    private var numOfDots: Int = 0
    private var prevActiveDot: Int = 0
    private var activeDot: Int = 0
    private var activeDotSizeFactor : Float = 0f
    private var animTime : Long = 0
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
                dotSpacing = getDimensionPixelSize(R.styleable.DotIndicator_dotIndicatorDotSpacing, 16)
                numOfDots = getInt(R.styleable.DotIndicator_dotIndicatorNumberOfDots, 1)
                activeDot = getInt(R.styleable.DotIndicator_dotIndicatorActiveDot, 0)
                animTime = getInt(R.styleable.DotIndicator_dotIndicatorAnimTime, 500).toLong()
            } finally {
                recycle()
            }
        }

        /** Inflate the layout */
        val root : View = inflate(context, R.layout.view_dot_indicator, this)

        /** Calculate active dot size factor */
        activeDotSizeFactor = activeDotSize.toFloat()/inactiveDotSize.toFloat()

        /** Get the container */
        llContainer = root.findViewById(R.id.llDotIndicatorContainer)

        var params = llContainer.layoutParams
        params.height = (inactiveDotSize * activeDotSizeFactor).toInt()
        llContainer.layoutParams = params

        /** Set up the layoutParams */
        params = MarginLayoutParams(inactiveDotSize, inactiveDotSize)
        params.marginStart = dotSpacing/2
        params.marginEnd = dotSpacing/2

        /** Add the dots */
        for(i in 0 until numOfDots){
            /** Create the view for the dots */
            val llDot = ImageView(context)
            llDot.layoutParams = params

            /** Set background and attach to container */
            llDot.setBackgroundResource(R.drawable.dot_indicator_empty_dot)
            llDot.backgroundTintList = ColorStateList.valueOf(inactiveDotColorVal)
            llContainer.addView(llDot)
        }

        setDotIndicatorActiveDot(activeDot)

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
        for(i in prevActiveDot until dotIndex){
            val dot = (llContainer.getChildAt(i) as ImageView)

            /** Change the size */
            dot.animate().scaleX(1f/activeDotSizeFactor).duration = animTime
            dot.animate().scaleY(1f/activeDotSizeFactor).duration = animTime

            /** Change background and color */
            dot.setBackgroundResource(R.drawable.dot_indicator_full_dot)
            dot.backgroundTintList = ColorStateList.valueOf(completedDotColorVal)
        }

        /** Get the new active dot */
        val dot = (llContainer.getChildAt(dotIndex) as ImageView)

        /** Change the size */
        dot.animate().scaleX(activeDotSizeFactor).duration = animTime
        dot.animate().scaleY(activeDotSizeFactor).duration = animTime

        /** Change background and color */
        dot.setBackgroundResource(R.drawable.dot_indicator_full_dot)
        dot.backgroundTintList = ColorStateList.valueOf(activeDotColorVal)

        /** Save the current and previous active dot */
        prevActiveDot = activeDot
        activeDot = dotIndex

    }
}
