package com.diamont.expense.tracker.util.database

import androidx.room.*
import com.diamont.expense.tracker.util.enums.PaymentMethod
import com.diamont.expense.tracker.util.enums.TransactionFrequency
import com.diamont.expense.tracker.util.enums.TransactionPlanned
import com.diamont.expense.tracker.util.enums.TransactionType

/**
 * This data class holds information about a transaction
 */
@Entity(tableName = "transaction_data")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "transaction_id")
    val transactionId : Int,

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

    @ColumnInfo(name = "payment_method")
    @TypeConverters(PaymentMethodConverter::class)
    val method: PaymentMethod,

    @ColumnInfo(name = "planned")
    @TypeConverters(TransactionPlannedConverter::class)
    val planned: TransactionPlanned,

    @ColumnInfo(name = "frequency")
    @TypeConverters(TransactionFrequencyConverter::class)
    val frequency: TransactionFrequency,

    @ColumnInfo(name = "date")
    val date: Long,

    @ColumnInfo(name = "plan_id")
    val planId : Int
){
    /**
     * Call this method to get amount as string
     */
    fun getAmountString() : String{
        return "$$amount"
    }

    /**
     * Call this method to get the date as string
     */
    fun getDateString() : String = "2021. 02. 16."

}
