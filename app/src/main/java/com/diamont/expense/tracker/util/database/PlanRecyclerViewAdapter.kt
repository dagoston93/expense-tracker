package com.diamont.expense.tracker.util.database

import androidx.recyclerview.widget.RecyclerView
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
    private val deleteIconCallback: (id: Int, description: String, typeStringId: Int, date: String, position: Int) -> Unit
): TransactionDetailViewAdapter<Plan>(recyclerView) {

    /**
     * This method is called by parent when view and item needs to be bound together
     */
    override fun bind(holder: ViewHolder, item: Plan, position: Int) {

    }
}