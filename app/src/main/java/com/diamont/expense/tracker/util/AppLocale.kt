package com.diamont.expense.tracker.util

import com.diamont.expense.tracker.R

class AppLocale(val localeString: String, val stringResId: Int) {
    companion object{
        val supportedLocales:List<AppLocale> = listOf<AppLocale>(
            AppLocale("en", R.string.english),
            AppLocale("hu", R.string.hungarian)
        )
    }
}