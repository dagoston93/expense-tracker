package com.diamont.expense.tracker.util.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "venue_data")
data class VenueData(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "venue_id")
    val venueId : Int = 0,

    @ColumnInfo(name = "venue_name")
    val venueName : String = ""
) {
    override fun toString(): String {
        return venueName
    }
}