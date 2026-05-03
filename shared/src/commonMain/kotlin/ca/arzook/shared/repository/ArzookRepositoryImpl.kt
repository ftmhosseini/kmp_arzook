package ca.arzook.shared.repository

import ca.arzook.shared.Result
import ca.arzook.shared.model.*
import ca.arzook.shared.network.createHttpClient
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class ArzookRepositoryImpl(
    private val baseUrl: String,
    private val client: HttpClient = createHttpClient()
) : ArzookRepository {

    private fun String.bearer() = "Bearer $this"

    private suspend inline fun <reified T> safeCall(block: () -> T): Result<T> =
        try { Result.Success(block()) } catch (e: Exception) { Result.Error(e.message ?: "Unknown error") }

    override suspend fun getDailyStats(): Result<List<AveRates>> = safeCall {
        client.get("$baseUrl/api/main/daily-stats").body()
    }

    override suspend fun getTradesList(): Result<List<TradeItem>> = safeCall {
        client.get("$baseUrl/api/main?min=0&max=2&displayDepositedOnly=false").body()
    }

    override suspend fun getUSDTradesList(): Result<List<TradeItem>> = safeCall {
        client.get("$baseUrl/api/main?min=0&max=2&displayDepositedOnly=false&selectedCurrencyCode=USD").body()
    }


    private fun HttpRequestBuilder.withOrigin() {
        header("Origin", "https://arzook.ca")
        header("Referer", "https://arzook.ca/")
    }

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = client.post("$baseUrl/api/auth/login") {
                contentType(ContentType.Application.Json)
                withOrigin()
                parameter("recaptchaV3Token", request.recaptchaToken)
                setBody(mapOf("email" to request.email, "password" to request.password))
            }
            val bodyText = response.bodyAsText()
            println("[login] status=${response.status} rawBody=$bodyText")
            val raw = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromString(LoginResponse.serializer(), bodyText)
            println("[login] parsed: accessToken=${raw.accessToken} tokenType=${raw.tokenType} resolved=${raw.resolvedToken()}")
            Result.Success(raw)
        } catch (e: Exception) {
            println("[login] exception: ${e.message}")
            Result.Error(e.message ?: "Unknown error")
        }
    }
