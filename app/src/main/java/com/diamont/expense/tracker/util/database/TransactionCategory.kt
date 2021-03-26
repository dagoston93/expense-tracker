package com.diamont.expense.tracker.util.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.diamont.expense.tracker.R

/**
 * This data class holds information about a transaction category
 */
@Entity(tableName = "transaction_category")
data class TransactionCategory(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "category_id")
    val categoryId : Int = 0,

    @ColumnInfo(name = "category_name")
    val categoryName: String = "",

    @ColumnInfo(name = "color_res_id")
    val categoryColorResId : Int = R.color.black
)