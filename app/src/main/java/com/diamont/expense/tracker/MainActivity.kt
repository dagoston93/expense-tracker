package com.diamont.expense.tracker


import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import com.diamont.expense.tracker.databinding.ActivityMainBinding
import com.diamont.expense.tracker.util.AppLocale
import com.diamont.expense.tracker.util.KEY_PREF_DARK_THEME_ENABLED
import com.diamont.expense.tracker.util.KEY_PREF_LOCALE
import com.diamont.expense.tracker.util.interfaces.BackPressHandlerFragment
import java.util.*

class MainActivity : AppCompatActivity() {

    /**
     * Declare some objects we will need later
     */
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel : MainActivityViewModel
    private lateinit var sysDefaultLocale: Locale


    /**
     * onCreate()
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**  Inflate the layout using data binding */
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        /** Set up toolbar */
        setSupportActionBar(binding.toolbar)

        /** Get our viewModel */
        val viewModelFactory = MainActivityViewModelFactory(application)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(MainActivityViewModel::class.java)

        /*** Apply dark theme if needed **/
        if(viewModel.sharedPreferences.getBoolean(KEY_PREF_DARK_THEME_ENABLED, false)) {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        }else{
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
        }

        /** Observe some live data from the view model */
        viewModel.actionbarTitle.observe(this, Observer {
            supportActionBar?.title = it
        })

        viewModel.isUpButtonVisible.observe(this, Observer {
            supportActionBar?.setDisplayHomeAsUpEnabled(it)
        })

        viewModel.isDrawerEnabled.observe(this, Observer {
            if(it){
                binding.toolbar.setNavigationIcon(R.drawable.ic_hamburger_menu)
            }
        })

        Log.d("GUS", "onCreate()")
        /** Set the app language */
        setLocale()
    }

    /**
     * Handle the back button press
     */
    override fun onBackPressed() {
        /** Check if the currently loaded fragment implements BackPressHandlerFragment*/
        val fragment = this.supportFragmentManager.findFragmentById(R.id.mainNavHostFragment) as NavHostFragment
        val currentFragment = fragment?.childFragmentManager?.fragments?.get(0)

        /** If yes we call it's onBackPressed() */
        if(currentFragment is BackPressHandlerFragment)
        {
            /** If it did not handle the button press we let the default behaviour to happen */
            if((currentFragment as? BackPressHandlerFragment)?.onBackPressed() != true){
                super.onBackPressed()
            }
        }else{
            /** If it does not implement it, we let the default behaviour to happen */
            super.onBackPressed()
        }
    }

    /**
     * Methods to set the locale
     */
    private fun getSavedLocale(context: Context?): Locale{
        val savedLocaleString = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_PREF_LOCALE, "") ?: ""
        //val savedLocaleString = viewModel.sharedPreferences.getString(KEY_PREF_LOCALE, "") ?: ""

        val appLocale = AppLocale.supportedLocales.find { it.localeString == savedLocaleString }

        Log.d("GUS", "${sysDefaultLocale}")

        return if(appLocale != null){
            Locale(savedLocaleString)
        }else{

            Locale(AppLocale.supportedLocales[0].localeString)
        }
    }

    private fun setLocale(){
        /** Get locale from shared prefs */
        val locale = getSavedLocale(baseContext)
        val configuration = baseContext.resources.configuration
        Locale.setDefault(locale)
        configuration.setLocale(locale);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N){
            applicationContext.createConfigurationContext(configuration);
        } else {
            resources.updateConfiguration(configuration, baseContext.resources.displayMetrics);
        }

        //baseContext.resources.updateConfiguration(configuration, baseContext.resources.displayMetrics)
    }

    override fun attachBaseContext(newBase: Context?) {
        sysDefaultLocale = Locale.getDefault()
        Log.d("GUS", "attachBaseContext()")
        super.attachBaseContext(updateBaseContextLocale(newBase))
    }

    private fun updateBaseContextLocale(context: Context?): Context?{
        val locale = getSavedLocale(context)

        return if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            updateResourcesLocale(context, locale)
        }else{
            updateResourcesLocaleLegacy(context, locale)
        }
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    private fun updateResourcesLocale(context: Context?, locale: Locale): Context? {
        val configuration = Configuration(context?.resources?.configuration)
        configuration.setLocale(locale)
        return context?.createConfigurationContext(configuration)
    }

    @Suppress("deprecation")
    private fun updateResourcesLocaleLegacy(context: Context?, locale: Locale): Context? {
        val configuration = context?.resources?.configuration
        configuration?.locale = locale
        context?.resources?.updateConfiguration(configuration, context.resources.displayMetrics)
        return context
    }

}