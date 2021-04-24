package com.diamont.expense.tracker.util

data class PlanStatisticData(
    var id: Int = 0,
    var desciption: String = "",
    var color: Int = 0,
    var plannedAmount: Float = 0f,
    var actualAmount: Float = 0f,
    var plannedPercentage: Float = 0f,
    var actualPercentage: Float = 0f

)

