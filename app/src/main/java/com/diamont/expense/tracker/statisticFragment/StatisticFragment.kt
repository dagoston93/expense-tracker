package com.diamont.expense.tracker.statisticFragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.transition.TransitionManager
import com.diamont.expense.tracker.MainActivityViewModel
import com.diamont.expense.tracker.MainActivityViewModelFactory
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.databinding.FragmentStatisticBinding
import com.diamont.expense.tracker.util.*
import com.diamont.expense.tracker.util.database.TransactionDatabase
import com.diamont.expense.tracker.util.enums.TransactionType
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.text.DecimalFormat


class StatisticFragment : DateRangeSelectorFragment() {
    /** Data binding and view model */
    private lateinit var binding : FragmentStatisticBinding
    override lateinit var baseClassViewModel: DateRangeSelectorFragmentViewModel
    private lateinit var viewModel: StatisticFragmentViewModel

    /** Get the Activity View Model */
    private val activityViewModel : MainActivityViewModel by activityViewModels {
        MainActivityViewModelFactory(
            requireNotNull(this.activity).application
        )
    }

    /** Array adapter for statistic types */
    private lateinit var statisticTypeAdapter : ArrayAdapter<String>
    private var previousSelectedStatisticTypeIndex: Int = 0

    /**
     * Declare variables
     */
    private lateinit var layout: LinearLayout
    private val fadeIn = MutableLiveData<Boolean>(false)

