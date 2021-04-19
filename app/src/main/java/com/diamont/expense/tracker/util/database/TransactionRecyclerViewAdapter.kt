package com.diamont.expense.tracker.util.database

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.enums.PaymentMethod
import com.diamont.expense.tracker.util.enums.TransactionType
import java.text.DecimalFormat

/**
 * This adapter class helps recycler view
 * to display Transaction data in our
 * TransactionDetailView
 */
class TransactionRecyclerViewAdapter(
    private val recyclerView: RecyclerView,
    private val decimalFormat: DecimalFormat,
    tvNoItems: TextView,
    private val editIconCallback: (id: Int) -> Unit,
    private val deleteIconCallback: (id: Int, description: String, typeStringId: Int, date: String, position: Int) -> Unit
): TransactionDetailViewAdapter<Transaction>(recyclerView, tvNoItems) {
    /** We need a list of plans */
    var plans = listOf<Plan>()

    /**
     * This method is called by parent when view and item needs to be bound together
     */
    override fun bind(holder: ViewHolder, item: Transaction, position: Int){
        /** First reset the view */
        holder.view.resetFields()

        /** Set the color of the strip */
        val category = categories.find { it.categoryId == item.categoryId }

        if(category != null){
            holder.view.setStripColor(category.categoryColorResId)
        }

        /** Get the plan */
        val plan = plans.find{ it.id == item.planId }

        /** Set the title and the amount */
        holder.view.setTitleAndAmount(
            item.description,
            item.getAmountString(decimalFormat)
        )

        /** Set the date */
        holder.view.addDataField(
            recyclerView.context.getString(R.string.date),
            item.getDateString(recyclerView.context)
        )

        /** Set the icon and color */
        var iconResId: Int = R.drawable.ic_cash
        var iconColorId: Int = R.color.primaryColor

        when(item.transactionType){
            /**
             * Expense
             */
            TransactionType.EXPENSE -> {
                iconResId = if(item.method == PaymentMethod.CASH){
                    R.drawable.ic_cash
                }else{
                    R.drawable.ic_credit_card
                }

                iconColorId = R.color.colorGoalNotAchieved
            }

            /**
             * Income
             */
            TransactionType.INCOME -> {
                iconResId = if(item.method == PaymentMethod.CASH){
                    R.drawable.ic_cash
                }else{
                    R.drawable.ic_credit_card
                }

                iconColorId = R.color.colorGoalAchieved
            }

            /**
             * Withdraw
             */
            TransactionType.WITHDRAW -> {
                iconResId = R.drawable.ic_withdraw
                iconColorId = R.color.colorGoalNotAchieved
            }

            /**
             * Deposit
             */
            TransactionType.DEPOSIT -> {
                iconResId = R.drawable.ic_deposit
                iconColorId = R.color.colorGoalAchieved
            }
        }

        holder.view.setIcon(iconResId, iconColorId)

        /** Display transaction type */
        holder.view.addDataField(
            recyclerView.context.getString(R.string.transaction_type),
            recyclerView.context.getString(item.transactionType.stringId)
        )

        /** Display the fields we only need for income/expense */
        if(item.transactionType == TransactionType.EXPENSE || item.transactionType == TransactionType.INCOME){
            /** Set category */
            holder.view.addDataField(
                recyclerView.context.getString(R.string.category),
                category?.categoryName ?: ""
            )

            /** Display the plan if it is planned otherwise show 'Not planned' */
            holder.view.addDataField(
                recyclerView.context.getString(R.string.plan_colon),
                plan?.description ?: recyclerView.context.getString(R.string.not_planned)
            )

            /** Show recipient/source */
            holder.view.addDataField(
                recyclerView.context.getString(
                    if(item.transactionType == TransactionType.EXPENSE){
                        R.string.recipient_venue
                    }else{
                        R.string.source
                    }
                ),
                item.secondParty
            )

            /** Show method */
            holder.view.addDataField(
                recyclerView.context.getString(
                    if(item.transactionType == TransactionType.EXPENSE){
                        R.string.payment_method
                    }else{
                        R.string.received_by
                    }
                ),
                recyclerView.context.getString(item.method.stringId)
            )
        }

        /** Set the onClickListeners for the icons */
        holder.view.setEditIconOnClickListener {
            editIconCallback(item.transactionId)
        }

        holder.view.setDeleteIconOnClickListener {
            deleteIconCallback(item.transactionId, item.description,
                item.transactionType.stringId, item.getDateString(recyclerView.context), position)
        }

        /** Set the visibility of the content */
        val isExpanded = position == expandedPosition
        holder.view.clExpandable.visibility = if (isExpanded) {
            View.VISIBLE
        } else {
            View.GONE
        }

        /** Set the onClickListener for the show more icon*/
        holder.view.setOnClickListener {
            super.transactionOnClick(isExpanded, position)
        }
    }
}