package com.diamont.expense.tracker.util.database

import android.view.View
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
    private val editIconCallback: (id: Int) -> Unit,
    private val deleteIconCallback: (id: Int, description: String, typeStringId: Int, dateLabel: String, date: String, position: Int) -> Unit
): TransactionDetailViewAdapter<Plan>(recyclerView) {

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
            holder.view.addDataField(
                recyclerView.context.getString(R.string.expected_date),
                item.getDateString(item.firstExpectedDate, recyclerView.context)
            )
        }else{
            holder.view.addDataField(
                recyclerView.context.getString(R.string.next_date),
                item.getDateString(item.nextExpectedDate, recyclerView.context)
            )
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
                item.id, item.description,
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