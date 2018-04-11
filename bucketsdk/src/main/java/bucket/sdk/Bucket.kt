package bucket.sdk

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import de.adorsys.android.securestoragelibrary.SecurePreferences
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class Bucket {
    companion object {
        @JvmStatic var requestQueue : RequestQueue? = null
        @SuppressLint("SimpleDateFormat") @JvmStatic val df : DateFormat = SimpleDateFormat("yyyyMMdd")

        @JvmStatic var appContext : Context? = null
            set(value) {
                // Make this a regular setter:
                field = value
                if (!value.isNil) {
                    this.requestQueue = Volley.newRequestQueue(value!!)
                }
            }

        @JvmStatic var environment : Bucket.DeploymentEnvironment = Bucket.DeploymentEnvironment.Development
        @JvmStatic private var denoms : List<Int> = listOf(10000, 5000, 2000, 1000, 500, 200)
        @JvmStatic fun bucketAmount(changeDueBack: Long): Long {
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
    class Callbacks {
        abstract class RetailerLogin {
            abstract fun didLogIn()
            abstract fun didError(error: Bucket.Error?)
        }
        abstract class CreateTransaction {
            abstract fun transactionCreated()
            abstract fun didError(error: Bucket.Error?)
        }
    }

    class Error(var message: String?, var code : Int?)

    class Retailer {
        companion object {
            @JvmStatic fun logInWith(password: String, username: String, callback: Bucket.Callbacks.RetailerLogin?) {

                val json = JSONObject()
                json.put("password", password)
                json.put("username", username)

                val url = Bucket.environment.retailerLogin().build().toString()

                val request = JsonObjectRequest(Request.Method.POST, url, json, Response.Listener { responseJSON ->

                    val clientId = responseJSON.getString("client_id")
                    val clientSecret = responseJSON.getString("client_secret")

                    if (clientId.isNullOrEmpty() || clientSecret.isNullOrEmpty()) return@Listener

                    // Now go & set the values securely:
                    Bucket.Credentials.setClientId(clientId)
                    Bucket.Credentials.setClientSecret(clientSecret)

                    // Notify our listeners:
                    callback?.didLogIn()

                }, Response.ErrorListener { error ->
                    callback?.didError(error.bucketError)
                })

                request.headers.set("Content-Type", "application/json; charset=UTF-8")

                Bucket.requestQueue?.add(request)

            }
        }
    }

    class Transaction(var amount: Int, var clientTransactionId: String) {

        // This is the primary key for the transaction in our db, as annotated:
        @PrimaryKey var bucketTransactionId : String? = null
        var customerCode                    : String?  = null
        var qrCodeContent                   : URL? = null

        // The rest of these fields are specified by the retailer:
        var intervalId                      : String? = null

        var locationId                      : String? = null
        var terminalId                      : String? = Build.SERIAL

        private fun updateWith(json: JSONObject) {

            this.customerCode = json.getString("customerCode")
            this.bucketTransactionId = json.getString("bucketTransactionId")
            this.qrCodeContent = json.getURL("qrCodeContent")

        }

        private fun toJSON(): JSONObject {

            val obj = JSONObject()

            // Set the intervalId to this date:
            intervalId = Date().toYYYYMMDD
            // We will always set the amount & clientTransactionId when sending the JSON:
            obj.put("amount", amount)
            obj.put("clientTransactionId", clientTransactionId)
            obj.put("intervalId", intervalId)
            obj.put("terminalId", terminalId)

            if (!locationId.isNil) { obj.put("locationId", locationId!!) }
            if (!customerCode.isNil) { obj.put("customerCode", customerCode!!) }
            if (!qrCodeContent.isNil) { obj.put("qrCodeContent", qrCodeContent!!) }

            return obj
        }

        fun create(callback: Bucket.Callbacks.CreateTransaction?) {

            // Get the client id & client secret for this retailer:
            val clientId = Bucket.Credentials.clientId()
            val clientSecret = Bucket.Credentials.clientSecret()

            var shouldIReturn = false
            if (clientId.isNullOrEmpty() || clientSecret.isNullOrEmpty()) {
                shouldIReturn = true
                callback?.didError(Bucket.Error("You must have a client id & client secret.",null))
            }

            if (shouldIReturn) return

            val urlBuilder = Bucket.environment.transaction(clientId!!, clientSecret!!)

            val url = urlBuilder.build().toString()

            // Get the request body JSON:
            val httpBody = this.toJSON()

            // Okay we need to go & create the request & send the information to Marco:
            val request = JsonObjectRequest(Request.Method.POST, url, httpBody, Response.Listener { response ->
                // Deal with the response object:
                this.updateWith(response)
                callback?.transactionCreated()

            }, Response.ErrorListener { error ->
                // Tell the listener that we had an error:
                callback?.didError(error.bucketError)

            })
            request.headers.set("Content-Type", "application/json; charset=UTF-8")
            Bucket.requestQueue?.add(request)

        }

    }

    object Credentials {

        @JvmStatic fun clientId(): String? {
            // The only thing hardcoded here is the /clientId.  This makes it so the hacker would need the actual device to read the clientId or clientSecret, which would mean we already have a security breach.
            @SuppressLint("HardwareIds") var theKey = Settings.Secure.getString(Bucket.appContext?.contentResolver, Settings.Secure.ANDROID_ID)
            theKey += "/clientId"
            return SecurePreferences.getStringValue(theKey, null)

        }

        @JvmStatic fun setClientId(value: String) {
            // The only thing hardcoded here is the /clientId.  This makes it so the hacker would need the actual device to read the clientId or clientSecret, which would mean we already have a security breach.
            @SuppressLint("HardwareIds") var theKey = Settings.Secure.getString(appContext?.contentResolver, Settings.Secure.ANDROID_ID)
            theKey += "/clientId"
            SecurePreferences.setValue(theKey, value)
        }

        @JvmStatic fun clientSecret(): String? {
            // The only thing hardcoded here is the /clientSecret.  This makes it so the hacker would need the actual device to read the clientId or clientSecret, which would mean we already have a security breach.
            @SuppressLint("HardwareIds") var theKey = Settings.Secure.getString(appContext?.contentResolver, Settings.Secure.ANDROID_ID)
            theKey += "/clientSecret"
            return SecurePreferences.getStringValue(theKey, null)
        }

        @JvmStatic fun setClientSecret(value: String) {
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
        fun transaction(clientId: String, clientSecret: String): Uri.Builder {
            return this.bucketBaseUri().appendPath("transaction").appendPath(clientId).appendQueryParameter("code", clientSecret)
        }

        fun retailerLogin(): Uri.Builder {
            return this.retailerBaseUri().appendPath("login")
        }
    }
}

private fun JSONObject?.getURL(name : String): URL? {
    if (this.isNil) return null
    val stringVal = this!!.getString(name)
    return URL(stringVal)
}

annotation class PrimaryKey
var Any?.isNil : Boolean
    get() { return this == null }
    private set(value) {}

var Date.toYYYYMMDD : String
    get() { return Bucket.df.format(this) }
    set(value) {  }

var VolleyError?.bucketError : Bucket.Error?
    get() {
        if (this.isNil) return null
        val code = this!!.networkResponse.statusCode
        val json = JSONObject(String(this.networkResponse.data))
        return Bucket.Error(json.getString("message"), code)
    }
    set(value) {  }
