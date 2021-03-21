package com.diamont.expense.tracker


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import com.diamont.expense.tracker.databinding.ActivityMainBinding
import com.diamont.expense.tracker.util.*

class MainActivity : AppCompatActivity() {

    /**
     * Declare some objects we will need later
     */
    private lateinit var binding: ActivityMainBinding

    /**
     * onCreate()
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**  Inflate the layout using data binding */
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        /** Set up toolbar */
        setSupportActionBar(binding.toolbar)
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

}