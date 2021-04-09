package com.diamont.expense.tracker.util.database

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.enums.PaymentMethod
import com.diamont.expense.tracker.util.enums.TransactionType
import com.diamont.expense.tracker.util.view.TransactionDetailsView
import java.text.DecimalFormat

/**
 * This abstract class helps to create recycler view adapters
 * for Transaction and Plan objects
 */
abstract class TransactionDetailViewAdapter<T: TransactionDetailViewAdaptable>
    (private val recyclerView: RecyclerView
) : RecyclerView.Adapter<TransactionDetailViewAdapter.ViewHolder>() {

    var items  = mutableListOf<T>()
        set(value){
            field = value
            notifyDataSetChanged()
        }
    var categories = listOf<TransactionCategory>()
    var expandedPosition : Int = -1

    /**
     * This method needs to return the data count
     */
    override fun getItemCount(): Int = items.size

    /**
     * This method is responsible for binding data to views
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        bind(holder, item, position)
    }

    /**
     * Create new view holder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {
        return ViewHolder.from(parent)
    }

    /**
     *  This method keeps track of the currently expanded transaction detail view
     */
    protected fun transactionOnClick(isExpanded: Boolean, position: Int) {
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
     * Call this method if user deletes an item
     */
    fun itemDeletedAtPos(position: Int){
        /** If item was deleted close the open view */
        expandedPosition = -1
        items.removeAt(position)
        notifyDataSetChanged()
    }

    /**
     * This method is responsible for binding the
     * data to the view and setting the onClickListeners
     */
    protected abstract fun bind(holder: ViewHolder, item: T, position: Int)

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