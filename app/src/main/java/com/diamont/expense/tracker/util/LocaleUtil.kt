package com.diamont.expense.tracker.util

import android.annotation.TargetApi
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.preference.PreferenceManager
import com.diamont.expense.tracker.R
import java.util.*

/**
 * This class helps setting the correct language within the app
 *
 * see the following links:
 * - https://stackoverflow.com/questions/4985805/set-locale-programmatically
 * - https://medium.com/swlh/the-all-in-one-guide-for-changing-app-locale-dynamically-in-android-kotlin-d2506e5535d0
 */
class LocaleUtil(base: Context) : ContextWrapper(base) {
    companion object {

        /**
         * The list of the supported languages
         */
        val supportedLocales:List<AppLocale> = listOf<AppLocale>(
            AppLocale("en", R.string.english),
            AppLocale("hu", R.string.hungarian)
        )

        /**
         * This retrieves the locale to use.
         * If user selection is saved in shared prefs the user selection will be retrieved,
         * otherwise the device default.
         *
         * This way if the user selected default or left it as default, if the device language is
         * supported that will be used, if not, English. If user selected a language that one will be used.
         */
        private fun getSavedLocale(context: Context?): Locale {
            val savedLocaleString = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_PREF_LOCALE, "") ?: ""
            val appLocale = supportedLocales.find { it.localeString == savedLocaleString }

            return if (appLocale != null) {
                Locale(savedLocaleString)
            } else {
                val systemLocale = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                    Resources.getSystem().configuration.locales.get(0)
                } else {
                    Resources.getSystem().configuration.locale
                }
                systemLocale
            }
        }

        /**
         * Call this method in activity's onCreate() method, otherwise
         * language selection does not work on older devices. (Below API level 25.)
         */
        fun setLocale(baseContext: Context) {
            /** Get locale from shared prefs */
            val locale = getSavedLocale(baseContext)

            val configuration = baseContext.resources.configuration
            Locale.setDefault(locale)
            configuration.setLocale(locale)

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                baseContext.createConfigurationContext(configuration)
            } else {
                baseContext.resources.updateConfiguration(
                    configuration,
                    baseContext.resources.displayMetrics
                );
            }
        }

        /**
         * Call this method from activity's and the application class's
         * attachBaseContext() method otherwise language selection will not
         * work on newer devices...
         */
        fun updateBaseContextLocale(context: Context?): Context? {
            val locale = getSavedLocale(context)

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                return updateResourcesLocale(context, locale)
            } else {
                return updateResourcesLocaleLegacy(context, locale)
            }
        }

        /**
         * Update locale of resources on newer devices
         */
        @TargetApi(Build.VERSION_CODES.N_MR1)
        private fun updateResourcesLocale(context: Context?, locale: Locale): Context? {
            val configuration = Configuration(context?.resources?.configuration)
            configuration.setLocale(locale)
            return context?.createConfigurationContext(configuration)
        }

        /**
         * Update locale of resources on older devices
         */
        @Suppress("deprecation")
        private fun updateResourcesLocaleLegacy(context: Context?, locale: Locale): Context? {
            val configuration = context?.resources?.configuration
            configuration?.locale = locale
            context?.resources?.updateConfiguration(configuration, context.resources.displayMetrics)
            return context
        }

        /**
         * Call this method to get localise resources
         */
        fun getLocalisedResources(context: Context): Resources{
            /** Our input context cannot be null so the return value of updateBaseContextLocale() shall not be null either */
            return updateBaseContextLocale(context)!!.resources
        }

        /**
         * Call this method to get localise context
         */
        fun getLocalisedContext(context: Context): Context{
            /** Our input context cannot be null so the return value of updateBaseContextLocale() shall not be null either */
            return updateBaseContextLocale(context)!!
        }
    }
}