package com.diamont.expense.tracker.util.database

import android.content.Context
import androidx.room.*
import com.diamont.expense.tracker.util.enums.PaymentMethod
import com.diamont.expense.tracker.util.enums.TransactionFrequency
import com.diamont.expense.tracker.util.enums.TransactionPlanned
import com.diamont.expense.tracker.util.enums.TransactionType
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*

/**
 * This data class holds information about a transaction
 */
@Entity(tableName = "transaction_data")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "transaction_id")
    var transactionId : Int = 0,

    @ColumnInfo(name = "type")
    @TypeConverters(TransactionTypeConverter::class)
    var transactionType : TransactionType = TransactionType.EXPENSE,

    @ColumnInfo(name = "description")
    var description : String = "",

    @ColumnInfo(name = "amount")
    var amount : Float = 0f,

    @ColumnInfo(name = "category")
    var categoryId: Int = 0,

    @ColumnInfo(name = "second_party")
    var secondParty : String = "",

    @ColumnInfo(name = "payment_method")
    @TypeConverters(PaymentMethodConverter::class)
    var method: PaymentMethod = PaymentMethod.CASH,

    @ColumnInfo(name = "planned")
    @TypeConverters(TransactionPlannedConverter::class)
    var planned: TransactionPlanned = TransactionPlanned.NOT_PLANNED,

    @ColumnInfo(name = "frequency")
    @TypeConverters(TransactionFrequencyConverter::class)
    var frequency: TransactionFrequency = TransactionFrequency.ONE_TIME,

    @ColumnInfo(name = "date")
    var date: Long = 0,

    @ColumnInfo(name = "plan_id")
    var planId : Int = -1
): TransactionDetailViewAdaptable() {
    /**
     * Call this method to get amount as string
     */
    fun getAmountString(decimalFormat: DecimalFormat) : String{
        return decimalFormat.format(amount)
    }

    /**
     * Call this method to get the date as string
     */
    fun getDateString(context: Context) : String {
        val dateFormat = android.text.format.DateFormat.getDateFormat(context)
        return dateFormat.format(date)
    }

}
