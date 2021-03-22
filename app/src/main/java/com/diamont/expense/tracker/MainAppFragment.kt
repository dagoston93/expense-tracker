package com.diamont.expense.tracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.diamont.expense.tracker.databinding.FragmentMainAppBinding

class MainAppFragment : Fragment() {
    /** Data binding */
    private lateinit var binding : FragmentMainAppBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /** Inflate the layout for this fragment */
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_app, container, false)
        binding.lifecycleOwner = this

        /** Set uo bottom nav */
        binding.bottomNavView.background = null
        binding.bottomNavView.menu.getItem(2).isEnabled = false

        /** Connect bottom navigation view with navigation controller */
        val navController = childFragmentManager.findFragmentByTag("main_app_nav")!!.findNavController()
        NavigationUI.setupWithNavController(binding.bottomNavView, navController)

        /** Set up the icon change to filled icon on active tab */
        navController.addOnDestinationChangedListener(){ _, destination: NavDestination, _ ->
            setActiveMenuItemIcon(destination)
        }

        /** Setup onCLickListener for the fab */
        binding.fabAdd.setOnClickListener{
            navController.navigate(R.id.addOrEditTransactionFragment)
        }

        /** Return the inflated layout */
        return binding.root
    }

    /**
     * Call this method to change the icon of the active
     * item selected on the bottom app bar
     */
    private fun setActiveMenuItemIcon(destination : NavDestination){
        val menu = binding.bottomNavView.menu

        /** First reset the other menu item icons */
        menu.findItem(R.id.menu_btm_home).setIcon(R.drawable.ic_home_outline)
        menu.findItem(R.id.menu_btm_history).setIcon(R.drawable.ic_history_outline)
        menu.findItem(R.id.menu_btm_plan).setIcon(R.drawable.ic_plan_outline)
        menu.findItem(R.id.menu_btm_statistic).setIcon(R.drawable.ic_statistic_outline)

        /** Now set the filled icon for the active item */
        when(destination.id){
            R.id.menu_btm_home -> menu.findItem(R.id.menu_btm_home).setIcon(R.drawable.ic_home_filled)
            R.id.menu_btm_history -> menu.findItem(R.id.menu_btm_history).setIcon(R.drawable.ic_history_filled)
            R.id.menu_btm_plan -> menu.findItem(R.id.menu_btm_plan).setIcon(R.drawable.ic_plan_filled)
            R.id.menu_btm_statistic -> menu.findItem(R.id.menu_btm_statistic).setIcon(R.drawable.ic_statistic_filled)
        }

    }

}