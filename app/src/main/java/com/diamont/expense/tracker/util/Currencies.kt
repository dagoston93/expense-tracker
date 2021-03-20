package com.diamont.expense.tracker.util

/**
 * This data class holds our available currencies
 */
data class Currency(
    val id : Int,
    val name : String,
    val sign : String
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
        val availableCurrencies : List<Currency> = listOf(
            Currency(0,"USD", "$"),
            Currency(1,"GBP", "£"),
            Currency(2,"HUF", "Ft"),
            Currency(3,"EUR", "€")
        )
    }
}

