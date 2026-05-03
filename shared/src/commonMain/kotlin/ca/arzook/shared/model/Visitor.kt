package ca.arzook.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class AveRates(
    val buyingAskingRateAvg: Double,
    val dayOfWeek: String,
    val sellingAskingRateAvg: Double
)

@Serializable
data class Status(val title: String)

@Serializable
data class PaymentMethod(val title: String, val value: Int)

