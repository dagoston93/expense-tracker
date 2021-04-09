package com.diamont.expense.tracker.util.database

import androidx.room.*
import com.diamont.expense.tracker.util.enums.PaymentMethod
import com.diamont.expense.tracker.util.enums.TransactionFrequency
import com.diamont.expense.tracker.util.enums.TransactionType

@Entity(tableName = "plan_data")
data class Plan(

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
)
