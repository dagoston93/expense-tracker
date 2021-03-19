package com.diamont.expense.tracker.util

class Currencies {
    companion object{
        val availableCurrencies : List<Currency> = listOf(
            Currency("USD", "$"),
            Currency("GBP", "£"),
            Currency("HUF", "Ft"),
            Currency("EUR", "€")
        )
    }
}

data class Currency(
    val name : String,
    val sign : String
)

