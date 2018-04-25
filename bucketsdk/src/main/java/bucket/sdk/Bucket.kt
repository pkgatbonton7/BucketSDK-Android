package bucket.sdk

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.Settings
import de.adorsys.android.securestoragelibrary.SecurePreferences
import org.json.JSONObject
import java.net.URL
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlin.collections.ArrayList

class Bucket {

    companion object {

        @JvmStatic private var tz : TimeZone = TimeZone.getTimeZone("UTC")
        @JvmStatic private var df : DateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
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
            get() { return SecurePreferences.getBooleanValue("usesNaturalChangeFunction", false) }
            set(value) { SecurePreferences.setValue("usesNaturalChangeFunction", value) }

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
            val retailerId = Credentials.clientId()
            val retailerSecret = Credentials.clientSecret()

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
                            callback?.closedInterval()
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
        abstract class RetailerLogin {
            abstract fun didLogIn()
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
            abstract fun closedInterval()
            abstract fun didError(error: Bucket.Error?)
        }
    }

    class Error(var message: String?, var code : Int?) {
        companion object {
            @JvmStatic val unauthorized : Bucket.Error = Error("Unauthorized", 401)
        }
    }

    class Retailer {
        companion object {
            @JvmStatic fun logInWith(password: String, username: String, callback: Callbacks.RetailerLogin?) {

                val json = JSONObject()
                json.put("password", password)
                json.put("username", username)

                callback?.didError(Error("Retailer login is not supported just yet.", null))

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
            val retailerId = Credentials.clientId()
            val retailerSecret = Credentials.clientSecret()

            var shouldIReturn = false
            if (retailerId.isNullOrEmpty() || retailerSecret.isNullOrEmpty()) {
                shouldIReturn = true
                callback?.didError(Error.unauthorized)
            }

            if (shouldIReturn) return

            val jsonBody = this.toJSON()

            val url = environment.transaction(retailerId!!).build().toString()

            AndroidNetworking.post(url)
                    .addHeaders("Content-Type", "application/json; charset=UTF-8")
                    .addHeaders("x-functions-key", retailerSecret!!)
                    .addJSONObjectBody(jsonBody)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
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

        @JvmStatic fun clientId(): String? {
            // The only thing hardcoded here is the /clientId.  This makes it so the hacker would need the actual device to read the clientId or clientSecret, which would mean we already have a security breach.
            @SuppressLint("HardwareIds") var theKey = Settings.Secure.getString(appContext?.contentResolver, Settings.Secure.ANDROID_ID)
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
        fun transaction(clientId : String): Uri.Builder {
            return this.bucketBaseUri().appendPath("transaction").appendPath(clientId)
        }
        fun closeInterval(clientId: String, intervalId : String): Uri.Builder {
            return this.bucketBaseUri().appendPath("closeInterval").appendPath(clientId).appendPath(intervalId)
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

var ANError?.bucketError : Bucket.Error?
    get() {
        if (this.isNil) return null

        val code = this!!.errorCode
        if (code == 401) return Bucket.Error.unauthorized
        else if (code == 400) {
            val json = JSONObject(this.errorBody)
            return Bucket.Error(json.getString("message"), code)
        }
        return Bucket.Error("Unknown Error", code)
    }
    private set(value) {  }
