package com.diamont.expense.tracker.util.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "second_party_data")
data class VenueData(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val venueId : Int = 0,

    @ColumnInfo(name = "name")
    val venueName : String = "",

    @ColumnInfo(name = "is_recipient")
    val isRecipient : Boolean = true
) {
    override fun toString(): String {
        return venueName
    }
}