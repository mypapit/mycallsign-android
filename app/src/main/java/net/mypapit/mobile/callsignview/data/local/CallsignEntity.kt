package net.mypapit.mobile.callsignview.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "callsigns",
    indices = [
        Index(value = ["holderName"]),
        Index(value = ["assignmentNo"]),
        Index(value = ["expiryDateIso"]),
        Index(value = ["prefixGroup"])
    ]
)
data class CallsignEntity(
    @PrimaryKey val callsign: String,
    val recordNo: Int,
    val holderName: String,
    val assignmentNo: String,
    val expiryDateIso: String,
    val expiryDateDisplay: String,
    val prefixGroup: String
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val callsign: String,
    val addedAtEpochMs: Long
)
