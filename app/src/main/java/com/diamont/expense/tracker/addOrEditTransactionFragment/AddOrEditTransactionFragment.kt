package com.diamont.expense.tracker.addOrEditTransactionFragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.diamont.expense.tracker.MainActivityViewModel
import com.diamont.expense.tracker.MainActivityViewModelFactory
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.databinding.FragmentAddOrEditTransactionBinding
import com.diamont.expense.tracker.util.BackPressHandlerFragment

class AddOrEditTransactionFragment : Fragment(), BackPressHandlerFragment {
    /** Data binding */
    private lateinit var binding : FragmentAddOrEditTransactionBinding

    /** Get the Activity View Model */
    private val activityViewModel : MainActivityViewModel by activityViewModels {
        MainActivityViewModelFactory(
            requireNotNull(this.activity).application
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /** Inflate the layout for this fragment */
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_or_edit_transaction, container, false)
        binding.lifecycleOwner = this

        /** Set up values for activity view model */
        activityViewModel.setTitle("Add")
        activityViewModel.setBottomNavBarVisibility(false)
        activityViewModel.setUpButtonVisibility(true)

        /** We need to have it true to be able to handle the up button */
        setHasOptionsMenu(true)

        /** Return the inflated layout */
        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home ->{
                binding.root.findNavController().navigateUp()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed(): Boolean {
        binding.root.findNavController().navigateUp()
        return true
    }
}