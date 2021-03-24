package com.diamont.expense.tracker.util.view

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.view.doOnLayout
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.TextViewCompat
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.PaymentMethod
import com.diamont.expense.tracker.util.TransactionType

class TransactionDetailsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    /**
     * Declare the required variables
     */
    private var isExpanded = false
    private var expandedHeight : Int = 0

    private var stripColor : Int = 0
    private var editIconColor : Int = 0
    private var deleteIconColor : Int = 0
    private var titleTextAppearance : Int = 0
    private var labelTextAppearance : Int = 0
    private var title : String = ""
    private var amount : String = ""
    private var date : String = ""
    private var category : String = ""
    private var venue : String = ""
    private var paymentMethod : PaymentMethod = PaymentMethod.CASH
    private var transactionType : TransactionType = TransactionType.INCOME_ONE_TIME

    /**
     * The required views
     */
    private var ivShowMoreIcon : ImageView
    private var ivEditIcon : ImageView
    private var ivDeleteIcon : ImageView
    private var ivStrip : ImageView

    private var tvTitle : TextView
    private var tvAmount : TextView
    private var tvDateLabel : TextView
    private var tvDate : TextView
    private var tvCategoryLabel : TextView
    private var tvCategory : TextView
    private var tvVenueLabel : TextView
    private var tvVenue : TextView
    private var tvPaymentMehodLabel : TextView
    private var tvPaymentMehod : TextView




    private var clExpandable : ConstraintLayout
    private var llContainer : LinearLayout

    init{
        /** Receive the attributes */
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.TransactionDetailsView,
            R.attr.transactionDetailViewStyle,
            R.style.Theme_ExpenseTracker_Widget_TransactionDetailsView
        ).apply {
            try {
                stripColor = getColor(R.styleable.TransactionDetailsView_transactionDetailStripColor, Color.BLACK)
                editIconColor = getColor(R.styleable.TransactionDetailsView_transactionDetailEditIconColor, Color.BLACK)
                deleteIconColor = getColor(R.styleable.TransactionDetailsView_transactionDetailDeleteIconColor, Color.BLACK)

                titleTextAppearance = getResourceId(R.styleable.TransactionDetailsView_transactionDetailTitleTextAppearance,
                R.style.TextAppearance_AppCompat_Title)

                labelTextAppearance = getResourceId(R.styleable.TransactionDetailsView_transactionDetailLabelTextAppearance,
                    R.style.TextAppearance_AppCompat)

                title = getString(R.styleable.TransactionDetailsView_transactionDetailTitle) ?: ""
                amount = getString(R.styleable.TransactionDetailsView_transactionDetailAmount) ?: ""
                date = getString(R.styleable.TransactionDetailsView_transactionDetailDate) ?: ""
                category = getString(R.styleable.TransactionDetailsView_transactionDetailCategory) ?: ""
                venue = getString(R.styleable.TransactionDetailsView_transactionDetailVenue) ?: ""

                paymentMethod = PaymentMethod.fromInt(
                    getInt(R.styleable.TransactionDetailsView_transactionDetailPaymentMethod, 0)
                ) ?: PaymentMethod.CASH

                transactionType = TransactionType.fromInt(
                    getInt(R.styleable.TransactionDetailsView_transactionType, 0)
                ) ?: TransactionType.INCOME_ONE_TIME

            } finally {
                recycle()
            }
        }
        /** Inflate the layout */
        val root : View = inflate(context, R.layout.view_transaction_details, this)

        /** Get the required views */
        ivShowMoreIcon = root.findViewById<ImageView>(R.id.ivTransactionDetailsShowMore) as ImageView
        ivEditIcon = root.findViewById<ImageView>(R.id.ivTransactionDetailsEdit) as ImageView
        ivDeleteIcon = root.findViewById<ImageView>(R.id.ivTransactionDetailsDelete) as ImageView
        ivStrip = root.findViewById<ImageView>(R.id.ivTransactionDetailColoredStrip) as ImageView

        tvTitle = root.findViewById<TextView>(R.id.tvTransactionDetailTitle) as TextView
        tvAmount = root.findViewById<TextView>(R.id.tvTransactionDetailAmount) as TextView
        tvDate = root.findViewById<TextView>(R.id.tvTransactionDetailDate) as TextView
        tvDateLabel = root.findViewById<TextView>(R.id.tvTransactionDetailDateLabel) as TextView
        tvCategory = root.findViewById<TextView>(R.id.tvTransactionDetailCategory) as TextView
        tvCategoryLabel = root.findViewById<TextView>(R.id.tvTransactionDetailCategoryLabel) as TextView
        tvVenue = root.findViewById<TextView>(R.id.tvTransactionDetailVenue) as TextView
        tvVenueLabel = root.findViewById<TextView>(R.id.tvTransactionDetailVenueLabel) as TextView
        tvPaymentMehod = root.findViewById<TextView>(R.id.tvTransactionDetailPaymentMethod) as TextView
        tvPaymentMehodLabel = root.findViewById<TextView>(R.id.tvTransactionDetailPaymentMethodLabel) as TextView

        clExpandable = root.findViewById<ConstraintLayout>(R.id.clTransactionDetailsExpandable) as ConstraintLayout
        llContainer = root.findViewById<LinearLayout>(R.id.llTransactionDetailsContainer) as LinearLayout

        /** Set text appearances */
        TextViewCompat.setTextAppearance(tvTitle, titleTextAppearance)
        TextViewCompat.setTextAppearance(tvAmount, titleTextAppearance)

        TextViewCompat.setTextAppearance(tvDate, labelTextAppearance)
        TextViewCompat.setTextAppearance(tvDateLabel, labelTextAppearance)
        TextViewCompat.setTextAppearance(tvCategory, labelTextAppearance)
        TextViewCompat.setTextAppearance(tvCategoryLabel, labelTextAppearance)
        TextViewCompat.setTextAppearance(tvVenue, labelTextAppearance)
        TextViewCompat.setTextAppearance(tvVenueLabel, labelTextAppearance)
        TextViewCompat.setTextAppearance(tvPaymentMehod, labelTextAppearance)
        TextViewCompat.setTextAppearance(tvPaymentMehodLabel, labelTextAppearance)

        /** Set the texts */
        tvTitle.text = title
        tvAmount.text = amount
        tvDate.text = date
        tvCategory.text = category
        tvVenue.text = venue
        tvPaymentMehod.text = context.resources.getString(paymentMethod.stringId)

        /** Set the colors */
        ImageViewCompat.setImageTintList(ivStrip, ColorStateList.valueOf(stripColor))
        ImageViewCompat.setImageTintList(ivEditIcon, ColorStateList.valueOf(editIconColor))
        ImageViewCompat.setImageTintList(ivDeleteIcon, ColorStateList.valueOf(deleteIconColor))

        /** Set onClickListener for the expand/collapse button */
        ivShowMoreIcon.setOnClickListener {
            if(isExpanded) {
                collapse()
            }else{
                expand()
            }
            isExpanded = !isExpanded
        }

        /** Measure the expanded height of the content */
        this.doOnLayout {
            /** Save the expanded height of the content then set it to 0 */
            expandedHeight = clExpandable.height

            val params = clExpandable.layoutParams
            params.height = 0
            clExpandable.layoutParams = params

            clExpandable.visibility = INVISIBLE
        }
    }

    /**
     * Call this method to perform the expand animation
     */
    private fun expand(){
        /** Make content view visible */
        clExpandable.visibility = VISIBLE

        /** Set up the animator */
        val animator = ValueAnimator.ofInt(0, expandedHeight)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = 500

        /** Update the layout params as value is updated */
        animator.addUpdateListener {
            val params = clExpandable.layoutParams as MarginLayoutParams
            params.height = animator.animatedValue as Int
            clExpandable.layoutParams = params
        }

        /** Play the animation */
        animator.start()
    }

    /**
     * Call this method to perform the collapsing animation
     */
    private fun collapse(){
        /** Set up the animator */
        val animator = ValueAnimator.ofInt(expandedHeight, 0)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = 500

        /** Update the layout params as value is updated */
        animator.addUpdateListener {
            val params = clExpandable.layoutParams as MarginLayoutParams
            params.height = animator.animatedValue as Int
            clExpandable.layoutParams = params
        }

        /** Play the animation */
        animator.start()

        /** Hide content when animation is done */
        animator.doOnEnd {
            clExpandable.visibility = INVISIBLE
        }
    }

    /**
     * Call this method to set an onClickListener for the edit icon
     */
    fun setEditIconOnClickListener(listener : () -> Unit){
        ivEditIcon.setOnClickListener {
            listener()
        }
    }

    /**
     * Call this method to set an onClickListener for the delete icon
     */
    fun setDeleteIconOnClickListener(listener : () -> Unit){
        ivDeleteIcon.setOnClickListener {
            listener()
        }
    }

    /**
     * Call this method to change the strip color
     */
    fun setTransactionDetailStripColor(color : Int){
        stripColor = color
        ImageViewCompat.setImageTintList(ivStrip, ColorStateList.valueOf(stripColor))
    }
}
