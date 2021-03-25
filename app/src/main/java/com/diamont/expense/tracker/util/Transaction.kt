package com.diamont.expense.tracker.util

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.diamont.expense.tracker.util.database.PaymentMethodConverter
import com.diamont.expense.tracker.util.database.TransactionFrequencyConverter
import com.diamont.expense.tracker.util.database.TransactionPlannedConverter
import com.diamont.expense.tracker.util.database.TransactionTypeConverter

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
    val date: Long
){

    /**
     * Call this method to get amount as string
     */
    fun getAmount() : String{
        return "$$amount"
    }

    /**
     * Call this method to get the date as string
     */
    fun getDate() : String = "2021. 02. 16."

}
