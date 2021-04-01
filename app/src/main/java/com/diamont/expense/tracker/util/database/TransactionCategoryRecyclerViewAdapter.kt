package com.diamont.expense.tracker.util.database

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.view.TransactionDetailsView

class TransactionCategoryRecyclerViewAdapter(
    private val editIconCallback: (id: Int, position: Int) -> Unit,
    private val deleteIconCallback: (id: Int, name: String, position: Int) -> Unit
): RecyclerView.Adapter<TransactionCategoryRecyclerViewAdapter.ViewHolder>() {

    var categories  = mutableListOf<TransactionCategory>()
        set(value){
            field = value
            notifyDataSetChanged()
        }

    /**
     * This method needs to return the data count
     */
    override fun getItemCount(): Int = categories.size

    /**
     * This method is responsible for binding data to views
     */
    override fun onBindViewHolder(holder: TransactionCategoryRecyclerViewAdapter.ViewHolder, position: Int) {
        val item = categories[position]
        bind(holder, item, position)
    }

    /**
     * Create new view holder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : TransactionCategoryRecyclerViewAdapter.ViewHolder {
        return TransactionCategoryRecyclerViewAdapter.ViewHolder.from(parent)
    }

    /**
     * This method is responsible for binding the
     * data to the view and setting the onClickListners
     */
    private fun bind(holder: TransactionCategoryRecyclerViewAdapter.ViewHolder, item: TransactionCategory, position: Int){
        /** Get the views */
        val tvCategoryName = holder.view.findViewById<TextView>(R.id.tvCategoryName) as TextView
        val ivCategoryColorStrip = holder.view.findViewById<ImageView>(R.id.ivCategoryColorStrip) as ImageView
        val ivEditIcon = holder.view.findViewById<ImageView>(R.id.ivCategoryEdit) as ImageView
        val ivDeleteIcon = holder.view.findViewById<ImageView>(R.id.ivCategoryDelete) as ImageView

        /** If position is 0 (category 'Unspecified') we hide the delete icon */
        if(position == 0){
            ivDeleteIcon.visibility = View.INVISIBLE
        }

        /** Set the text and the color */
        tvCategoryName.text = categories[position].categoryName
        ImageViewCompat.setImageTintList(ivCategoryColorStrip, ColorStateList.valueOf(
            ContextCompat.getColor(holder.view.context, categories[position].categoryColorResId)
        ))

        /**
         * Add onClickListeners
         */
        ivEditIcon.setOnClickListener {
            editIconCallback(categories[position].categoryId, position)
        }

    }

    /**
     * Call this method from add/edit dialog if an item changes
     */
    fun itemChanged(position: Int, newItem: TransactionCategory)
    {
        categories[position] = newItem
        notifyItemChanged(position)
    }
    /**
     * Call this method if user deletes an item
     */
    fun itemDeletedAtPos(position: Int){
        /** If item was deleted close the open view */
        categories.removeAt(position)
        notifyDataSetChanged()
    }

    /**
     * RecyclerView and the adapter needs this ViewHolder class
     */
    class ViewHolder private constructor(val view: ConstraintLayout): RecyclerView.ViewHolder(view){
        companion object {
            /**
             * Creates a ViewHolder from the given view group
             * for our TransactionDetailView
             */
            fun from(viewGroup: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val layout = layoutInflater.inflate(
                    R.layout.item_category,
                    viewGroup,
                    false
                ) as ConstraintLayout

                return ViewHolder(layout)
            }
        }
    }
}