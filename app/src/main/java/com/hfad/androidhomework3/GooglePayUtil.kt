package com.hfad.androidhomework3

import android.app.Activity
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.wallet.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object GooglePayUtil {

    private val merchantInfo: JSONObject
        @Throws(JSONException::class)
        get() = JSONObject().put("merchantName", Constants.MERCHANT_NAME)

    private fun getBaseRequest(): JSONObject = JSONObject()
        .put("apiVersion", 2)
        .put("apiVersionMinor", 0)

    private val environment: Int
        get() = if (BuildConfig.DEBUG) {
            WalletConstants.ENVIRONMENT_TEST
        } else {
            WalletConstants.ENVIRONMENT_PRODUCTION
        }

    private fun createTokenizationParameters(): JSONObject {
        val tokenizationSpecification = JSONObject()
        tokenizationSpecification.put("type", "PAYMENT_GATEWAY")
        tokenizationSpecification.put(
            "parameters",
            JSONObject()
                .put("gateway", "sberbank")
                .put("gatewayMerchantId", "exampleMerchantId")
        )
        return tokenizationSpecification
    }

    private fun getAllowedCardNetworks(): JSONArray = JSONArray()
        .put("MASTERCARD")
        .put("VISA")

    private fun getAllowedCardAuthMethods(): JSONArray = JSONArray()
            .put("PAN_ONLY")
            .put("CRYPTOGRAM_3DS")

    fun createPaymentsClient(activity: Activity): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(environment)
            .build()
        Log.d(Constants.TAG_LOG, "environment: $environment")
        return Wallet.getPaymentsClient(activity, walletOptions)
    }

    fun getIsReadyToPayRequest(
        paymentsClient: PaymentsClient,
        listener: OnCompleteListener<Boolean>
    ) {

        val request = JSONObject(getBaseRequest().toString()).apply {
            put("allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod()))
        }
        val isReadyToPayRequest = IsReadyToPayRequest.fromJson(request.toString())
        val task = paymentsClient.isReadyToPay(isReadyToPayRequest)
        task.addOnCompleteListener(listener)
    }

    private fun baseCardPaymentMethod(): JSONObject {
        val baseCardPaymentMethod = JSONObject().apply {
            put("type", "CARD")
        }
        val parameters = JSONObject()
        parameters.put("allowedAuthMethods", getAllowedCardAuthMethods())
        parameters.put("allowedCardNetworks", getAllowedCardNetworks())
        baseCardPaymentMethod.put("parameters", parameters)
        return  baseCardPaymentMethod
    }

    private fun getTransactionInfo(): JSONObject =
        JSONObject().apply {
            put("totalPrice", "2.45")
            put("totalPriceStatus", "FINAL")
            put("currencyCode", "USD")
        }

    private fun getCardPaymentMethod(): JSONObject {
        val cardPaymentMethod = baseCardPaymentMethod()
        cardPaymentMethod.put("tokenizationSpecification", createTokenizationParameters())
        return cardPaymentMethod
    }

    fun getPaymentDataRequest(): PaymentDataRequest {
        val paymentDataRequest = getBaseRequest()
        paymentDataRequest.put(
            "allowedPaymentMethods",
            JSONArray()
                .put(getCardPaymentMethod()))
        paymentDataRequest.put("transactionInfo", getTransactionInfo())
        paymentDataRequest.put("merchantInfo", merchantInfo)
        paymentDataRequest.put("shippingAddressRequired", false);

        return PaymentDataRequest.fromJson(paymentDataRequest.toString())
    }
}
