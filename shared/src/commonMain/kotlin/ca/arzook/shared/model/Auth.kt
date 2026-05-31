package ca.arzook.shared.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val recaptchaToken: String = ""
)

@Serializable
data class LoginResponse(
    val accessToken: String = "",
    val tokenType: String = ""
) {
    fun resolvedToken(): String {
        if (accessToken.isEmpty()) return ""
        return if (tokenType.isNotEmpty() && !accessToken.startsWith(tokenType)) "$tokenType $accessToken" else accessToken
    }
}

@Serializable
data class User(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val inviterEmail: String? = "",
    val recaptchaToken: String = ""
)

private fun JsonElement?.asStringOrNull(): String? =
    try { this?.jsonPrimitive?.content?.takeIf { it.isNotEmpty() } } catch (_: Exception) { null }

@Serializable
data class PhotoIdType(
    val value: Int? = null,
    val title: String? = null
)

@Serializable
data class AuthenticatedData(
    val id: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val legalFirstName: String? = null,
    val legalLastName: String? = null,
    val address: JsonElement? = null,
    val birthday: String? = null,
    val city: JsonElement? = null,
    val createdAt: String? = null,
    val customerDepositId: Int? = null,
    val occupation: JsonElement? = null,
    val phoneNumber: String? = null,
    val photoIdAttached: Boolean? = null,
    val photoIdAttachedDate: String? = null,
    val photoIdExpiryDate: String? = null,
    val photoIdFileName: String? = null,
    val photoIdFileType: String? = null,
    val photoIdIssueDate: String? = null,
    val photoIdNo: String? = null,
    val photoIdRejected: String? = null,
    val photoIdRejectedDate: String? = null,
    val photoIdRejectedNote: String? = null,
    val photoIdType: PhotoIdType? = null,
    val photoIdVerified: Boolean? = null,
    val photoIdVerifiedDate: String? = null,
    val pictureUrl: String? = null,
    val postalCode: JsonElement? = null,
    val provider: String? = null,
    val updatedAt: String? = null,
    val utilityBillFileName: String? = null,
    val utilityBillFileType: String? = null,
//    val admin: Boolean? = null
) {
    val cityStr: String? get() = city.asStringOrNull()
    val addressStr: String? get() = address.asStringOrNull()
    val occupationStr: String? get() = occupation.asStringOrNull()
    val postalCodeStr: String? get() = postalCode.asStringOrNull()
}

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Authenticated(val data: AuthenticatedData) : AuthState()
    data class Error(val message: String) : AuthState()
}

@Serializable
data class RateAlert(
    val sellingEnabled: Boolean? = null,
    val minSellingRate: Double? = null,
    val minSellingAmount: Double? = null,
    val maxSellingRate: Double? = null,
    val maxSellingAmount: Double? = null,
    val buyingEnabled: Boolean? = null,
    val minBuyingRate: Double? = null,
    val minBuyingAmount: Double? = null,
    val maxBuyingRate: Double? = null,
    val maxBuyingAmount: Double? = null
)

@Serializable
data class UpdateProfileRequest(
    val phoneNumber: String? = null,
    val birthday: String? = null,
    val occupation: String? = null,
    val address: String? = null,
    val city: String? = null,
    val postalCode: String? = null,
)