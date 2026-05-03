package ca.arzook.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateSellingAmountRequest(
    val amount: String,
    val nowruzGiftEnabled: Boolean = false,
    val zakatAlFitrEnabled: Boolean = false
)

@Serializable
data class UpdateSellingRateRequest(
    val rate: String,
    val nowruzGiftEnabled: Boolean = false,
    val zakatAlFitrEnabled: Boolean = false
)

@Serializable
data class UpdateSellingPayeeRequest(
    val id: String,
    val sheba: String,
    val payeeName: String
)

@Serializable
data class PromoCodeResponse(
    val id: String? = null,
    val name: String? = null,
    val code: String? = null,
    val channel: String? = null,
    val description: String? = null,
    val discountPercentage: Int? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val valid: Boolean? = null,
    val message: String? = null
)

//@Serializable
//data class CreateBuyingSellingRequest(
//    val amount: Double,
//    val askingRate: Double,
//    val serviceRate: Double,
//    val exchangeRate: Double,
//    val currency: String,
//    val purposeOfTransaction: String,
//    val sourceOfFund: String,
//    val urgent: Boolean? = null,
//    val smartMatchingEnabled: Boolean? = null,
//    val advertised: Boolean? = true,
//    val nowruzGiftEnabled: Boolean? = false,
//    val zakatAlFitrEnabled: Boolean? = false
//)

@Serializable
data class CurrentRate(
    val currentMaxAskingRate: Double,
    val currentMaxBuyingExchangeRate: Double,
    val currentMidMarketRate: Double,
    val currentMinAskingRate: Double,
    val currentMinSellingExchangeRate: Double,
    val last24HourRateAvg: Double,
    val userBuyingRateOffset: Double,
    val userSellingRateOffset: Double
)

@Serializable
data class WatchItem(
    val amount: Double = 0.0,
    val createdTime: String = "",
    val listingRate: Double = 0.0,
    val offeringId: String,
    val watcherId: String
)

@Serializable
data class LockedTrade(
    val id: String = "",
    val offeringId: String? = null,
    val buyerId: String = "",
    val arzookBankInfoName: String? = null,
    val arzookBankInfoSheba: String? = null,
    val arzookDepositEmail: String? = null,
    val autoLockExpiresIn: Int = 0,
    val autoLockExpiryTime: String = "",
    val autoLockTime: String = "",
    val holdTransactionFee: Int = 0
)

@Serializable
data class WalletStatus(
    val userId: String = "",
    val balance: Int = 0,
    val holdCredit: Int = 0,
    val lastUpdated: String = ""
)

@Serializable
data class DigitalWalletItem(
    val id: String,
    val userId: String,
    val amount: Double,
    val balance: Double,
    val code: String? = null,
    val date: String,
    val description: String? = null,
    val trackingNumber: Long? = null,
    val type: String
)

@Serializable
data class TradeItem(
    val id: String? = null,
    val code: String? = null,
    val eTransferPassword: String? = null,
    val arzookDepositEmail: String? = null,
    val payeeName: String? = null,
    val payeeLastName: String? = null,
    val payeeFirstName: String? = null,
    val payeeEntityName: String? = null,
    val sheba: String? = null,
    val payeeCity: String? = null,
    val payeeIrPostalCode: String? = null,
    val payeeIrNationalId: String? = null,
    val amount: Double? = null,
    val totalActiveETransferAmount: Double? = null,
    val currency: String? = null,
    val askingRate: Double? = null,
    val exchangeRate: Double? = null,
    val purposeOfTransaction: String? = null,
    val sourceOfFund: String? = null,
    val discount: String? = null,
    val sellerId: String? = null,
    val sellerName: String? = null,
    val sellerEmail: String? = null,
    val sellerPhone: String? = null,
    val createdTime: String? = null,
    val deposited: Boolean? = null,
    val depositedDate: String? = null,
    val advertised: Boolean? = null,
    val advertisedDate: String? = null,
    val exchangeDeposited: Boolean? = null,
    val exchangeDepositedDate: String? = null,
    val transferred: Boolean? = null,
    val lockExpiresIn: Int? = null,
    val expiryDate: String? = null,
    val userLockedDate: String? = null,
    val isLocked: Boolean? = null,
    val buyingId: String? = null,
    val buyingCode: String? = null,
    val eTransferForwarded: Boolean? = null,
    val eTransferForwardedDate: String? = null,
    val buyingDraftExchangeDeposited: Boolean? = null,
    val buyingDraftExchangeDepositedDate: String? = null,
    val offered: Boolean? = null,
    val deactivated: Boolean? = null,
    val photoIdType: String? = null,
    val photoIdVerifiedDate: String? = null,
    val photoIdExpiryDate: String? = null,
    val paymentMethod: PaymentMethod? = null,
    val smartlyMatched: Boolean? = null,
    val nowruzGiftEnabled: Boolean? = false,
    val zakatAlFitrEnabled: Boolean? = false,
    val urgent: Boolean? = null,
    val promotion: String? = null,
    val totalCopied: Boolean? = null,
    val shebaCopied: Boolean? = null,
    val locked: Boolean? = null,
    val sellingId: String? = null,
    val sellingCode: String? = null,
    val payeeEmail: String? = null,
    val buyerId: String? = null,
    val buyerName: String? = null,
    val buyerEmail: String? = null,
    val buyerPhone: String? = null,
    val holdTransactionFee: Int? = null,
    val timeZoneId: String? = null,
    val arzookBankInfoName: String? = null,
    val arzookBankInfoSheba: String? = null,
    val customerDepositId: Int? = null,
    val sellingPaymentMethod: String? = null,
    val smartMatchingEnabled: Boolean? = null,
    val selling: Boolean = false,
    val status: Status? = null
)

@Serializable
data class Payee(
    val id: String? = null,
    val userId: String? = null,
    val name: String,
    val sheba: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val city: String? = null,
    val irNationalId: String? = null,
    val irPostalCode: String? = null
)
