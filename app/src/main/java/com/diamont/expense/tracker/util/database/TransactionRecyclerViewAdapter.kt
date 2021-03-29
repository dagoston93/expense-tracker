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
class TransactionRecyclerViewAdapter
    (private val recyclerView: RecyclerView,
     private val editIconCallback: (id: Int) -> Unit
) : RecyclerView.Adapter<TransactionRecyclerViewAdapter.ViewHolder>() {
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
    override fun onBindViewHolder(holder: TransactionRecyclerViewAdapter.ViewHolder, position: Int) {
        val item = transactions[position]
        bind(holder, item, position)
    }

    /**
     * Create new view holder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : TransactionRecyclerViewAdapter.ViewHolder {
        return TransactionRecyclerViewAdapter.ViewHolder.from(parent)
    }

    /**
     * This method is responsible for binding the
     * data to the view and setting the onClickListners
     */
    private fun bind(
        holder: ViewHolder,
        item: Transaction,
        position: Int
    ) {
        holder.view.setTransactionAndCategory(
            item,
            categories.find { it.categoryId == item.categoryId } ?: TransactionCategory()
        )

        holder.view.setEditIconOnClickListener {
            editIconCallback(item.transactionId)
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
            transactionOnClick(isExpanded, position)
        }
    }

    /**
     *  This method keeps track of the currently expanded transaction detail view
     */
    private fun transactionOnClick(isExpanded: Boolean, position: Int) {
        /**
         * If this view was expanded we are closing it, so expanded position will be set to -1
         * If another view was expanded we save the new expanded position and also the old one
         * */
        var prevExpanded = -1
        if (isExpanded) {
            expandedPosition = -1
        } else {
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
        if (prevExpanded != -1) {
            notifyItemChanged(prevExpanded)
        }
    }

    /**
     * RecyclerView and the adapter needs this ViewHolder class
     */
    class ViewHolder private constructor(val view: TransactionDetailsView): RecyclerView.ViewHolder(view){
        companion object {
            /**
             * Creates a ViewHolder from the given view group
             * for our TransactionDetailView
             */
            fun from(viewGroup: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val layout = layoutInflater.inflate(
                    R.layout.item_transaction_detail,
                    viewGroup,
                    false
                ) as TransactionDetailsView

                return ViewHolder(layout)
            }
        }
    }
}

