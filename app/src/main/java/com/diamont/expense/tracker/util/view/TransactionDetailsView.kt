package com.diamont.expense.tracker.util.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.TextViewCompat
import com.diamont.expense.tracker.R

class TransactionDetailsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    /**
     * Declare the required variables
     */
    private var editIconColor : Int = 0
    private var deleteIconColor : Int = 0
    private var cancelIconColor : Int = 0
    private var titleTextAppearance : Int = 0
    private var labelTextAppearance : Int = 0
    private var nextDataFieldIndex: Int = 0

    /**
     * The required views
     */
    private var ivShowMoreIcon : ImageView
    private var ivEditIcon : ImageView
    private var ivCancelIcon : ImageView
    private var ivDeleteIcon : ImageView
    private var ivStrip : ImageView
    private var ivTransaction : ImageView

    private var tvTitle : TextView
    private var tvAmount : TextView

    private var labelTextViews = listOf<TextView>()
    private var dataTextViews = listOf<TextView>()

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
                cancelIconColor = getColor(R.styleable.TransactionDetailsView_transactionDetailCancelIconColor, Color.BLACK)

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
        ivCancelIcon = root.findViewById<ImageView>(R.id.ivTransactionDetailsCancel) as ImageView
        ivStrip = root.findViewById<ImageView>(R.id.ivTransactionDetailColoredStrip) as ImageView
        ivTransaction = root.findViewById<ImageView>(R.id.ivTransactionDetailIcon) as ImageView

        tvTitle = root.findViewById<TextView>(R.id.tvTransactionDetailTitle) as TextView
        tvAmount = root.findViewById<TextView>(R.id.tvTransactionDetailAmount) as TextView

        dataTextViews = listOf(
            root.findViewById<TextView>(R.id.tvTransactionDetailData1) as TextView,
            root.findViewById<TextView>(R.id.tvTransactionDetailData2) as TextView,
            root.findViewById<TextView>(R.id.tvTransactionDetailData3) as TextView,
            root.findViewById<TextView>(R.id.tvTransactionDetailData4) as TextView,
            root.findViewById<TextView>(R.id.tvTransactionDetailData5) as TextView,
            root.findViewById<TextView>(R.id.tvTransactionDetailData6) as TextView,
            root.findViewById<TextView>(R.id.tvTransactionDetailData7) as TextView,
            root.findViewById<TextView>(R.id.tvTransactionDetailData8) as TextView,
            root.findViewById<TextView>(R.id.tvTransactionDetailData9) as TextView,
            root.findViewById<TextView>(R.id.tvTransactionDetailData10) as TextView,
        )

        labelTextViews = listOf(
            root.findViewById<TextView>(R.id.tvTransactionDetailLabel1) as TextView,
            root.findViewById<TextView>(R.id.tvTransactionDetailLabel2) as TextView,
            root.findViewById<TextView>(R.id.tvTransactionDetailLabel3) as TextView,
            root.findViewById<TextView>(R.id.tvTransactionDetailLabel4) as TextView,
            root.findViewById<TextView>(R.id.tvTransactionDetailLabel5) as TextView,
            root.findViewById<TextView>(R.id.tvTransactionDetailLabel6) as TextView,
            root.findViewById<TextView>(R.id.tvTransactionDetailLabel7) as TextView,
            root.findViewById<TextView>(R.id.tvTransactionDetailLabel8) as TextView,
            root.findViewById<TextView>(R.id.tvTransactionDetailLabel9) as TextView,
            root.findViewById<TextView>(R.id.tvTransactionDetailLabel10) as TextView
        )

        clExpandable = root.findViewById<ConstraintLayout>(R.id.clTransactionDetailsExpandable) as ConstraintLayout
        llContainer = root.findViewById<LinearLayout>(R.id.llTransactionDetailsContainer) as LinearLayout

        /** Set text appearances */
        TextViewCompat.setTextAppearance(tvTitle, titleTextAppearance)
        TextViewCompat.setTextAppearance(tvAmount, titleTextAppearance)

        for(i in dataTextViews.indices){
            TextViewCompat.setTextAppearance(labelTextViews[i], labelTextAppearance)
            TextViewCompat.setTextAppearance(dataTextViews[i], labelTextAppearance)
        }

        /** Set the colors */
        ImageViewCompat.setImageTintList(ivEditIcon, ColorStateList.valueOf(editIconColor))
        ImageViewCompat.setImageTintList(ivDeleteIcon, ColorStateList.valueOf(deleteIconColor))
        ImageViewCompat.setImageTintList(ivCancelIcon, ColorStateList.valueOf(cancelIconColor))

        /** Hide the cancel icon */
        ivCancelIcon.visibility = GONE

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
     * Call this method to set an onClickListener for the cancel icon and sets its visibility
     */
    fun setCancelIconOnClickListener(listener : () -> Unit){
        ivCancelIcon.setOnClickListener {
            listener()
        }

        /** If onClickListener added we show the icon */
        ivCancelIcon.visibility = VISIBLE
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
     * Call this method to add a field of data
     *
     * @param label The label to display.
     * @param data  The value to display.
     */
    fun addDataField(label: String, data: String){
        /** Check if we have any more fields available. If not, return */
        if(nextDataFieldIndex == labelTextViews.size) return

        /** Set the next field visibility and texts */
        labelTextViews[nextDataFieldIndex].visibility = VISIBLE
        labelTextViews[nextDataFieldIndex].text = label

        dataTextViews[nextDataFieldIndex].visibility = VISIBLE
        dataTextViews[nextDataFieldIndex].text = data

        /** Increase next index */
        nextDataFieldIndex++
    }

    /**
     * Call this method to reset the fields
     */
    fun resetFields(){
        /** First hide all text views */
        for(i in labelTextViews.indices){
            labelTextViews[i].visibility = GONE
            dataTextViews[i].visibility = GONE
        }

        /** Hide cancel icon */
        ivCancelIcon.visibility = GONE

        /** Reset the next field index */
        nextDataFieldIndex = 0
    }

    /**
     * Call this method to set the icon and its color
     *
     * @param iconResId The drawable resource id of the required icon.
     * @param colorResId The required color resource id.
     */
    fun setIcon(iconResId: Int, colorResId: Int){
        ivTransaction.setImageResource(iconResId)

        ImageViewCompat.setImageTintList(ivTransaction, ColorStateList.valueOf(
            ContextCompat.getColor(context, colorResId)
        ))
    }

    /**
     * Call this method to set the strip color
     *
     * @param colorResId The required color resource id.
     */
    fun setStripColor(colorResId: Int){
        ImageViewCompat.setImageTintList(ivStrip, ColorStateList.valueOf(
            ContextCompat.getColor(context, colorResId)
        ))
    }

    /**
     * Call this method to set the title and the amount
     *
     * @param title The title to be displayed.
     * @param amount The amount to be displayed.
     */
    fun setTitleAndAmount(title: String, amount: String){
        tvTitle.text = title
        tvAmount.text = amount
    }


    /*
    /**
     * Call this method to set the transaction
     */
    fun setTransactionAndCategory(tran : Transaction, cat: TransactionCategory){
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
        tvAmount.text = transaction.getAmountString(decimalFormat)
        tvDate.text = transaction.getDateString(context)

        /**
         * Configure the rest according to the transaction type
         */
        if(transaction.transactionType == TransactionType.DEPOSIT || transaction.transactionType == TransactionType.WITHDRAW) {
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

            /** Set transaction type and label */
            tvTransactionTypeLabel.text = context.resources.getString(R.string.transaction_type)
            tvTransactionType.text = context.resources.getString(transaction.transactionType.stringId)

            /** Set date label */
            tvDateLabel.text = context.resources.getString(R.string.date)

            /** Hide status */
            tvStatus.visibility = GONE
            tvStatusLabel.visibility = GONE

        }else if(transaction.transactionType == TransactionType.INCOME || transaction.transactionType == TransactionType.EXPENSE){
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

            /** Set transaction type and label */
            tvTransactionTypeLabel.text = context.resources.getString(R.string.transaction_type)
            tvTransactionType.text = context.resources.getString(transaction.transactionType.stringId)

            /** Set date label */
            tvDateLabel.text = context.resources.getString(R.string.date)

            /** Hide status */
            tvStatus.visibility = GONE
            tvStatusLabel.visibility = GONE

        }else if(transaction.transactionType == TransactionType.PLAN_EXPENSE || transaction.transactionType == TransactionType.PLAN_INCOME){
            /** Display the category title */
            tvCategory.visibility = VISIBLE
            tvCategoryLabel.visibility = VISIBLE
            tvCategory.text = category.categoryName

            /** Is planned */
            tvIsPlanned.visibility = GONE
            tvIsPlannedLabel.visibility = GONE

            /** Set transaction type and label */
            tvTransactionTypeLabel.text = context.resources.getString(R.string.plan_type)
            tvTransactionType.text = context.resources.getString(
                if(transaction.transactionType == TransactionType.PLAN_EXPENSE){
                    R.string.expense
                }else{
                    R.string.income
                }
            )

            /** Set up source/venue label depending on if it is expense or income */
            if(transaction.transactionType == TransactionType.PLAN_INCOME){
                tvVenueLabel.text = context.resources.getString(R.string.source)
                tvPaymentMethodLabel.text = context.resources.getString(R.string.receive_by)
            }else{
                tvVenueLabel.text = context.resources.getString(R.string.recipient_venue)
                tvPaymentMethodLabel.text = context.resources.getString(R.string.payment_method)
            }

            /** Set the transaction frequency */
            tvFrequency.visibility = VISIBLE
            tvFrequencyLabel.visibility = VISIBLE
            tvFrequency.text = context.resources.getString(transaction.frequency.stringId)

            /** Set the date label depending on transaction frequency */
            if(transaction.frequency == TransactionFrequency.MONTHLY_SUM
                || transaction.frequency == TransactionFrequency.FORTNIGHTLY_SUM
                || transaction.frequency == TransactionFrequency.WEEKLY_SUM
                || transaction.frequency == TransactionFrequency.YEARLY_SUM){

                tvDateLabel.text = context.resources.getString(R.string.first_date)
            }else{
                tvDateLabel.text = context.resources.getString(R.string.expected_date)
            }

            /** Show status if it is a repeated expense */
            if(transaction.frequency == TransactionFrequency.ONE_TIME){
                tvStatus.visibility = GONE
                tvStatusLabel.visibility = GONE
            }else{
                tvStatus.visibility = VISIBLE
                tvStatusLabel.visibility = VISIBLE

                tvStatus.text = context.resources.getString(
                    if(transaction.planId == 1){
                        R.string.active
                    }else{
                        R.string.inactive
                    }
                )
            }


            /** Set the icon*/
            ivTransaction.setImageResource(
                if(transaction.transactionType == TransactionType.PLAN_INCOME){
                    R.drawable.ic_income
                }else{
                    R.drawable.ic_expense
                }
            )

            /** Set icon color */
            ImageViewCompat.setImageTintList(ivTransaction, ColorStateList.valueOf(
                ContextCompat.getColor(
                    context,
                    if(transaction.transactionType == TransactionType.PLAN_EXPENSE){
                        R.color.colorGoalNotAchieved
                    }else{
                        R.color.colorGoalAchieved
                    }
                )
            ))

        }
    }*/
}
