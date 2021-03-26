package com.diamont.expense.tracker.util.database

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.view.TransactionDetailsView

/**
 * This adapter class helps recycler view
 * to display Transaction data in our
 * TransactionDetailView
 */
class TransactionAdapter : RecyclerView.Adapter<TransactionDetailsViewHolder>() {
    var transactions  = listOf<Transaction>()
    set(value){
        field = value
        notifyDataSetChanged()
    }

    var categories = listOf<TransactionCategory>()

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