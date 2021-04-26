package com.diamont.expense.tracker.statisticFragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
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
import com.diamont.expense.tracker.util.view.CircularProgressBar
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.text.DecimalFormat
import kotlin.math.roundToInt


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

                val listener = object : OnChartValueSelectedListener{
                    override fun onValueSelected(e: Entry?, h: Highlight?) {

                        val tvLabel = layout.findViewById<TextView>(R.id.tvStatCatCategoryAmount)
                        tvLabel.text = viewModel.getAmountStringFromList((e?.data as? Int) ?: 0)

                        val tvAmount = layout.findViewById<TextView>(R.id.tvStatCatCategoryLabel)
                        tvAmount.text = (e as? PieEntry)?.label ?: ""

                        tvLabel.visibility = View.VISIBLE
                        tvAmount.visibility = View.VISIBLE
                        TransitionManager.beginDelayedTransition(layout.findViewById<ConstraintLayout>(R.id.clStatCatChartContainer))
                    }

                    override fun onNothingSelected() {
                        layout.findViewById<TextView>(R.id.tvStatCatCategoryAmount).visibility = View.GONE
                        layout.findViewById<TextView>(R.id.tvStatCatCategoryLabel).visibility = View.GONE
                        TransitionManager.beginDelayedTransition(layout.findViewById<ConstraintLayout>(R.id.clStatCatChartContainer))
                    }
                }

                setUpPieChart(chart, it, listener)

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

        /**
         * By plans
         *
         * Actual pie data
         */
        viewModel.planActualPieChartData.observe(viewLifecycleOwner, Observer {
            if(previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_INCOME_PLANS
                || previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_EXPENSE_PLANS){

                val chart = layout.findViewById<PieChart>(R.id.pcStatPlanActualChart) as PieChart

                setUpPieChart(chart, it, createPlanPagePieChartListener(R.id.pcStatPlannedChart))
                chart.holeRadius = 60f

                layout.findViewById<TextView>(R.id.tvStatPlanNoActualDataLabel).visibility =
                    if(it?.dataSet?.entryCount == 0){
                        View.VISIBLE
                    }else{
                        View.GONE
                    }

                /**
                 * Add plan cards
                 */
                val mainContainer = layout.findViewById<LinearLayout>(R.id.llStatPlanMainContainer)
                mainContainer.removeAllViews()

                for(i  in viewModel.planStatisticDataList.indices){
                    if(viewModel.planStatisticDataList[i].actualAmount != 0f || viewModel.planStatisticDataList[i].plannedAmount != 0f){
                        val planLayout = inflater.inflate(
                            R.layout.item_statistic_plan,
                            mainContainer,
                            false
                        ) as ConstraintLayout

                        ImageViewCompat.setImageTintList(
                            planLayout.findViewById<ImageView>(R.id.ivStatPlanItemColorStrip),
                            ColorStateList.valueOf(viewModel.planStatisticDataList[i].color)
                        )

                        planLayout.findViewById<TextView>(R.id.tvStatPlanItemName).text = viewModel.planStatisticDataList[i].desciption

                        planLayout.findViewById<TextView>(R.id.tvStatPlanItemActualAmount).text =
                            viewModel.formatAmount(viewModel.planStatisticDataList[i].actualAmount)

                        planLayout.findViewById<TextView>(R.id.tvStatPlanItemActualPercentage).text =
                            "%.0f%%".format(viewModel.planStatisticDataList[i].actualPercentage)

                        val tvPlannedSum = planLayout.findViewById<TextView>(R.id.tvStatPlanItemPlannedAmount)
                        val tvPlannedSumLabel = planLayout.findViewById<TextView>(R.id.tvStatPlanItemPlannedAmountLabel)
                        val tvPlannedPercentage = planLayout.findViewById<TextView>(R.id.tvStatPlanItemPercentagePlanned)
                        val tvPlannedPercentageLabel = planLayout.findViewById<TextView>(R.id.tvStatPlanItemPercentagePlannedLabel)
                        val circularProgressbar = planLayout.findViewById<CircularProgressBar>(R.id.cpbStatPlanItemProgress)

                        if(viewModel.planStatisticDataList[i].id == -1){
                            tvPlannedSum.visibility = View.GONE
                            tvPlannedSumLabel.visibility = View.GONE

                            tvPlannedPercentage.visibility = View.GONE
                            tvPlannedPercentageLabel.visibility = View.GONE

                            circularProgressbar.visibility = View.GONE
                        }else{
                            if(viewModel.planStatisticDataList[i].plannedAmount == 0f){
                                circularProgressbar.visibility = View.GONE
                            }else{
                                circularProgressbar.setCircularProgressBarProgress(
                                    ((viewModel.planStatisticDataList[i].actualAmount/viewModel.planStatisticDataList[i].plannedAmount) * 100).roundToInt()
                                )
                            }
                            tvPlannedSum.text = viewModel.formatAmount(viewModel.planStatisticDataList[i].plannedAmount)
                            tvPlannedPercentage.text = "%.0f%%".format(viewModel.planStatisticDataList[i].plannedPercentage)

                            circularProgressbar.setCircularProgressBarForegroundColor(
                                if(previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_EXPENSE_PLANS){
                                    R.color.colorGoalNotAchieved
                                }else{
                                    R.color.colorGoalAchieved
                                }
                            )
                        }

                        mainContainer.addView(planLayout)

                    }
                }

            }
        })

        /**
         * Plan pie entries
         */
        viewModel.planPlannedPieChartData.observe(viewLifecycleOwner, Observer {
            if(previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_INCOME_PLANS
                || previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_EXPENSE_PLANS){

                val chart = layout.findViewById<PieChart>(R.id.pcStatPlannedChart) as PieChart
                setUpPieChart(chart, it, createPlanPagePieChartListener(R.id.pcStatPlanActualChart))

                layout.findViewById<TextView>(R.id.tvStatPlanNoPlannedDataLabel).visibility =
                    if(it?.dataSet?.entryCount == 0){
                        View.VISIBLE
                    }else{
                        View.GONE
                    }

            }
        })

        /**
         * Planned label
         */
        viewModel.planPageTotalPlannedIncomeOrExpenseLabel.observe(viewLifecycleOwner, Observer {
            if(previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_INCOME_PLANS
                || previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_EXPENSE_PLANS){

                layout.findViewById<TextView>(R.id.tvStatPlanTotalPlannedLabel).text = it
            }
        })

        /**
         * Actual label
         */
        viewModel.planPageTotalActualIncomeOrExpenseLabel.observe(viewLifecycleOwner, Observer {
            if(previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_INCOME_PLANS
                || previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_EXPENSE_PLANS){

                layout.findViewById<TextView>(R.id.tvStatPlanTotalActualLabel).text = it
            }
        })

        /**
         * Planned amount
         */
        viewModel.planPageTotalPlanned.observe(viewLifecycleOwner, Observer {
            if(previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_INCOME_PLANS
                || previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_EXPENSE_PLANS){

                layout.findViewById<TextView>(R.id.tvStatPlanTotalPlannedAmount).text = it
            }
        })

        /**
         * Actual amount
         */
        viewModel.planPageTotalActual.observe(viewLifecycleOwner, Observer {
            if(previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_INCOME_PLANS
                || previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_EXPENSE_PLANS){

                layout.findViewById<TextView>(R.id.tvStatPlanTotalActualAmount).text = it
            }
        })

        /**
         * Outer chart label
         */
        viewModel.planPageOuterPieChartLabelLabel.observe(viewLifecycleOwner, Observer {
            if(previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_INCOME_PLANS
                || previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_EXPENSE_PLANS){
                layout.findViewById<TextView>(R.id.tvStatPlanOuterChartLabel).text = it
            }
        })

        /**
         * Inner chart label
         */
        viewModel.planPageInnerPieChartLabelLabel.observe(viewLifecycleOwner, Observer {
            if(previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_INCOME_PLANS
                || previousSelectedStatisticTypeIndex == StatisticFragmentViewModel.IDX_EXPENSE_PLANS){
                layout.findViewById<TextView>(R.id.tvStatPlanInnerChartLabel).text = it
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
     * Call this method to setup a PieChart
     */
    private fun setUpPieChart(chart: PieChart, data: PieData?, listener: OnChartValueSelectedListener? = null){
        val animDuration = resources.getInteger(android.R.integer.config_mediumAnimTime)

        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setUsePercentValues(true)
        chart.setDrawEntryLabels(false)
        chart.isHighlightPerTapEnabled = true

        chart.setHoleColor(
            ContextCompat.getColor(requireContext(), android.R.color.transparent)
        )

        chart.data = data
        chart.setOnChartValueSelectedListener(listener)
        chart.invalidate()
        chart.highlightValue(null)
        chart.animateXY(animDuration, animDuration)
    }

    /**
     * Call this method to create the listener for the plan page pie charts
     */
    private fun createPlanPagePieChartListener(otherChartId: Int): OnChartValueSelectedListener{
        return object : OnChartValueSelectedListener{
            override fun onValueSelected(e: Entry?, h: Highlight?) {

                val tvPlanNameLabel = layout.findViewById<TextView>(R.id.tvStatPlanNameLabel)
                val tvPlannedLabel = layout.findViewById<TextView>(R.id.tvStatPlanPlannedLabel)
                val tvActualLabel = layout.findViewById<TextView>(R.id.tvStatPlanActualLabel)
                val tvPlannedAmount = layout.findViewById<TextView>(R.id.tvStatPlanSelectedPlanPlannedAmount)
                val tvActualAmount = layout.findViewById<TextView>(R.id.tvStatPlanSelectedPlanActualAmount)
                val pcOtherPieChart = layout.findViewById<PieChart>(otherChartId)

                pcOtherPieChart.highlightValue(null)

                val statData = viewModel.planStatisticDataList.find { stat->
                    stat.id == e?.data as Int
                }

                //Log.d("GUS", "data: ${viewModel.planStatisticDataList}")
                //Log.d("GUS", "sel:$statData")

                if(statData != null){
                    tvActualLabel.visibility = View.VISIBLE
                    tvPlanNameLabel.visibility = View.VISIBLE
                    tvActualAmount.visibility = View.VISIBLE

                    tvPlanNameLabel.text = statData.desciption
                    tvActualAmount.text = viewModel.formatAmount(statData.actualAmount)


                    if(statData.id == -1){
                        tvPlannedAmount.visibility = View.GONE
                        tvPlannedLabel.visibility = View.GONE
                    }else{
                        tvPlannedAmount.visibility = View.VISIBLE
                        tvPlannedLabel.visibility = View.VISIBLE

                        tvPlannedAmount.text = viewModel.formatAmount(statData.plannedAmount)
                    }

                    TransitionManager.beginDelayedTransition(layout.findViewById<ConstraintLayout>(R.id.clStatPlanChartContainer))
                }
            }

            override fun onNothingSelected() {
                layout.findViewById<TextView>(R.id.tvStatPlanNameLabel).visibility = View.GONE
                layout.findViewById<TextView>(R.id.tvStatPlanPlannedLabel).visibility = View.GONE
                layout.findViewById<TextView>(R.id.tvStatPlanActualLabel).visibility = View.GONE
                layout.findViewById<TextView>(R.id.tvStatPlanSelectedPlanPlannedAmount).visibility = View.GONE
                layout.findViewById<TextView>(R.id.tvStatPlanSelectedPlanActualAmount).visibility = View.GONE

                TransitionManager.beginDelayedTransition(layout.findViewById<ConstraintLayout>(R.id.clStatPlanChartContainer))
            }
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