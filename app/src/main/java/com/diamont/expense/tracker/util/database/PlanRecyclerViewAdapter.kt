package com.diamont.expense.tracker.util.database

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.enums.TransactionFrequency
import com.diamont.expense.tracker.util.enums.TransactionType
import java.text.DecimalFormat

/**
 * This adapter class helps recycler view
 * to display Plan data in our
 * TransactionDetailView
 */
class PlanRecyclerViewAdapter(
    private val recyclerView: RecyclerView,
    private val decimalFormat: DecimalFormat,
    tvNoItems: TextView,
    private val editIconCallback: (id: Int) -> Unit,
    private val deleteIconCallback: (id: Int, description: String, typeStringId: Int, dateLabel: String, date: String, position: Int) -> Unit,
    private val cancelIconCallback: (id: Int, description: String, typeStringId: Int, date: String, position: Int) -> Unit
): TransactionDetailViewAdapter<Plan>(recyclerView, tvNoItems) {

    /**
     * This method is called by parent when view and item needs to be bound together
     */
    override fun bind(holder: ViewHolder, item: Plan, position: Int) {
        /** First reset the view */
        holder.view.resetFields()

        /** Set the color of the strip */
        val category = categories.find { it.categoryId == item.categoryId }

        if(category != null){
            holder.view.setStripColor(category.categoryColorResId)
        }

        /** Set the title and the amount */
        holder.view.setTitleAndAmount(
            item.description,
            item.getAmountString(decimalFormat)
        )

        /** Set the date */
        if(item.frequency == TransactionFrequency.ONE_TIME){
            if(item.isStatusActive) {
                /** Active one time plan -> Show expected date */
                holder.view.addDataField(
                    recyclerView.context.getString(R.string.expected_date),
                    item.getDateString(item.firstExpectedDate, recyclerView.context)
                )
            }else{
                /** Completed one time plan -> Show completion date */
                holder.view.addDataField(
                    recyclerView.context.getString(R.string.completed_on),
                    item.getDateString(item.lastCompletedDate, recyclerView.context)
                )
            }
        }else{
            if(item.isStatusActive) {
                /** Active regular plan -> Show next expected date */
                holder.view.addDataField(
                    recyclerView.context.getString(R.string.next_date),
                    item.getDateString(item.nextExpectedDate, recyclerView.context)
                )
            }else{
                /** Cancelled regular plan -> Show last completed date */
                holder.view.addDataField(
                    recyclerView.context.getString(R.string.last_date),
                    if(item.lastCompletedDate == 0L){
                        recyclerView.context.getString(R.string.never)
                    }else{
                        item.getDateString(item.lastCompletedDate, recyclerView.context)
                    }
                )
            }
        }

        /** Set the icon and color */
        var iconResId: Int = R.drawable.ic_cash
        var iconColorId: Int = R.color.primaryColor

        if(item.transactionType == TransactionType.PLAN_EXPENSE){
            iconResId = R.drawable.ic_expense
            iconColorId = R.color.colorGoalNotAchieved
        }else{
            iconResId = R.drawable.ic_income
            iconColorId = R.color.colorGoalAchieved
        }

        holder.view.setIcon(iconResId, iconColorId)

        /** Set category */
        holder.view.addDataField(
            recyclerView.context.getString(R.string.category),
            category?.categoryName ?: ""
        )

        /** Show frequency */
        holder.view.addDataField(
            recyclerView.context.getString(R.string.frequency),
            recyclerView.context.getString(item.frequency.stringId)
        )

        /** Show first date if it is not a one time plan */
        if(item.frequency != TransactionFrequency.ONE_TIME){
            holder.view.addDataField(
                recyclerView.context.getString(R.string.first_date),
                item.getDateString(item.firstExpectedDate, recyclerView.context)
            )
        }

        /** Show last date if it is an active regular plan */
        if(item.frequency != TransactionFrequency.ONE_TIME && item.isStatusActive){
            holder.view.addDataField(
                recyclerView.context.getString(R.string.last_date),
                if(item.lastCompletedDate == 0L){
                    recyclerView.context.getString(R.string.never)
                }else{
                    item.getDateString(item.lastCompletedDate, recyclerView.context)
                }
            )
        }

        /** Show status */
        holder.view.addDataField(
            recyclerView.context.getString(R.string.status),
            recyclerView.context.getString(
                if(item.isStatusActive){
                    R.string.active
                }else{
                    if(item.frequency == TransactionFrequency.ONE_TIME){
                        R.string.completed
                    }else{
                        R.string.cancelled
                    }
                }
            )
        )

        /** If cancelled regular plan, show date of cancellation */
        if(item.frequency != TransactionFrequency.ONE_TIME && !item.isStatusActive){
            holder.view.addDataField(
                recyclerView.context.getString(R.string.cancelled_on),
                item.getDateString(item.cancellationDate, recyclerView.context)
            )
        }

        /** Show recipient/source */
        holder.view.addDataField(
            recyclerView.context.getString(
                if(item.transactionType == TransactionType.PLAN_EXPENSE){
                    R.string.recipient_venue
                }else{
                    R.string.source
                }
            ),
            item.sourceOrRecipient
        )

        /** Show method */
        holder.view.addDataField(
            recyclerView.context.getString(
                if(item.transactionType == TransactionType.PLAN_EXPENSE){
                    R.string.payment_method
                }else{
                    R.string.receive_by
                }
            ),
            recyclerView.context.getString(item.method.stringId)
        )
        
        /** Set the onClickListeners for the icons */
        holder.view.setEditIconOnClickListener {
            editIconCallback(item.id)
        }

        holder.view.setDeleteIconOnClickListener {
            deleteIconCallback(
                item.id,
                item.description,
                item.transactionType.stringId,
                recyclerView.context.getString(
                    if(item.frequency == TransactionFrequency.ONE_TIME){
                        R.string.expected_date
                    }else{
                        R.string.first_date
                    }
                ),

                item.getDateString(item.firstExpectedDate, recyclerView.context),
                position
            )
        }

        /** If plan is not a one time plan and it is active  we add the cancel feature */
        if(item.frequency != TransactionFrequency.ONE_TIME && item.isStatusActive) {
            holder.view.setCancelIconOnClickListener {
                cancelIconCallback(
                    item.id,
                    item.description,
                    item.transactionType.stringId,
                    item.getDateString(item.firstExpectedDate, recyclerView.context),
                    position
                )
            }
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

    /**
     * Call this method if user cancels an item
     */
    fun itemCancelledAtPos(position: Int){
        items[position].isStatusActive = false
        notifyItemChanged(position)
    }
}