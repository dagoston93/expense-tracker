package com.diamont.expense.tracker.util.database

import android.content.Context
import androidx.room.*
import com.diamont.expense.tracker.util.enums.PaymentMethod
import com.diamont.expense.tracker.util.enums.TransactionFrequency
import com.diamont.expense.tracker.util.enums.TransactionType
import java.text.DecimalFormat

@Entity(tableName = "plan_data")
data class Plan (

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0,

    @ColumnInfo(name = "amount")
    var amount: Float = 0f,

    @ColumnInfo(name = "description")
    var description: String = "",

    @ColumnInfo(name = "category_id")
    var categoryId: Int = 0,

    @ColumnInfo(name = "frequency")
    @TypeConverters(TransactionFrequencyConverter::class)
    var frequency: TransactionFrequency = TransactionFrequency.ONE_TIME,

    @ColumnInfo(name = "transaction_type")
    @TypeConverters(TransactionTypeConverter::class)
    var transactionType: TransactionType = TransactionType.PLAN_EXPENSE,

    @ColumnInfo(name = "source_or_recipient")
    var sourceOrRecipient: String = "",

    @ColumnInfo(name = "payment_method")
    @TypeConverters(PaymentMethodConverter::class)
    var method: PaymentMethod = PaymentMethod.CARD,

    @ColumnInfo(name = "first_expected_date")
    var firstExpectedDate: Long = 0,

    @Ignore
    var nextExpectedDate: Long = 0,

    @ColumnInfo(name = "last_completed_date")
    var lastCompletedDate: Long = 0,

    @ColumnInfo(name = "cancellation_date")
    var cancellationDate: Long = 0,

    @ColumnInfo(name = "is_status_active")
    var isStatusActive: Boolean = true
) : TransactionDetailViewAdaptable(), Comparable<Plan>{
    /**
     * Call this method to get amount as string
     */
    fun getAmountString(decimalFormat: DecimalFormat) : String{
        return decimalFormat.format(amount)
    }

    /**
     * Call this method to get the date as string
     */
    fun getDateString(date:Long, context: Context) : String {
        val dateFormat = android.text.format.DateFormat.getDateFormat(context)
        return dateFormat.format(date)
    }

    /**
     * This method is called when ordering the list
     *
     * Return positive if this object has priority
     * Return negative if other object has priority
     * Return zero if equal priority
     */
    override fun compareTo(other: Plan): Int {
        var priority: Int

        /** Check whether this plan is active or one time plan */
        if(this.isStatusActive || this.frequency == TransactionFrequency.ONE_TIME){
            if(other.isStatusActive || other.frequency == TransactionFrequency.ONE_TIME){
                /** Both are active or one time plans -> order by next expected date */
                if(this.nextExpectedDate > other.nextExpectedDate){
                    priority = 1
                }else if(this.nextExpectedDate == other.nextExpectedDate){
                    priority = 0
                }else{
                    priority = -1
                }
            }else{
                /** This is active or one time, the other is not -> This one has priority */
                priority = 1
            }
        }else{
            if(other.isStatusActive || other.frequency == TransactionFrequency.ONE_TIME){
                /** This one is not active or one time -> The other one has priority */
                priority = -1
            }else{
                /** Neither is active or one time -> Sort by cancellation date */
                if(this.cancellationDate > other.cancellationDate){
                    priority = 1
                }else if(this.cancellationDate == other.cancellationDate){
                    priority = 0
                }else{
                    priority = -1
                }
            }
        }

        return priority
    }
}
