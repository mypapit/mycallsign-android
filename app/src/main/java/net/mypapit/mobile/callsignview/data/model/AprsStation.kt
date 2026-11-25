package net.mypapit.mobile.callsignview.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AprsFeatureCollection(
    val type: String = "FeatureCollection",
    val features: List<AprsFeature> = emptyList()
)

@Serializable
data class AprsFeature(
    val type: String = "Feature",
    val geometry: AprsGeometry? = null,
    val properties: AprsStationProperties? = null
)

@Serializable
data class AprsGeometry(
    val type: String = "Point",
    val coordinates: List<Double> = emptyList()
)

@Serializable
data class AprsStationProperties(
    val callsign: String = "",
    @SerialName("object_name") val objectName: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @SerialName("received_at") val receivedAt: String = "",
    val comment: String = "",
    val symbol: String = "",
    @SerialName("symbol_table") val symbolTable: String = ""
)

data class AprsStation(
    val callsign: String,
    val baseCallsign: String,
    val objectName: String?,
    val latitude: Double,
    val longitude: Double,
    val receivedAt: String,
    val comment: String,
    val symbol: String,
    val symbolTable: String
)
