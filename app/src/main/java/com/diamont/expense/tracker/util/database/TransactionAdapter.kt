package com.diamont.expense.tracker.util.database

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.view.TransactionDetailsView

/**
 * This adapter class helps recycler view
 * to display Transaction data in our
 * TransactionDetailView
 */
class TransactionAdapter(private val recyclerView: RecyclerView) : RecyclerView.Adapter<TransactionDetailsViewHolder>() {
    var transactions  = listOf<Transaction>()
    set(value){
        field = value
        notifyDataSetChanged()
    }

    var categories = listOf<TransactionCategory>()
    var expandedPosition : Int = -1

    /**
     * This method needs to return the data count
     */
    override fun getItemCount(): Int = transactions.size

    /**
     * This method is responsible for binding data to views
     */
    override fun onBindViewHolder(holder: TransactionDetailsViewHolder, position: Int) {
        val item = transactions[position]
        holder.view.setTransactionAndCategory(
            item,
            categories.find {it.categoryId == item.categoryId} ?: TransactionCategory()
        )

        /** Set the visibility of the content */
        val isExpanded = position == expandedPosition
        holder.view.clExpandable.visibility = if(isExpanded) {
                View.VISIBLE
            }else{
                View.GONE
            }

        /** Set the onClickListener for the show more icon*/
        holder.view.setOnClickListener {
            /**
             * If this view was expanded we are closing it, so expanded position will be set to -1
             * If another view was expanded we save the new expanded position and also the old one
             * */
            var prevExpanded = -1
            if(isExpanded){
                expandedPosition = -1
            }else{
                prevExpanded = expandedPosition
                expandedPosition = position
            }

            /** Start the transition (this will be animated) */
            TransitionManager.beginDelayedTransition(recyclerView)

            /**
             *  Notify recycler view that this view is changed
             *  And if a different one was open before notify
             *  that that one has changed too
             */
            notifyItemChanged(position)
            if(prevExpanded != -1)
            {
                notifyItemChanged(prevExpanded)
            }
        }
    }

    /**
     * Create new view holder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : TransactionDetailsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val layout = layoutInflater.inflate(
            R.layout.item_transaction_detail,
            parent,
            false
        ) as TransactionDetailsView

        return TransactionDetailsViewHolder(layout)

    }
}

class TransactionDetailsViewHolder(val view: TransactionDetailsView): RecyclerView.ViewHolder(view)