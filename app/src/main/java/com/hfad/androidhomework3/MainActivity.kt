package com.hfad.androidhomework3

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentsClient
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var mPaymentsClient: PaymentsClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mPaymentsClient = GooglePayUtil.createPaymentsClient(this)
        GooglePayUtil.getIsReadyToPayRequest(mPaymentsClient) { task ->
            try {
                val result = task.getResult(ApiException::class.java) ?: false
                setGooglePayAvailable(result)
            } catch (e: ApiException) {
                Log.w(Constants.TAG_LOG, e);
            }
        }
        btn_google_pay.setOnClickListener {
            setButtonClickable(false)
                val request = GooglePayUtil.getPaymentDataRequest()
                mPaymentsClient.let {
                    // showLoading()
                    AutoResolveHelper.resolveTask(
                        it.loadPaymentData(request),
                        this,
                        Constants.LOAD_PAYMENT_DATA_REQUEST_CODE
                    )
                }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        setButtonClickable(true)
        when (requestCode) {
            Constants.LOAD_PAYMENT_DATA_REQUEST_CODE ->
                when (resultCode) {
                    Activity.RESULT_OK ->
                        data?.let {
                            val paymentData = PaymentData.getFromIntent(data)
                            handlePaymentSuccess(paymentData)
                        }
                    Activity.RESULT_CANCELED -> {

                    }
                    AutoResolveHelper.RESULT_ERROR -> {
                        data?.let {
                            val status = AutoResolveHelper.getStatusFromIntent(data)
                            handleError(status?.statusMessage ?: "Nothing")
                        }
                    }
                    else -> {
                    }
                }
        }
    }

    private fun handleError(statusError: String) {
        Log.w(Constants.TAG_LOG, "Status message $statusError")
    }

    private fun handlePaymentSuccess(paymentData: PaymentData?) {
        val paymentInformation = paymentData?.toJson() ?: return
        var paymentMethodData: JSONObject
        try {
            paymentMethodData = JSONObject(paymentInformation).getJSONObject("paymentMethodData")
            val token = paymentMethodData.getJSONObject("tokenizationData").getString("token")
            Toast.makeText(this, "Token: $token", Toast.LENGTH_LONG).show()
            Log.d(Constants.TAG_LOG, token)
        } catch (e: JSONException) {
            Log.e(Constants.TAG_LOG, "Error: $e")
            return
        }
    }

    private fun setGooglePayAvailable(result: Boolean) {
        if (result) {
            btn_google_pay.visibility = View.VISIBLE
        } else {
            Log.d(Constants.TAG_LOG, "Button hasn't been shown");
        }
    }

    private fun setButtonClickable(isClickable: Boolean) {
        btn_google_pay.isClickable = isClickable
    }
}
