package com.bucket.bucketsdk

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import de.adorsys.android.securestoragelibrary.SecurePreferences
import org.json.JSONObject
import java.net.URL
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class Bucket {
    var requestQueue : RequestQueue = Volley.newRequestQueue(Bucket.appContext)
    companion object {
        @SuppressLint("SimpleDateFormat") @JvmStatic val df : DateFormat = SimpleDateFormat("yyyyMMdd")
        @JvmStatic var appContext : Context? = null
        @JvmStatic var environment : Bucket.DeploymentEnvironment = Bucket.DeploymentEnvironment.Development
        @JvmStatic var denoms : List<Int> = listOf(10000, 5000, 2000, 1000, 500, 200)
        @JvmStatic fun bucketAmount(changeDueBack: Int): Int {
            var bucketAmount = changeDueBack
            // Make sure this is ordered by the amount
            val filteredDenoms = denoms.filter { it <= changeDueBack }

            // These values should already be descended from 10000 down to 100.
            filteredDenoms.forEach { denomination ->
                bucketAmount = (bucketAmount % denomination)
            }
            return bucketAmount
        }
    }
    abstract class Callbacks {
        abstract class RetailerLogin {
            abstract fun didLogIn()
            abstract fun didError(error: VolleyError?)
        }
        abstract class CreateTransaction {
            abstract fun transactionCreated()
            abstract fun didError(error: VolleyError?)
        }
    }

    class Retailer {
        companion object {
            @JvmStatic fun logInWith(password: String, username: String, callback: Bucket.Callbacks.RetailerLogin?): JsonObjectRequest {

                val json = JSONObject()

                json.put("password", password)
                json.put("username", username)

                val url = Bucket.environment.retailerLogin().build().toString()

                val request = JsonObjectRequest(Request.Method.POST, url, json, Response.Listener { _ ->
                    callback.let { callback!!.didLogIn() }
                }, Response.ErrorListener { error ->
                    callback.let { callback!!.didError(error) }
                })

                // Return the request to add in the queue
                return request

            }
        }
    }

    class Transaction(var amount: Int, var clientTransactionId: String) {

        var customerCode                 : String?  = null
        var qrCodeContent       : URL = URL("https://test.com")

        // The rest of these fields are specified by the retailer:
        var intervalId          : String? = null
        var locationId          : String? = null
        var terminalId          : String? = null

        // Primary key (for bucket)
        var bucketTransactionId : String? = null


        init {

            this.intervalId = ""
            this.locationId = "Ryan's Temporary Location"
            this.terminalId = Build.SERIAL
        }

        private fun updateWith(json: JSONObject) {
            this.clientTransactionId = json.getString("clientTransactionId")
            this.customerCode = json.getString("customerCode")
            this.bucketTransactionId = json.getString("bucketTransactionId")

            // Now take care of the qrCode contents:
            val qrCodeContent = json.getString("qrCodeContent")
            qrCodeContent.let { this.qrCodeContent = URL(qrCodeContent) }

        }

        private fun toJSON(): JSONObject {

            val obj = JSONObject()

            // We will always set the amount when sending the JSON:
            obj.put("amount", amount)
            intervalId = Date().now
            locationId.let { obj.put("locationId", it) }
            clientTransactionId.let { obj.put("clientTransactionId", it) }
            terminalId.let { obj.put("terminalId", it) }
            customerCode.let { obj.put("customerCode", it) }
            qrCodeContent.let { obj.put("qrCodeContent", it) }

            return obj
        }

        fun create(callback: Bucket.Callbacks.CreateTransaction?): JsonObjectRequest? {

            val clientId = Bucket.Credentials.clientId()
            val clientSecret = Bucket.Credentials.clientSecret()
            if (clientId == null || clientSecret == null) {
                callback.let { it!!.didError(VolleyError("You need a client id & a client secret to create a transaction.")) }
                return null
            }

            val urlBuilder = Bucket.environment.transaction()

            // Modify the URL String for this request:
            urlBuilder.appendPath(clientId)
            urlBuilder.appendQueryParameter("code", clientSecret)

            val url = urlBuilder.build().toString()

            // Get the request body JSON:
            val httpBody = this.toJSON()

            Log.d("CREATE TRANS", url)
            // Okay we need to go & create the request & send the information to Marco:
            val request = JsonObjectRequest(Request.Method.POST, url, httpBody, Response.Listener<JSONObject> { response ->
                // Deal with the response object:
                this.updateWith(response)
                callback.let { it!!.transactionCreated() }

            }, Response.ErrorListener { error ->
                callback.let { it!!.didError(error) }
            })

            return request

        }

    }

    object Credentials {

        fun clientId(): String? {
            // The only thing hardcoded here is the /clientId.  This makes it so the hacker would need the actual device to read the clientId or clientSecret, which would mean we already have a security breach.
            @SuppressLint("HardwareIds") var theKey = Settings.Secure.getString(Bucket.appContext?.contentResolver, Settings.Secure.ANDROID_ID)
            theKey += "/clientId"
            return SecurePreferences.getStringValue(theKey, null)

        }

        fun setClientId(value: String) {
            // The only thing hardcoded here is the /clientId.  This makes it so the hacker would need the actual device to read the clientId or clientSecret, which would mean we already have a security breach.
            @SuppressLint("HardwareIds") var theKey = Settings.Secure.getString(appContext?.contentResolver, Settings.Secure.ANDROID_ID)
            theKey += "/clientId"
            SecurePreferences.setValue(theKey, value)
        }

        fun clientSecret(): String? {
            // The only thing hardcoded here is the /clientSecret.  This makes it so the hacker would need the actual device to read the clientId or clientSecret, which would mean we already have a security breach.
            @SuppressLint("HardwareIds") var theKey = Settings.Secure.getString(appContext?.contentResolver, Settings.Secure.ANDROID_ID)
            theKey += "/clientSecret"
            return SecurePreferences.getStringValue(theKey, null)
        }

        fun setClientSecret(value: String) {
            // The only thing hardcoded here is the /clientSecret.  This makes it so the hacker would need the actual device to read the clientId or clientSecret, which would mean we already have a security breach.
            @SuppressLint("HardwareIds") var theKey = Settings.Secure.getString(appContext?.contentResolver, Settings.Secure.ANDROID_ID)
            theKey += "/clientSecret"
            SecurePreferences.setValue(theKey, value)
        }

    }

    enum class DeploymentEnvironment {

        // Cases: (Production & Development for now)
        Production, Development;

        // Case URL Endpoint:
        private fun bucketBaseUri(): Uri.Builder {
            val builder = Uri.Builder()
            builder.scheme("https")
            when (this) {
                Production -> builder.authority(appContext?.getString(R.string.prodEndpoint))
                Development -> builder.authority(appContext?.getString(R.string.devEndpoint))
            }
            builder.appendPath("api")
            return builder
        }

        // Case URL Endpoint:
        private fun retailerBaseUri(): Uri.Builder {
            val builder = Uri.Builder()
            builder.scheme("https")
            when (this) {
                Production -> builder.authority(appContext?.getString(R.string.prodEndpoint))
                Development -> builder.authority(appContext?.getString(R.string.devEndpoint))
            }
            builder.appendPath("api")
            return builder
        }

        // PRE-BUILT ENDPOINT PATHS:
        fun redeem(): Uri.Builder {
            return this.bucketBaseUri().appendPath("redeem")
        }

        fun transaction(): Uri.Builder {
            return this.bucketBaseUri().appendPath("transaction")
        }

        fun retailerLogin(): Uri.Builder {
            return this.retailerBaseUri().appendPath("login")
        }
    }
}

var Date.now : String
    get() { return Bucket.df.format(this) }
    set(value) {  }