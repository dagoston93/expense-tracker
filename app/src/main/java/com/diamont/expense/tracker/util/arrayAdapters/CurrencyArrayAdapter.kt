package com.diamont.expense.tracker.util.arrayAdapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.Currency

/**
 * This ArrayAdapter creates the layouts
 * for our exposed dropdown menu from our
 * available currencies
 */
class CurrencyArrayAdapter(context: Context,
                           private val currencyList : List<Currency>)
    : ArrayAdapter<Currency>(context, 0, currencyList) {

    /** Return the item at the given position */
    override fun getItem(position: Int): Currency? = currencyList[position]

    /** Create our views */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    /** Create the view from the Currency object */
    private fun createView(position: Int, convertView: View?, parent: ViewGroup) : TextView{
        val context = parent.context
        val view = (convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_simple_dropdown, parent, false)) as TextView
        view.text = currencyList[position].toString()
        return view
    }
}