    /**
     * onCreateView()
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /** Inflate the layout for this fragment */
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_statistic, container, false)
        binding.lifecycleOwner = this

        /**
         * Show the default statistic type layout
         */
        showStatisticTypeLayout(0, inflater, false, false) // Do it before view model is created

        /**
         *  Create the view model using a view model factory
         */
        val application = requireNotNull(this.activity).application
        val databaseDao = TransactionDatabase.getInstance(application).transactionDatabaseDao
        val viewModelFactory = StatisticFragmentViewModelFactory(application, databaseDao, activityViewModel.sharedPreferences)

        baseClassViewModel = ViewModelProvider(this, viewModelFactory)
            .get(StatisticFragmentViewModel::class.java)

        viewModel = baseClassViewModel as StatisticFragmentViewModel

        /** Set up values for activity view model */
        activityViewModel.setTitle(getString(R.string.statistics))
        activityViewModel.setBottomNavBarVisibility(true)
        activityViewModel.setUpButtonVisibility(false)
        activityViewModel.setDrawerLayoutEnabled(true)
        activityViewModel.defaultTransactionType = TransactionType.EXPENSE

        /**
         * Get the decimal format of the currency
         */
        val currencyId = activityViewModel.sharedPreferences.getInt(KEY_PREF_CURRENCY_ID, 0)
        val decimalFormat = Currency.getDecimalFormat(currencyId) ?: DecimalFormat()

        /**
         * Add text changed listener for the period dropdown
         */
        binding.actvStatisticPeriod.addTextChangedListener {
            val idx = binding.actvStatisticPeriod.getStringListIndexFromText(viewModel.periodStringList.value ?: listOf<String>())

            onDateRangeSelected(idx, binding.actvStatisticPeriod)
        }

        /**
         * Add text changed listener for the statistic type dropdown
         */
        binding.actvStatisticType.addTextChangedListener {
            val idx = binding.actvStatisticType.getStringListIndexFromText(viewModel.statisticTypeStringList.value ?: listOf<String>())

            if(previousSelectedStatisticTypeIndex != idx) {
                previousSelectedStatisticTypeIndex = idx ?: 0
                showStatisticTypeLayout(idx ?: 0, inflater)
            }
        }

        /**
         * Observe period string list
         */
        viewModel.periodStringList.observe(viewLifecycleOwner, Observer {
            periodAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, it)
            binding.actvStatisticPeriod.setAdapter(periodAdapter)
            binding.actvStatisticPeriod.setText(periodAdapter.getItem(0).toString(), false)
        })

        /**
         * Observe statistic type string list
         */
        viewModel.statisticTypeStringList.observe(viewLifecycleOwner, Observer {
            statisticTypeAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, it)
            binding.actvStatisticType.setAdapter(statisticTypeAdapter)
            binding.actvStatisticType.setText(statisticTypeAdapter.getItem(0).toString(), false)
        })

        /**
         * Observe UI live data for the different statistic types
         *
         * Incomes and expenses
         * Total incomes
         */
        viewModel.totalIncomesPeriod.observe(viewLifecycleOwner, Observer {
            if(previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_INCOME_EXPENSE){
                layout.findViewById<TextView>(R.id.tvStatIncExpTotalIncomes).text = it
            }
        })

        /**
         * Total expenses
         */
        viewModel.totalExpensesPeriod.observe(viewLifecycleOwner, Observer {
            if(previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_INCOME_EXPENSE){
                layout.findViewById<TextView>(R.id.tvStatIncExpTotalExpenses).text = it
            }
        })

        /**
         * Planned incomes
         */
        viewModel.plannedIncomesPeriod.observe(viewLifecycleOwner, Observer {
            if(previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_INCOME_EXPENSE){
                layout.findViewById<TextView>(R.id.tvStatIncExpPlannedIncomes).text = it
            }
        })

        /**
         * Planned expenses
         */
        viewModel.plannedExpensesPeriod.observe(viewLifecycleOwner, Observer {
            if(previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_INCOME_EXPENSE){
                layout.findViewById<TextView>(R.id.tvStatIncExpPlannedExpenses).text = it
            }
        })

        /**
         * Not planned incomes
         */
        viewModel.notPlannedIncomesPeriod.observe(viewLifecycleOwner, Observer {
            if(previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_INCOME_EXPENSE){
                layout.findViewById<TextView>(R.id.tvStatIncExpNotPlannedIncomes).text = it
            }
        })

        /**
         * Not planned expenses
         */
        viewModel.notPlannedExpensesPeriod.observe(viewLifecycleOwner, Observer {
            if(previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_INCOME_EXPENSE){
                layout.findViewById<TextView>(R.id.tvStatIncExpNotPlannedExpenses).text = it
            }
        })

        /**
         * Savings label
         */
        viewModel.savingsOrOverspendLabel.observe(viewLifecycleOwner, Observer {
            if(previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_INCOME_EXPENSE){
                layout.findViewById<TextView>(R.id.tvStatIncExpSavingsLabel).text = it
            }
        })

        /**
         * Savings
         */
        viewModel.savingsOrOverspendPeriod.observe(viewLifecycleOwner, Observer {
            if(previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_INCOME_EXPENSE){
                layout.findViewById<TextView>(R.id.tvStatIncExpSavings).text = it
            }
        })

        /**
         * By categories
         *
         * Pie chart data
         */
        viewModel.categoryPieChartData.observe(viewLifecycleOwner, Observer {
            if(previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_EXPENSE_CATEGORIES
                || previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_INCOME_CATEGORIES){

                val chart = layout.findViewById<PieChart>(R.id.pcStatCatChart) as PieChart

                chart.description.isEnabled = false
                chart.legend.isEnabled = false
                chart.setUsePercentValues(true)
                chart.setDrawEntryLabels(false)
                chart.isHighlightPerTapEnabled = true

                val animDuration = resources.getInteger(android.R.integer.config_mediumAnimTime)

                val listener = object : OnChartValueSelectedListener{
                    override fun onValueSelected(e: Entry?, h: Highlight?) {

                        val tvLabel = layout.findViewById<TextView>(R.id.tvStatCatCategoryAmount)
                        tvLabel.text = viewModel.getAmountStringFromList((e?.data as? Int) ?: 0)

                        val tvAmount = layout.findViewById<TextView>(R.id.tvStatCatCategoryLabel)
                        tvAmount.text = (e as? PieEntry)?.label ?: ""


                        TransitionManager.beginDelayedTransition(layout.findViewById<ConstraintLayout>(R.id.clStatCatChartContainer))
                        tvLabel.visibility = View.VISIBLE
                        tvAmount.visibility = View.VISIBLE
                    }

                    override fun onNothingSelected() {
                        TransitionManager.beginDelayedTransition(layout.findViewById<ConstraintLayout>(R.id.clStatCatChartContainer))
                        layout.findViewById<TextView>(R.id.tvStatCatCategoryAmount).visibility = View.GONE
                        layout.findViewById<TextView>(R.id.tvStatCatCategoryLabel).visibility = View.GONE
                    }
                }

                chart.data = it
                chart.setOnChartValueSelectedListener(listener)
                chart.invalidate()
                chart.animateXY(animDuration, animDuration)

                val mainContainer = layout.findViewById<LinearLayout>(R.id.llStatCatMainContainer)
                mainContainer.removeAllViews()

                /** Add category cards */
                for(i in viewModel.actualPieEntries.indices){

                    val categoryLayout = inflater.inflate(
                        R.layout.item_statistic_category,
                        mainContainer,
                        false
                    ) as ConstraintLayout

                    ImageViewCompat.setImageTintList(
                        categoryLayout.findViewById<ImageView>(R.id.ivStatCategoryItemColorStrip),
                        ColorStateList.valueOf(viewModel.dataColorList[i])
                    )

                    categoryLayout.findViewById<TextView>(R.id.tvStatCategoryItemName).text =
                        viewModel.actualPieEntries[i].label

                    categoryLayout.findViewById<TextView>(R.id.tvStatCategoryItemAmount).text =
                        viewModel.getAmountStringFromList(i)

                    categoryLayout.findViewById<TextView>(R.id.tvStatCategoryItemPercentage).text =
                        "%.0f%%".format(viewModel.actualPieEntries[i].value)

                    mainContainer.addView(categoryLayout)

                }
            }
        })

        /**
         * Total expense/income label
         */
        viewModel.catPageTotalIncomeOrExpenseLabel.observe(viewLifecycleOwner, Observer {
            if(previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_EXPENSE_CATEGORIES
                || previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_INCOME_CATEGORIES){

                layout.findViewById<TextView>(R.id.tvStatCatTotalLabel).text = it
            }
        })

        /**
         * Total expenses/incomes
         */
        viewModel.catPageTotalIncomeOrExpense.observe(viewLifecycleOwner, Observer {
            if(previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_EXPENSE_CATEGORIES
                || previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_INCOME_CATEGORIES){

                layout.findViewById<TextView>(R.id.tvStatCatTotalAmount).text = it
            }
        })

        /** Return the inflated layout */
        return binding.root
    }

    /**
     * Call this method to display the required layout
     */
    private fun showStatisticTypeLayout(
        idx: Int,
        inflater: LayoutInflater,
        animate: Boolean = true,
        notifyViewModel: Boolean = true
    ){
        val layoutId = when(idx){
            StatisticFragmentViewModel.IDX_EXPENSE_CATEGORIES, StatisticFragmentViewModel.IDX_INCOME_CATEGORIES -> R.layout.layout_statistic_categories
            StatisticFragmentViewModel.IDX_EXPENSE_PLANS, StatisticFragmentViewModel.IDX_INCOME_PLANS -> R.layout.layout_statistic_plan
            else -> R.layout.layout_statistic_income_expense
        }

        /** This local function adds the new layout */
        fun addNewLayout(){
            /** Add new layout */
            binding.llStatisticContent.removeAllViews()
            layout = inflater.inflate(layoutId, binding.llStatisticContent, true) as LinearLayout

            if(notifyViewModel){
                viewModel.onSelectedStatisticTypeChanged(idx)
            }
        }

        /** If animation is on do animation */
        if(animate) {
            /**
             * Fade out, when done add the new layout, and than fade in
             */
            val animDuration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
            binding.llStatisticContent.animate()
                .setDuration(animDuration)
                .alpha(0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        addNewLayout()
                        /** Fade in */
                        binding.llStatisticContent.animate()
                            .setDuration(animDuration)
                            .alpha(1f)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator?) {}
                            })
                    }
                })
        }else{
            /** No animation, just add the layout */
            addNewLayout()
        }
    }

    /**
     * We fill the dropdown menus in onResume() so that
     * if device configuration is changed we don't loose the
     * items from the menu
     */
    override fun onResume() {
        binding.actvStatisticPeriod.setAdapter(periodAdapter)
        binding.actvStatisticType.setAdapter(statisticTypeAdapter)

        super.onResume()
    }
}