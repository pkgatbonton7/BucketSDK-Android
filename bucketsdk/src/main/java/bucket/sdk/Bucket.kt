package bucket.sdk

import android.content.Context
import android.net.Uri
import android.os.Build
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.androidnetworking.interfaces.OkHttpResponseAndJSONObjectRequestListener
import okhttp3.Response
import kotlin.collections.ArrayList

class Bucket {

    companion object {

        @JvmStatic private var tz : TimeZone = TimeZone.getTimeZone("UTC")
        @JvmStatic private var df : SimpleDateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            get() {
                if (field.timeZone != tz) field.timeZone = tz
                return field
            }

        @JvmStatic var appContext : Context? = null
            set(value) {
                // Make this a regular setter:
                field = value
                if (!value.isNil) {
                    AndroidNetworking.initialize(value)
                }
            }

        @JvmStatic var environment : DeploymentEnvironment = DeploymentEnvironment.Development
        @JvmStatic private var denoms : List<Int> = listOf(10000, 5000, 2000, 1000, 500, 200, 100)
        @JvmStatic private var usesNaturalChangeFunction : Boolean
            get() { return Credentials.sharedPrefs?.getBoolean("USES_NATURAL_CHANGE", false) ?: false }
            set(value) {
                val editor = Credentials.sharedPrefs?.edit()
                editor?.putBoolean("USES_NATURAL_CHANGE", value)
                editor?.apply()
            }

        @JvmStatic fun registerDevice(callback: Callbacks.RegisterTerminal?) {

            val theURL = environment.regTerminal().build().toString()
            val terminalId = Build.SERIAL
            val retailerId = Credentials.retailerId()

            // If either of these are nil, we need to throw an error:
            var jsonBody : JSONObject? = null
            if (terminalId.isNil || retailerId.isNil) {
                callback?.didError(Bucket.Error.invalidRetailer)
            }

            jsonBody = JSONObject()
            jsonBody.put("terminalId", terminalId)
            jsonBody.put("retailerId", retailerId!!)

            AndroidNetworking.post(theURL)
                    .addJSONObjectBody(jsonBody)
                    .setContentType("application/json; charset=utf8")
                    .build()
                    .getAsOkHttpResponseAndJSONObject(object : OkHttpResponseAndJSONObjectRequestListener {
                        override fun onResponse(okHttpResponse: Response?, response: JSONObject?) {
                            // See if the device is approved or not:
                            when (okHttpResponse?.code()) {
                                200 -> {
                                    val apiKey = response?.getString("apiKey")
                                    if (!apiKey.isNil) {
                                        Credentials.setRetailerSecret(apiKey!!)
                                    }
                                    callback?.deviceIsApproved()
                                }
                                201 -> {
                                    callback?.deviceWasRegistered()
                                }
                                else -> {
                                    callback?.didError(null)
                                }
                            }
                        }
                        override fun onError(anError: ANError?) {
                            callback?.didError(anError?.bucketError)
                        }
                    })
        }

        @JvmStatic fun bucketAmount(changeDueBack: Long): Long {
            var bucketAmount = changeDueBack
            if (usesNaturalChangeFunction) {
                // Make sure this is ordered by the amount
                val filteredDenoms = denoms.filter { it <= changeDueBack }

                // These values should already be descended from 10000 down to 100.
                filteredDenoms.forEach { denomination ->
                    bucketAmount = (bucketAmount % denomination)
                }

            } else {
                while (bucketAmount > 100) bucketAmount = (bucketAmount % 100)

            }
            return bucketAmount
        }

        @JvmStatic fun close(interval: String, callback: Callbacks.CloseInterval?) {

            // Get the client id & client secret for this retailer:
            val retailerId = Credentials.retailerId()
            val retailerSecret = Credentials.retailerSecret()

            var shouldIReturn = false
            if (retailerId.isNullOrEmpty() || retailerSecret.isNullOrEmpty()) {
                shouldIReturn = true
                callback?.didError(Error.unauthorized)
            }

            if (shouldIReturn) return

            // Okay we have the information we need for the request:
            val theURL = environment.closeInterval(retailerId!!, interval).build().toString()

            AndroidNetworking.get(theURL)
                    .addHeaders("x-functions-key", retailerSecret!!)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onError(anError: ANError?) {
                            callback?.didError(anError?.bucketError)
                        }
                        override fun onResponse(response: JSONObject?) {
                            callback?.let {
                                response?.let {
                                    it.getString("intervalId")?.let {
                                        callback.closedInterval(it)
                                    }
                                }
                            }
                        }
                    })

        }

        @JvmStatic fun fetchBillDenominations(countryCode: String, callback: Callbacks.BillDenomination?) {

            AndroidNetworking.get("https://bucketresources.blob.core.windows.net/static/Currencies.json")
                    .build().getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            // Deal with the data:
                            val denoms = response?.getJSONArray("currencies")
                            denoms?.let {
                                for (i in 0..(it.length()-1)) {

                                    val json = it.getJSONObject(i)

                                    if (json.getString("currencyCode") != countryCode) continue
                                    // Okay we are good to process.. Lets check if we need to set this for the natural change function:
                                    if (json.getBoolean("useNaturalChangeFunction")) {
                                        usesNaturalChangeFunction = true
                                        val theds = json.getJSONArray("commonDenominations")
                                        val theDenoms:MutableList<Int> = ArrayList()
                                        for (j in 0..(theds.length()-1)) {
                                            theDenoms.add(j, theds.getInt(j))
                                        }
                                        // Now set the denominations:
                                        Bucket.denoms = theDenoms
                                    } else usesNaturalChangeFunction = false
                                    // Let our interface know we finished processing:
                                    callback?.setBillDenoms()
                                }
                            }
                        }
                        override fun onError(anError: ANError?) {
                            callback?.didError(anError.bucketError)
                        }
                    })
        }

    }
    class Callbacks {
        abstract class RegisterTerminal {
            abstract fun deviceIsApproved()
            abstract fun deviceWasRegistered()
            abstract fun didError(error: Bucket.Error?)
        }
        abstract class CreateTransaction {
            abstract fun transactionCreated()
            abstract fun didError(error: Bucket.Error?)
        }
        abstract class BillDenomination {
            abstract fun setBillDenoms()
            abstract fun didError(error: Bucket.Error?)
        }
        abstract class CloseInterval {
            abstract fun closedInterval(intervalId: String?)
            abstract fun didError(error: Bucket.Error?)
        }
    }

    class Error(var message: String?, var detail : String?, var code : Int?) {
        companion object {
            @JvmStatic val unauthorized : Bucket.Error = Error("Unauthorized", "Check your retailer id & retailer secret", 401)
            @JvmStatic val unsupportedMethod : Bucket.Error = Bucket.Error("Unsupported API function.", "THE_METHOD", null)
            @JvmStatic val invalidRetailer : Bucket.Error = Bucket.Error("Invalid Retailer Id", "Please Check Retailer Id and Secret Code", 401)
        }
    }

    class Retailer {
        companion object {
            @JvmStatic fun logInWith(password: String, username: String, callback: Callbacks.RegisterTerminal?) {
                callback?.didError(Error.unsupportedMethod)
            }
        }
    }

    class Transaction(var amount: Int, var clientTransactionId: String, var totalAmount: Int) {

        // This is the primary key for the transaction in our db, as annotated:
        @PrimaryKey var bucketTransactionId : String? = null
        var customerCode                    : String?  = null
        var qrCodeContent                   : URL? = null

        // The rest of these fields are specified by the retailer:
        var intervalId                      : String? = null

        var locationId                      : String? = null
        var terminalId                      : String? = Build.SERIAL

        private fun updateWith(updateJSON: JSONObject?) {

            if (updateJSON.isNil) return

            this.customerCode = updateJSON!!.getString("customerCode")
            this.bucketTransactionId = updateJSON.getString("bucketTransactionId")
            this.qrCodeContent = updateJSON.getURL("qrCodeContent")

        }

        private fun toJSON(): JSONObject {

            val obj = JSONObject()

            // Set the intervalId to this date:
            intervalId = df.format(Date())
            // We will always set the amount & clientTransactionId when sending the JSON:
            obj.put("amount", amount)
            obj.put("totalTransactionAmount", totalAmount)
            obj.put("clientTransactionId", clientTransactionId)
            obj.put("intervalId", intervalId)
            obj.put("terminalId", terminalId)

            if (!locationId.isNil) { obj.put("locationId", locationId!!) }
            if (!customerCode.isNil) { obj.put("customerCode", customerCode!!) }
            if (!qrCodeContent.isNil) { obj.put("qrCodeContent", qrCodeContent!!) }

            return obj
        }

        fun create(callback: Callbacks.CreateTransaction?) {

            // Get the client id & client secret for this retailer:
            val retailerId = Credentials.retailerId()
            val retailerSecret = Credentials.retailerSecret()

            var shouldIReturn = false
            if (retailerId.isNullOrEmpty() || retailerSecret.isNullOrEmpty()) {
                shouldIReturn = true
                callback?.didError(Error.unauthorized)
            }

            if (shouldIReturn) return

            val jsonBody = this.toJSON()

            val url = environment.transaction(retailerId!!).build().toString()

            val build = AndroidNetworking.post(url)
                    .setContentType("application/json; charset=UTF-8")
                    .addHeaders("x-functions-key", retailerSecret!!)
                    .addJSONObjectBody(jsonBody)
                    .build()

            build.getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    this@Transaction.updateWith(response)
                    callback?.transactionCreated()
                }
                override fun onError(anError: ANError?) {
                    callback?.didError(anError?.bucketError)
                }
            })
        }

    }

    object Credentials {
        @JvmStatic val sharedPrefs = appContext?.getSharedPreferences("SHAREDPREFS", Context.MODE_PRIVATE)
        @JvmStatic fun retailerId(): String? {
            // The only thing hardcoded here is the /clientId.  This makes it so the hacker would need the actual device to read the clientId or clientSecret, which would mean we already have a security breach.
            return sharedPrefs?.getString("RETAILER_ID", null)
        }

        @JvmStatic fun setRetailerId(value: String) {
            // The only thing hardcoded here is the /clientId.  This makes it so the hacker would need the actual device to read the clientId or clientSecret, which would mean we already have a security breach.
            val editor = sharedPrefs?.edit()
            editor?.putString("RETAILER_ID", value)
            editor?.apply()
        }

        @JvmStatic fun retailerSecret(): String? {
            // The only thing hardcoded here is the /clientSecret.  This makes it so the hacker would need the actual device to read the clientId or clientSecret, which would mean we already have a security breach.
            return sharedPrefs?.getString("RETAILER_SECRET", null)
        }

        @JvmStatic fun setRetailerSecret(value: String) {
            // The only thing hardcoded here is the /clientSecret.  This makes it so the hacker would need the actual device to read the clientId or clientSecret, which would mean we already have a security breach.
            val editor = sharedPrefs?.edit()
            editor?.putString("RETAILER_SECRET", value)
            editor?.apply()
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
        fun transaction(clientId : String): Uri.Builder {
            return bucketBaseUri().appendPath("transaction").appendPath(clientId)
        }
        fun closeInterval(clientId: String, intervalId : String): Uri.Builder {
            return bucketBaseUri().appendPath("closeInterval").appendPath(clientId).appendPath(intervalId)
        }
        fun regTerminal(): Uri.Builder {
            return retailerBaseUri().appendPath("registerterminal")
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

var ANError?.bucketError : Bucket.Error?
    get() {
        if (this.isNil) return null
        println(this!!.errorCode)
        val code = this.errorCode
        if (code == 401) return Bucket.Error.unauthorized
        else if (code == 400) {
            val json = JSONObject(this.errorBody)
            val message = json.getString("message")
            return Bucket.Error(message, message, code)
        } else {
            return Bucket.Error(this.errorBody, this.errorDetail, this.errorCode)
        }
    }
    private set(value) {  }
