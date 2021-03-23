package com.diamont.expense.tracker

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.size
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.diamont.expense.tracker.databinding.FragmentMainAppBinding
import com.diamont.expense.tracker.util.BackPressHandlerFragment

class MainAppFragment : Fragment(), BackPressHandlerFragment{
    /** Data binding */
    private lateinit var binding : FragmentMainAppBinding

    /** The nav controller */
    private lateinit var navController : NavController

    /** Get the Activity View Model */
    private val activityViewModel : MainActivityViewModel by activityViewModels {
        MainActivityViewModelFactory(
            requireNotNull(this.activity).application
        )
    }

    /**
     * onCreateView()
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /** Inflate the layout for this fragment */
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_app, container, false)
        binding.lifecycleOwner = this

        /** Set up bottom nav */
        binding.bottomNavView.background = null
        binding.bottomNavView.menu.getItem(2).isEnabled = false

        /** Get the navigation controller */
         navController = childFragmentManager.findFragmentByTag("main_app_nav")!!.findNavController()

        /** Setup onCLickListener for the FAB */
        binding.fabAdd.setOnClickListener{
            navigateWithAnimation(R.id.addOrEditTransactionFragment, R.anim.anim_add_open, R.anim.anim_fade_out)
        }

        /** Setup the onClickListener for the bottom nav view menu items */
        binding.bottomNavView.setOnNavigationItemSelectedListener {
            setActiveMenuItemIcon(it)
            navigateWithSlideAnimation(it)
            true
        }

        /** Observe bottom nav bar visibility */
        activityViewModel.isBottomNavBarVisible.observe(viewLifecycleOwner, Observer {
            binding.coordLoBottomNav.visibility = if(it){View.VISIBLE}else{View.GONE}
        })

        /** Return the inflated layout */
        return binding.root
    }

    /**
     * Call this method to change the icon of the active
     * item selected on the bottom app bar
     *
     * @param menuItem - The menu item to set active.
     */
    private fun setActiveMenuItemIcon(menuItem: MenuItem){
        val menu = binding.bottomNavView.menu

        /** First reset the other menu item icons */
        menu.findItem(R.id.menu_btm_home).setIcon(R.drawable.ic_home_outline)
        menu.findItem(R.id.menu_btm_history).setIcon(R.drawable.ic_history_outline)
        menu.findItem(R.id.menu_btm_plan).setIcon(R.drawable.ic_plan_outline)
        menu.findItem(R.id.menu_btm_statistic).setIcon(R.drawable.ic_statistic_outline)

        /** Now set the filled icon for the active item */
        when(menuItem.itemId){
            R.id.menu_btm_home -> menu.findItem(R.id.menu_btm_home).setIcon(R.drawable.ic_home_filled)
            R.id.menu_btm_history -> menu.findItem(R.id.menu_btm_history).setIcon(R.drawable.ic_history_filled)
            R.id.menu_btm_plan -> menu.findItem(R.id.menu_btm_plan).setIcon(R.drawable.ic_plan_filled)
            R.id.menu_btm_statistic -> menu.findItem(R.id.menu_btm_statistic).setIcon(R.drawable.ic_statistic_filled)
        }
    }

    /**
     * Call this method to navigate left/right with slide in animation
     *
     * @param newMenuItem - The id of the menu item. It has to be the same
     * as the id of the destination in the navigation graph.
     */
    private fun navigateWithSlideAnimation(newMenuItem: MenuItem){
        val menu = binding.bottomNavView.menu
        val currentDestinationId :Int = navController.currentDestination?.id ?: R.id.menu_btm_home

        /**
         * Find out whether destination is left or right of our current position or is it the same.
         * The id of the menu items are the same as the navigation destination ids.
         */
        var currentDestPos : Int = 0
        var newDestPos : Int = 0

        for(i in 0 until menu.size()){
            if(menu.getItem(i).itemId == currentDestinationId){
                currentDestPos = i
            }
            if(menu.getItem(i).itemId == newMenuItem.itemId){
                newDestPos = i
            }
        }

        /** If positions are equal we don't need to navigate */
       if(currentDestPos == newDestPos) return

        /** If new pos is greater than old, it is right of the new */
        var enterAnimId : Int = 0
        var exitAnimId : Int = 0

        if(newDestPos > currentDestPos)
        {
            enterAnimId =R.anim.anim_slide_in_from_right
            exitAnimId =R.anim.anim_slide_out_to_left
        }else{
            enterAnimId =R.anim.anim_slide_in_from_left
            exitAnimId =R.anim.anim_slide_out_to_right
        }

        navigateWithAnimation(newMenuItem.itemId, enterAnimId, exitAnimId)
    }

    /**
     * Call this method to navigate adding animation
     *
     * @param destinationId - The resource id of the destination. (id in navigation resource)
     * @param enterAnimId - The id of the animation for the new fragment entering
     * @param exitAnimId - The id of the animation for the old fragment exiting
     */
    private fun navigateWithAnimation(destinationId : Int, enterAnimId : Int, exitAnimId : Int){
        val navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setEnterAnim(enterAnimId)
            .setExitAnim(exitAnimId)
            .build()

        navController.navigate(destinationId, null, navOptions)
    }

    /**
     * Handle the back button presses
     */
    override fun onBackPressed(): Boolean {
        /*/** Check if the currently loaded fragment implements BackPressHandlerFragment*/
        val fragment = childFragmentManager.fragments?.get(0)

        /** If yes we call it's onBackPressed() */
        if(fragment is BackPressHandlerFragment)
        {
            /** If it did not handle the button press we let the default behaviour to happen */
            if((fragment as? BackPressHandlerFragment)?.onBackPressed() != true){
                activity?.onBackPressed()
            }
        }else{
            /** If it does not implement it, we let the default behaviour to happen */
            activity?.onBackPressed()
        }
*/
        return true
    }
}