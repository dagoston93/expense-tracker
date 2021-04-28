package com.diamont.expense.tracker


import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
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
import com.diamont.expense.tracker.util.LocaleUtil
import com.diamont.expense.tracker.util.interfaces.BackPressHandlerFragment
import java.util.*

class MainActivity : AppCompatActivity() {

    /**
     * Declare some objects we will need later
     */
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel : MainActivityViewModel

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
        val localisedContext = LocaleUtil.getLocalisedContext(application)
        val viewModelFactory = MainActivityViewModelFactory(localisedContext)

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

        /** Set the app language */
        LocaleUtil.setLocale(baseContext)
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
     * attachBaseContext()
     *
     * Set locale
     */
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleUtil.updateBaseContextLocale(newBase))
    }
}