package com.diamont.expense.tracker.util.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.diamont.expense.tracker.util.enums.TransactionFrequency
import com.diamont.expense.tracker.util.enums.TransactionType

@Entity(tableName = "plan_data")
data class Plan(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "plan_id")
    val planId : Int = 0,

    @ColumnInfo(name = "type")
    @TypeConverters(TransactionTypeConverter::class)
    val transactionType : TransactionType,

    @ColumnInfo(name = "description")
    val description : String,

    @ColumnInfo(name = "amount")
    val amount : Float,

    @ColumnInfo(name = "category")
    val categoryId: Int,

    @ColumnInfo(name = "second_party")
    val secondParty : String,

    @ColumnInfo(name = "frequency")
    @TypeConverters(TransactionFrequencyConverter::class)
    val frequency: TransactionFrequency,

    @ColumnInfo(name = "date")
    val date: Long,

    @ColumnInfo(name = "is_active")
    val isActive : Boolean = false
){
    override fun toString(): String {
        return description
    }
}