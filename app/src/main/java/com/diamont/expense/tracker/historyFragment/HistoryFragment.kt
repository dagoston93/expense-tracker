package com.diamont.expense.tracker.historyFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.diamont.expense.tracker.MainActivityViewModel
import com.diamont.expense.tracker.MainActivityViewModelFactory
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.databinding.FragmentHistoryBinding
import com.diamont.expense.tracker.util.*

class HistoryFragment : Fragment() {
    /** Data binding */
    private lateinit var binding : FragmentHistoryBinding

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_history, container, false)
        binding.lifecycleOwner = this

        /** Set up values for activity view model */
        activityViewModel.setTitle(getString(R.string.history))
        activityViewModel.setBottomNavBarVisibility(true)
        activityViewModel.setUpButtonVisibility(false)
        activityViewModel.setDrawerLayoutEnabled(true)

        /******
         * TEST
         */
        val tr1 = Transaction(
            0,
            TransactionType.EXPENSE,
            "Food shopping",
            15.47f,
            TransactionCategory(0,"Food", R.color.secondaryColor),
            "InterSparhelt",
            PaymentMethod.CASH,
            0,
            TransactionPlanned.PLANNED
        )

        val tr2 = Transaction(
            0,
            TransactionType.EXPENSE,
            "Clothes shopping",
            23.77f,
            TransactionCategory(0,"Clothes", R.color.circularProgressbarBackground),
            "Whatever shop",
            PaymentMethod.CARD,
            0,
            TransactionPlanned.NOT_PLANNED
        )

        val tr3 = Transaction(
            0,
            TransactionType.WITHDRAW,
            "Withdrawal",
            50.0f,
            TransactionCategory(0,"Unspecified", android.R.color.holo_blue_dark),
            "Whatever shop",
            PaymentMethod.CARD,
            0,
            TransactionPlanned.NOT_PLANNED
        )

        val tr4 = Transaction(
            0,
            TransactionType.DEPOSIT,
            "Deposit",
            23.77f,
            TransactionCategory(0,"Unspecified", android.R.color.holo_blue_dark),
            "Whatever shop",
            PaymentMethod.CARD,
            0,
            TransactionPlanned.NOT_PLANNED
        )

        binding.tran1.setTransaction(tr1)
        binding.tran2.setTransaction(tr2)
        binding.tran3.setTransaction(tr3)
        binding.tran4.setTransaction(tr4)


        /** Return the inflated layout */
        return binding.root
    }
    
}