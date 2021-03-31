package com.diamont.expense.tracker.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

/**
 * This data class holds our available currencies
 */
data class Currency(
    val id : Int,
    val name : String,
    val sign : String,
    val format: String,
    /** If null, it uses default by the system locale */
    val groupingSeparator: Char? = null,
    val decimalSeparator: Char? = null
){
    override fun toString(): String {
        return "$name($sign)"
    }

    /**
     * By adding an id, it is possible to change the order of display
     * while maintaining the database valid.
     *
     * Do not change the ids!!!
     */
    companion object{

        /**
         * This method returns a currency given by its id
         */
        fun getCurrencyById(currencyId: Int) : Currency?{
            return availableCurrencies.find { it.id == currencyId }
        }

        /**
         * This method returns a DecimalFormat which will
         * format the currency as string properly
         */
        fun getDecimalFormat(currencyId: Int) : DecimalFormat?{
            val currency = getCurrencyById(currencyId)

            if(currency != null) {
                if (currency.groupingSeparator == null || currency.decimalSeparator == null)
                {
                    return DecimalFormat(currency.format)
                }else{
                    val symbols = DecimalFormatSymbols()
                    symbols.groupingSeparator = currency.groupingSeparator
                    symbols.decimalSeparator = currency.decimalSeparator

                    return DecimalFormat(currency.format, symbols)
                }
            }else{
                return DecimalFormat()
            }
        }

        /**
         * The list of currencies
         */
        val availableCurrencies : List<Currency> = listOf(
            Currency(0,"USD", "$", "$#,###.00", ',', '.'),
            Currency(1,"GBP", "£", "£#,###.00", ',', '.'),
            Currency(2,"HUF", "Ft", "# ### Ft", ' ', ','),
            Currency(3,"EUR", "€", "€#,###.00", ',', '.')
        )
    }
}

