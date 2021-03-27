package com.diamont.expense.tracker.util.database

import androidx.room.TypeConverter
import com.diamont.expense.tracker.util.enums.PaymentMethod
import com.diamont.expense.tracker.util.enums.TransactionFrequency
import com.diamont.expense.tracker.util.enums.TransactionPlanned
import com.diamont.expense.tracker.util.enums.TransactionType

/**
 * In this file we have type converters
 * which enable us to save enum classes
 * into room database
 */
class TransactionFrequencyConverter{
    @TypeConverter
    fun toTransactionFrequency(value : Int) = TransactionFrequency.fromInt(value)

    @TypeConverter
    fun fromTransactionFrequency(value : TransactionFrequency) = value.id
}

class TransactionTypeConverter{
    @TypeConverter
    fun toTransactionType(value : Int) = TransactionType.fromInt(value)

    @TypeConverter
    fun fromTransactionType(value : TransactionType) = value.id
}

class TransactionPlannedConverter{
    @TypeConverter
    fun toTransactionPlanned(value : Int) = TransactionPlanned.fromInt(value)

    @TypeConverter
    fun fromTransactionPlanned(value : TransactionPlanned) = value.id
}

class PaymentMethodConverter{
    @TypeConverter
    fun toPaymentMethod(value : Int) = PaymentMethod.fromInt(value)

    @TypeConverter
    fun fromPaymentMethod(value : PaymentMethod) = value.id
}