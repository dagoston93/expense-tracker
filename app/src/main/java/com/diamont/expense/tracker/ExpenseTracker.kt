package com.diamont.expense.tracker

import android.app.Application
import android.content.Context
import com.diamont.expense.tracker.util.LocaleUtil

/**
 * We need it for devices with Android Nougat or higher
 * Also need to add it to android manifest
 */
class ExpenseTracker: Application() {
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleUtil.updateBaseContextLocale(newBase))
    }
}