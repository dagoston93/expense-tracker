package com.diamont.expense.tracker.util.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.diamont.expense.tracker.R

class TransactionDetailsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var isExpanded = false
    private var expandedHeight : Int = 0
    private var collapsedHeight : Int = 0

    init{
        /** Inflate the layout */
        val root : View = inflate(context, R.layout.view_transaction_details, this)
        root.findViewById<ImageView>(R.id.ivTransactionDetailsShowMore).setOnClickListener {
            root.findViewById<ConstraintLayout>(R.id.clTransactionDetailsExpandable).visibility =
                if(isExpanded) { View.GONE } else {View.VISIBLE}
                isExpanded = !isExpanded

                Log.d("GUSTI", "height: $collapsedHeight")
        }

    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if(collapsedHeight == 0) {
            collapsedHeight = this.height
        }
    }

}
