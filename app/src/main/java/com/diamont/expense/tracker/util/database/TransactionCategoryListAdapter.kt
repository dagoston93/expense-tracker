package com.diamont.expense.tracker.util.database

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.diamont.expense.tracker.R

class TransactionCategoryListAdapter(
    private val editIconCallback: (id: Int) -> Unit,
    private val deleteIconCallback: (id: Int, name: String, position: Int) -> Unit
): ListAdapter<TransactionCategory, TransactionCategoryListAdapter.ViewHolder>(TransactionCategoryDiffCallback()) {
     /**
     * This method is responsible for binding data to views
     */
    override fun onBindViewHolder(holder: TransactionCategoryListAdapter.ViewHolder, position: Int) {
        val item = getItem(position)
        bind(holder, item, position)
    }

    /**
     * Create new view holder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : TransactionCategoryListAdapter.ViewHolder {
        return TransactionCategoryListAdapter.ViewHolder.from(parent)
    }

    /**
     * This method is responsible for binding the
     * data to the view and setting the onClickListners
     */
    private fun bind(holder: TransactionCategoryListAdapter.ViewHolder, item: TransactionCategory, position: Int){
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
        tvCategoryName.text = getItem(position).categoryName
        ImageViewCompat.setImageTintList(ivCategoryColorStrip, ColorStateList.valueOf(
            ContextCompat.getColor(holder.view.context, getItem(position).categoryColorResId)
        ))

        /**
         * Add onClickListeners
         */
        ivEditIcon.setOnClickListener {
            editIconCallback(getItem(position).categoryId)
        }

        ivDeleteIcon.setOnClickListener {
            deleteIconCallback(getItem(position).categoryId, getItem(position).categoryName, position)
        }
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

/**
 * DiffUtil callback class
 */
class TransactionCategoryDiffCallback : DiffUtil.ItemCallback<TransactionCategory>(){
    override fun areItemsTheSame(oldItem: TransactionCategory, newItem: TransactionCategory): Boolean {
        return oldItem.categoryId == newItem.categoryId
    }

    override fun areContentsTheSame(oldItem: TransactionCategory, newItem: TransactionCategory): Boolean {
        return oldItem == newItem
    }
}