//    = safeCall {
//        client.post("$baseUrl/api/auth/login") {
//            contentType(ContentType.Application.Json)
//            withOrigin()
//            parameter("recaptchaV3Token", request.recaptchaToken)
//            setBody(mapOf("email" to request.email, "password" to request.password))
//        }.body()
//    }

    override suspend fun googleSignIn(idToken: String): Result<LoginResponse> {
        return try {
            val response = client.post("$baseUrl/api/auth/social-login") {
                contentType(ContentType.Application.Json)
                withOrigin()
                setBody(mapOf("idToken" to idToken))
            }
            val raw = response.bodyAsText()
            println("[googleSignIn] status=${response.status} body=$raw")
            val parsed = try {
                kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                    .decodeFromString(LoginResponse.serializer(), raw)
            } catch (_: Exception) {
                val token = Regex(""""(?:accessToken|access_token|token|userToken|id_token)"\s*:\s*"([^"]+)"""")
                    .find(raw)?.groupValues?.get(1) ?: return Result.Error("No token in response: $raw")
                LoginResponse(accessToken = token)
            }
            if (parsed.resolvedToken().isEmpty()) {
                Result.Error("Google sign-in returned empty token. Response: $raw")
            } else {
                Result.Success(parsed)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun register(user: User): Result<User> = safeCall {
        client.post("$baseUrl/api/auth/register") {
            contentType(ContentType.Application.Json)
            withOrigin()
            parameter("recaptchaV3Token", user.recaptchaToken)
            setBody(user)
        }.body()
    }

    override suspend fun getUserDetails(token: String): Result<AuthenticatedData> = safeCall {
        client.get("$baseUrl/api/profile/me") { header(HttpHeaders.Authorization, token.bearer()) }.body()
    }

    override suspend fun updateProfile(token: String, request: UpdateProfileRequest): Result<AuthenticatedData> = safeCall {
        val response = client.put("$baseUrl/api/profile/me") {
            header(HttpHeaders.Authorization, token.bearer())
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        val body = response.bodyAsText()
        println("[updateProfile] status=${response.status} body=$body")
        if (!response.status.isSuccess()) throw Exception("Update failed: ${response.status} $body")
        response.body()
    }

    override suspend fun getCurrentRate(token: String): Result<CurrentRate> = safeCall {
        client.get("$baseUrl/api/main/rate-stats?selectedCurrencyCode=undefined") { header(HttpHeaders.Authorization, token.bearer()) }.body()
    }

    override suspend fun getWalletStatus(token: String): Result<WalletStatus> = safeCall {
        client.get("$baseUrl/api/digital-wallet") { header(HttpHeaders.Authorization, token.bearer()) }.body()
    }

    override suspend fun getWatchList(token: String): Result<List<WatchItem>> = safeCall {
        client.get("$baseUrl/api/trading/watch") { header(HttpHeaders.Authorization, token.bearer()) }.body()
    }

    override suspend fun watchTrade(token: String, id: String): Result<Unit> = safeCall {
        client.post("$baseUrl/api/trading/watch/$id") { header(HttpHeaders.Authorization, token.bearer()) }
        Unit
    }

    override suspend fun unwatchTrade(token: String, id: String): Result<Unit> = safeCall {
        client.post("$baseUrl/api/trading/unwatch/$id") { header(HttpHeaders.Authorization, token.bearer()) }
        Unit
    }

    override suspend fun lockTrade(token: String, id: String): Result<LockedTrade> = safeCall {
        val response = client.post("$baseUrl/api/trading/lock") {
            header(HttpHeaders.Authorization, token.bearer())
            contentType(ContentType.Text.Plain)
            setBody(id)
        }
        val body = response.bodyAsText()
        println("[lockTrade] status=${response.status} body=$body")
        if (!response.status.isSuccess()) throw Exception("Lock failed: ${response.status} $body")
        kotlinx.serialization.json.Json { ignoreUnknownKeys = true; coerceInputValues = true }.decodeFromString(LockedTrade.serializer(), body)
    }

    override suspend fun unlockTrade(token: String, id: String): Result<Unit> = safeCall {
        client.post("$baseUrl/api/trading/unlock") {
            header(HttpHeaders.Authorization, token.bearer())
            contentType(ContentType.Application.Json)
            setBody(mapOf("id" to id))
        }
        Unit
    }

    override suspend fun buyTrade(token: String, id: String): Result<Unit> = safeCall {
        val response = client.post("$baseUrl/api/buyings") {
            header(HttpHeaders.Authorization, token.bearer())
            contentType(ContentType.Application.Json)
            setBody(mapOf("offeringId" to id))
        }
        val body = response.bodyAsText()
        println("[buyTrade] status=${response.status} body=$body")
        if (!response.status.isSuccess()) throw Exception("${response.status}: $body")
        Unit
    }

    override suspend fun sellTrade(token: String, id: String): Result<Unit> = safeCall {
        val response = client.post("$baseUrl/api/sellings") {
            header(HttpHeaders.Authorization, token.bearer())
            contentType(ContentType.Application.Json)
            setBody(mapOf("offeringId" to id))
        }
        val body = response.bodyAsText()
        println("[sellTrade] status=${response.status} body=$body")
        if (!response.status.isSuccess()) throw Exception("${response.status}: $body")
        Unit
    }

    override suspend fun getDepositList(token: String): Result<List<DigitalWalletItem>> = safeCall {
        client.get("$baseUrl/api/digital-wallet/items") { header(HttpHeaders.Authorization, token.bearer()) }.body()
    }

    override suspend fun getBuyingTrades(token: String): Result<List<TradeItem>> = safeCall {
        println("[getBuyingTrades] token=$token url=$baseUrl/api/trades/buying-trades")
        val response = client.get("$baseUrl/api/trades/buying-trades") { header(HttpHeaders.Authorization, token.bearer()) }
        val body = response.bodyAsText()
        println("[getBuyingTrades] status=${response.status} body=$body")
        response.body()
    }

    override suspend fun getSellingTrades(token: String): Result<List<TradeItem>> = safeCall {
        println("[getSellingTrades] token=$token url=$baseUrl/api/trades/selling-trades")
        val response = client.get("$baseUrl/api/trades/selling-trades") {
            header(HttpHeaders.Authorization, token.bearer())
        }
        val body = response.bodyAsText()
        println("[getSellingTrades] status=${response.status} body=$body")
        response.body()
    }

    override suspend fun getBuyingDrafts(token: String): Result<List<TradeItem>> = safeCall {
        println("[getBuyingDrafts] Authorization header=Bearer $token")
        val response = client.get("$baseUrl/api/buyings/buying-drafts") { header(HttpHeaders.Authorization, token.bearer()) }
        val body = response.bodyAsText()
        println("[getBuyingDrafts] status=${response.status} body=$body")
        response.body()
    }

    override suspend fun getSellingDrafts(token: String): Result<List<TradeItem>> = safeCall {
        println("[getSellingDrafts] token=$token url=$baseUrl/api/sellings")
        val response = client.get("$baseUrl/api/sellings") {
            header(HttpHeaders.Authorization, token.bearer())
        }
        val body = response.bodyAsText()
        println("[getSellingDrafts] status=${response.status} body=$body")
        response.body()
    }

    override suspend fun createSellingDraft(token: String, request: TradeItem): Result<TradeItem> = safeCall {
        client.post("$baseUrl/api/sellings") {
            header(HttpHeaders.Authorization, token.bearer())
            contentType(ContentType.Application.Json)
            withOrigin()
            setBody(request)
        }.body()
    }

    override suspend fun getPayees(token: String): Result<List<Payee>> = safeCall {
        client.get("$baseUrl/api/payees") { header(HttpHeaders.Authorization, token.bearer()) }.body()
    }

    override suspend fun addPayee(token: String, payee: Payee): Result<Payee> = safeCall {
        client.post("$baseUrl/api/payees") {
            header(HttpHeaders.Authorization, token.bearer())
            contentType(ContentType.Application.Json)
            setBody(payee)
        }.body()
    }

    override suspend fun getServiceRateForBuying(token: String, amount: Double): Result<Double> = safeCall {
        client.post("$baseUrl/api/trading/buying-taker-service-rate") {
            header(HttpHeaders.Authorization, token.bearer())
            contentType(ContentType.Application.Json)
            setBody(amount)
        }.body()
    }

    override suspend fun getServiceRateForSelling(token: String, amount: Double): Result<Double> = safeCall {
        client.post("$baseUrl/api/trading/selling-taker-service-rate") {
            header(HttpHeaders.Authorization, token.bearer())
            contentType(ContentType.Application.Json)
            setBody(amount)
        }.body()
    }

    override suspend fun getSellingMakerServiceRate(token: String, amount: Double, promoCode: String): Result<Double> = safeCall {
        client.post("$baseUrl/api/trading/selling-maker-service-rate") {
            header(HttpHeaders.Authorization, token.bearer())
            contentType(ContentType.Application.Json)
            setBody(mapOf("amount" to amount, "promoCode" to promoCode))
        }.body()
    }

    override suspend fun createBuyingSellingDraft(token: String, id:String): Result<Unit> = safeCall {
        val r = client.post("$baseUrl/api/trading/$id") {
            header(HttpHeaders.Authorization, token.bearer())
            withOrigin()
        }
        println("[createBuyingSellingDraft] status=${r.status} body=$r")
        if (!r.status.isSuccess()) throw Exception("${r.status}: $r")
        Unit
    }

    override suspend fun createBuyingDraft(token: String, request: ca.arzook.shared.model.TradeItem): Result<Unit> = safeCall {
        val json = kotlinx.serialization.json.Json { explicitNulls = false }
        val jsonBody = json.encodeToString(ca.arzook.shared.model.TradeItem.serializer(), request)
        println("[createBuyingDraft] json=$jsonBody")
        println("[createBuyingDraft] tokenPresent=${token.isNotEmpty()} tokenPrefix=${token.take(20)}")
        val response = client.post("$baseUrl/api/buyings") {
            header(HttpHeaders.Authorization, token.bearer())
            contentType(ContentType.Application.Json)
            withOrigin()
            setBody(request)
        }
        val body = response.bodyAsText()
        println("[createBuyingDraft] status=${response.status} body=$body")
        if (!response.status.isSuccess()) throw Exception("${response.status}: $body")
        Unit
    }

    override suspend fun createBuyingDraftWithItem(token: String, draft: ca.arzook.shared.model.TradeItem): Result<Unit> = safeCall {
        val json = kotlinx.serialization.json.Json { explicitNulls = false }
        val jsonBody = json.encodeToString(ca.arzook.shared.model.TradeItem.serializer(), draft)
        println("[createBuyingDraftWithItem] json=$jsonBody")
        val response = client.post("$baseUrl/api/buyings") {
            header(HttpHeaders.Authorization, token.bearer())
            contentType(ContentType.Application.Json)
            withOrigin()
            setBody(draft)
        }
        val body = response.bodyAsText()
        println("[createBuyingDraftWithItem] status=${response.status} body=$body")
        if (!response.status.isSuccess()) throw Exception("${response.status}: $body")
        Unit
    }

    override suspend fun updateBuyingAmount(
        token: String,
        id: String,
        amount: Double
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun updateBuyingRate(token: String, id: String, rate: Double): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteBuyingDraft(token: String, id: String): Result<Unit> = safeCall {
        client.delete("$baseUrl/api/buyings/$id") { header(HttpHeaders.Authorization, token.bearer()) }
        Unit
    }

    override suspend fun deleteSellingDraft(token: String, id: String): Result<Unit> = safeCall {
        client.delete("$baseUrl/api/sellings/$id") { header(HttpHeaders.Authorization, token.bearer()) }
        Unit
    }

    override suspend fun updateSellingAmount(token: String, id: String, amount: Double): Result<Unit> = safeCall {
        client.put("$baseUrl/api/sellings/update-amount/$id") {
            header(HttpHeaders.Authorization, token.bearer())
            contentType(ContentType.Application.Json)
            setBody(ca.arzook.shared.model.UpdateSellingAmountRequest(amount = amount.toString()))
        }
        Unit
    }

    override suspend fun updateSellingRate(token: String, id: String, rate: Double): Result<Unit> = safeCall {
        client.put("$baseUrl/api/sellings/update-asking-rate/$id") {
            header(HttpHeaders.Authorization, token.bearer())
            contentType(ContentType.Application.Json)
            setBody(ca.arzook.shared.model.UpdateSellingRateRequest(rate = rate.toString()))
        }
        Unit
    }

    override suspend fun updateSellingPayee(token: String, id: String, sheba: String, payeeName: String): Result<Unit> = safeCall {
        client.put("$baseUrl/api/sellings/update-payee-bank-info") {
            header(HttpHeaders.Authorization, token.bearer())
            contentType(ContentType.Application.Json)
            setBody(ca.arzook.shared.model.UpdateSellingPayeeRequest(id = id, sheba = sheba, payeeName = payeeName))
        }
        Unit
    }

    override suspend fun createSellingDraftWithItem(token: String, draft: ca.arzook.shared.model.TradeItem): Result<Unit> = safeCall {
        val json = kotlinx.serialization.json.Json { explicitNulls = false }
        val jsonBody = json.encodeToString(ca.arzook.shared.model.TradeItem.serializer(), draft)
        println("[createSellingDraftWithItem] json=$jsonBody")
        val response = client.post("$baseUrl/api/sellings") {
            header(HttpHeaders.Authorization, token.bearer())
            contentType(ContentType.Application.Json)
            withOrigin()
            setBody(draft)
        }
        val body = response.bodyAsText()
        println("[createSellingDraftWithItem] status=${response.status} body=$body")
    }

    override suspend fun printBuyingTrade(token: String, id: String): Result<ByteArray> = safeCall {
        client.post("$baseUrl/api/trades/print-buying-trade") {
            header(HttpHeaders.Authorization, token.bearer())
            contentType(ContentType.Application.Json)
            setBody(mapOf("id" to id))
        }.body()
    }

    override suspend fun printSellingTrade(token: String, id: String): Result<ByteArray> = safeCall {
        client.post("$baseUrl/api/trades/print-selling-trade") {
            header(HttpHeaders.Authorization, token.bearer())
            contentType(ContentType.Application.Json)
            setBody(mapOf("id" to id))
        }.body()
    }

    override suspend fun validatePromoCode(token: String, promoCode: String): Result<PromoCodeResponse> = safeCall {
        client.get("$baseUrl/api/trading/promo-code/$promoCode") {
            header(HttpHeaders.Authorization, token.bearer())
            withOrigin()
        }.body()
    }
}
