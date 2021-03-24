package com.diamont.expense.tracker.util.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.TextViewCompat
import com.diamont.expense.tracker.R

/**
 * This class displays a circular progress bar
 */
class CircularProgressBar(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    /** Some variables */
    private var progress: Int = 0
    private var backgroundColorValue: Int = 0
    private var foregroundColorValue: Int = 0
    private var textAppearanceId: Int = 0
    private var pbProgressBar: ProgressBar
    private var tvProgressText: TextView
    private var ivProgressBarBg: ImageView

    init {
        /** Receive the attributes */
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CircularProgressBar,
            R.attr.circularProgressBarStyle,
            R.style.Theme_ExpenseTracker_Widget_CircularProgressBar
        ).apply {
            try {
                progress = getInt(R.styleable.CircularProgressBar_circularProgressBarProgress, 0)
                backgroundColorValue = getColor(
                    R.styleable.CircularProgressBar_circularProgressBarBackgroundColor,
                    0xffffff
                )
                foregroundColorValue =
                    getColor(R.styleable.CircularProgressBar_circularProgressBarForegroundColor, 0)
                textAppearanceId = getResourceId(
                    R.styleable.CircularProgressBar_circularProgressBarTextAppearance,
                    R.style.TextAppearance_AppCompat_Large
                )
            } finally {
                recycle()
            }
        }

        /** Inflate the layout */
        val root: View = View.inflate(context, R.layout.view_circular_progress_bar, this)
        pbProgressBar = root.findViewById(R.id.pbCircularProgressBar) as ProgressBar
        tvProgressText = root.findViewById(R.id.tvCircularProgressBarText) as TextView
        ivProgressBarBg = root.findViewById(R.id.ivCircularProgressBarBg) as ImageView

        /** Set the colors */
        ImageViewCompat.setImageTintList(
            ivProgressBarBg,
            ColorStateList.valueOf(backgroundColorValue)
        )
        pbProgressBar.progressTintList = ColorStateList.valueOf(foregroundColorValue)

        /** Set text appearance */
        TextViewCompat.setTextAppearance(tvProgressText, textAppearanceId)

        /** Set the progress and text */
        tvProgressText.text = "$progress %"

        /**
         * Validate progress value before setting it on the progress bar
         * This way we are able to display as text over 100 %.
         */
        if (progress > 100) {
            pbProgressBar.progress = 100
        } else if (progress < 0) {
            pbProgressBar.progress = 0
        } else {
            pbProgressBar.progress = progress
        }

    }

    /** Set progress */
    fun setCircularProgressBarProgress(prog: Int) {
        /** Check if within range */
        if (prog !in 0..100) return

        progress = prog

        /** Set the progress and text */
        tvProgressText.text = "$progress %"
        pbProgressBar.progress = progress
    }
}