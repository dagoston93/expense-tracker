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
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.TextViewCompat
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.*
import com.diamont.expense.tracker.util.database.Transaction
import com.diamont.expense.tracker.util.database.TransactionCategory

class TransactionDetailsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    /**
     * Declare the required variables
     */
    private var editIconColor : Int = 0
    private var deleteIconColor : Int = 0
    private var titleTextAppearance : Int = 0
    private var labelTextAppearance : Int = 0
    private lateinit var transaction : Transaction
    private var category = TransactionCategory(0,"", android.R.color.black)

    /**
     * The required views
     */
    private var ivShowMoreIcon : ImageView
    private var ivEditIcon : ImageView
    private var ivDeleteIcon : ImageView
    private var ivStrip : ImageView
    private var ivTransaction : ImageView

    private var tvTitle : TextView
    private var tvAmount : TextView
    private var tvDateLabel : TextView
    private var tvDate : TextView
    private var tvCategoryLabel : TextView
    private var tvCategory : TextView
    private var tvVenueLabel : TextView
    private var tvVenue : TextView
    private var tvPaymentMethodLabel : TextView
    private var tvPaymentMethod : TextView
    private var tvTransactionType : TextView
    private var tvTransactionTypeLabel : TextView
    private var tvIsPlanned : TextView
    private var tvIsPlannedLabel : TextView
    private var tvFrequency : TextView
    private var tvFrequencyLabel : TextView

    var clExpandable : ConstraintLayout
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
                editIconColor = getColor(R.styleable.TransactionDetailsView_transactionDetailEditIconColor, Color.BLACK)
                deleteIconColor = getColor(R.styleable.TransactionDetailsView_transactionDetailDeleteIconColor, Color.BLACK)

                titleTextAppearance = getResourceId(R.styleable.TransactionDetailsView_transactionDetailTitleTextAppearance,
                R.style.TextAppearance_AppCompat_Title)

                labelTextAppearance = getResourceId(R.styleable.TransactionDetailsView_transactionDetailLabelTextAppearance,
                    R.style.TextAppearance_AppCompat)

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
        ivTransaction = root.findViewById<ImageView>(R.id.ivTransactionDetailIcon) as ImageView

        tvTitle = root.findViewById<TextView>(R.id.tvTransactionDetailTitle) as TextView
        tvAmount = root.findViewById<TextView>(R.id.tvTransactionDetailAmount) as TextView
        tvDate = root.findViewById<TextView>(R.id.tvTransactionDetailDate) as TextView
        tvDateLabel = root.findViewById<TextView>(R.id.tvTransactionDetailDateLabel) as TextView
        tvCategory = root.findViewById<TextView>(R.id.tvTransactionDetailCategory) as TextView
        tvCategoryLabel = root.findViewById<TextView>(R.id.tvTransactionDetailCategoryLabel) as TextView
        tvVenue = root.findViewById<TextView>(R.id.tvTransactionDetailVenue) as TextView
        tvVenueLabel = root.findViewById<TextView>(R.id.tvTransactionDetailVenueLabel) as TextView
        tvPaymentMethod = root.findViewById<TextView>(R.id.tvTransactionDetailPaymentMethod) as TextView
        tvPaymentMethodLabel = root.findViewById<TextView>(R.id.tvTransactionDetailPaymentMethodLabel) as TextView
        tvTransactionType = root.findViewById<TextView>(R.id.tvTransactionDetailType) as TextView
        tvTransactionTypeLabel = root.findViewById<TextView>(R.id.tvTransactionDetailTypeLabel) as TextView
        tvIsPlanned = root.findViewById<TextView>(R.id.tvTransactionDetailIsPlanned) as TextView
        tvIsPlannedLabel = root.findViewById<TextView>(R.id.tvTransactionDetailIsPlannedLabel) as TextView
        tvFrequency = root.findViewById<TextView>(R.id.tvTransactionDetailFrequency) as TextView
        tvFrequencyLabel = root.findViewById<TextView>(R.id.tvTransactionDetailFrequencyLabel) as TextView

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
        TextViewCompat.setTextAppearance(tvPaymentMethod, labelTextAppearance)
        TextViewCompat.setTextAppearance(tvPaymentMethodLabel, labelTextAppearance)
        TextViewCompat.setTextAppearance(tvTransactionType, labelTextAppearance)
        TextViewCompat.setTextAppearance(tvTransactionTypeLabel, labelTextAppearance)
        TextViewCompat.setTextAppearance(tvIsPlanned, labelTextAppearance)
        TextViewCompat.setTextAppearance(tvIsPlannedLabel, labelTextAppearance)
        TextViewCompat.setTextAppearance(tvFrequency, labelTextAppearance)
        TextViewCompat.setTextAppearance(tvFrequencyLabel, labelTextAppearance)

        /** Set the colors */
        ImageViewCompat.setImageTintList(ivEditIcon, ColorStateList.valueOf(editIconColor))
        ImageViewCompat.setImageTintList(ivDeleteIcon, ColorStateList.valueOf(deleteIconColor))

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
     * Call this method to set the transaction
     */
    fun setTransactionAndCategory(tran : Transaction, cat: TransactionCategory){
        Log.d("GUS", "data in")
        /** Save the transaction */
        this.transaction = tran
        this.category = cat

        /**
         * Set up the view according to the transaction
         */

        /** Set the strip color */
        ImageViewCompat.setImageTintList(ivStrip, ColorStateList.valueOf(
            ContextCompat.getColor(context, category.categoryColorResId)
        ))

        /**
         * Set up the always used text views
         */
        tvTitle.text = transaction.description
        tvAmount.text = transaction.getAmountString()
        tvDate.text = transaction.getDateString()
        tvTransactionType.text = context.resources.getString(transaction.transactionType.stringId)

        /**
         * Configure the rest according to the transaction type
         */
        if(transaction.transactionType == TransactionType.DEPOSIT || transaction.transactionType == TransactionType.WITHDRAW)
        {
            /** If withdraw/deposit we hide the unnecessary text fields */
            tvCategory.visibility = GONE
            tvCategoryLabel.visibility = GONE
            tvIsPlanned.visibility = GONE
            tvIsPlannedLabel.visibility = GONE
            tvFrequency.visibility = GONE
            tvFrequencyLabel.visibility = GONE
            tvVenue.visibility = GONE
            tvVenueLabel.visibility = GONE
            tvPaymentMethod.visibility = GONE
            tvPaymentMethodLabel.visibility = GONE

            /** Set the icon*/
            ivTransaction.setImageResource(
                if(transaction.transactionType == TransactionType.DEPOSIT){
                    R.drawable.ic_deposit
                }else{
                    R.drawable.ic_withdraw
                }
            )

            /** Set icon color */
            ImageViewCompat.setImageTintList(ivTransaction, ColorStateList.valueOf(
                ContextCompat.getColor(
                    context,
                    if(transaction.transactionType == TransactionType.WITHDRAW){
                        R.color.colorGoalNotAchieved
                    }else{
                        R.color.colorGoalAchieved
                    }
                )
            ))
        }else {
            /** Display the category title */
            tvCategory.visibility = VISIBLE
            tvCategoryLabel.visibility = VISIBLE
            tvCategory.text = category.categoryName

            /** Is planned */
            tvIsPlanned.visibility = VISIBLE
            tvIsPlannedLabel.visibility = VISIBLE
            tvIsPlanned.text = context.resources.getString(transaction.planned.stringId)

            /** If expense/income is not planned we hide frequency */
            if(transaction.planned == TransactionPlanned.PLANNED)
            {
                tvFrequency.visibility = VISIBLE
                tvFrequencyLabel.visibility = VISIBLE
                tvFrequency.text = context.resources.getString(transaction.frequency.stringId)
            }else{
                tvFrequency.visibility = GONE
                tvFrequencyLabel.visibility = GONE
            }

            /** Show the rest of the labels */
            tvVenue.visibility = VISIBLE
            tvVenueLabel.visibility = VISIBLE
            tvPaymentMethod.visibility = VISIBLE
            tvPaymentMethodLabel.visibility = VISIBLE

            tvVenue.text = transaction.secondParty
            tvPaymentMethod.text = context.resources.getString(transaction.method.stringId)

            /** Set them up depending on if it is expense or income */
            if(transaction.transactionType == TransactionType.INCOME){
                tvVenueLabel.text = context.resources.getString(R.string.source)
                tvPaymentMethodLabel.text = context.resources.getString(R.string.received_by)
            }else{
                tvVenueLabel.text = context.resources.getString(R.string.recipient_venue)
                tvPaymentMethodLabel.text = context.resources.getString(R.string.payment_method)
            }

            /** Set the icon */
            ivTransaction.setImageResource(
                if(transaction.method == PaymentMethod.CASH){
                    R.drawable.ic_cash
                }else{
                    R.drawable.ic_credit_card
                }
            )

            /** Set icon color */
            ImageViewCompat.setImageTintList(ivTransaction, ColorStateList.valueOf(
                ContextCompat.getColor(
                    context,
                    if(transaction.transactionType == TransactionType.EXPENSE){
                        R.color.colorGoalNotAchieved
                    }else{
                        R.color.colorGoalAchieved
                    }
                )
            ))
        }
    }
}
