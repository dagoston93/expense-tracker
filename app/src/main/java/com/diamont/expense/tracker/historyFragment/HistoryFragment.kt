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
        val cat1 = TransactionCategory(0,"Food", R.color.secondaryColor)
        val cat2 = TransactionCategory(0,"Clothes", R.color.circularProgressbarBackground)
        val cat3 = TransactionCategory(0,"Unspecified", android.R.color.holo_blue_dark)
        val cat4 = TransactionCategory(0,"Unspecified", android.R.color.holo_blue_dark)
        val cat5 = TransactionCategory(0,"Salary", android.R.color.holo_purple)

        val tr1 = Transaction(
            0,
            TransactionType.EXPENSE,
            "Food shopping",
            15.47f,
            cat1.categoryId,
            "InterSparhelt",
            PaymentMethod.CASH,
            TransactionPlanned.PLANNED,
            TransactionFrequency.MONTHLY_SUM,
            0
        )

        val tr2 = Transaction(
            0,
            TransactionType.EXPENSE,
            "Clothes shopping",
            23.77f,
            cat2.categoryId,
            "Whatever shop",
            PaymentMethod.CARD,
            TransactionPlanned.NOT_PLANNED,
            TransactionFrequency.FORTNIGHTLY_ONCE,
            0
        )

        val tr3 = Transaction(
            0,
            TransactionType.WITHDRAW,
            "Withdrawal",
            50.0f,
            cat3.categoryId,
            "Whatever shop",
            PaymentMethod.CARD,
            TransactionPlanned.NOT_PLANNED,
            TransactionFrequency.FORTNIGHTLY_ONCE,
            0
        )

        val tr4 = Transaction(
            0,
            TransactionType.DEPOSIT,
            "Deposit",
            70.0f,
            cat4.categoryId,
            "Whatever shop",
            PaymentMethod.CARD,
            TransactionPlanned.NOT_PLANNED,
            TransactionFrequency.FORTNIGHTLY_ONCE,
            0
        )

        val tr5 = Transaction(
            0,
            TransactionType.INCOME,
            "Salary",
            1245.55f,
            cat5.categoryId,
            "My boss",
            PaymentMethod.CARD,
            TransactionPlanned.PLANNED,
            TransactionFrequency.MONTHLY_ONCE,
            0
        )

        binding.tran1.setTransactionAndCategory(tr1, cat1)
        binding.tran2.setTransactionAndCategory(tr2, cat2)
        binding.tran3.setTransactionAndCategory(tr3, cat3)
        binding.tran4.setTransactionAndCategory(tr4, cat4)
        binding.tran5.setTransactionAndCategory(tr5, cat5)


        /** Return the inflated layout */
        return binding.root
    }
    
}