package com.bucket.bucketsdk

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.Settings
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import de.adorsys.android.securestoragelibrary.SecurePreferences
import org.json.JSONObject

class Bucket {
    companion object {
        @JvmStatic var appContext : Context? = null
        @JvmStatic var environment : Bucket.DeploymentEnvironment = Bucket.DeploymentEnvironment.development
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
    abstract class RetailerLogin {
        abstract fun didLogIn()
        abstract fun didError(error: VolleyError?)
    }
    class Retailer {
        companion object {
            @JvmStatic fun logInWith(password: String, username: String, callback: Bucket.RetailerLogin?): JsonObjectRequest {

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

    object Credentials {

        fun clientId(): String? {
            // The only thing hardcoded here is the /clientId.  This makes it so the hacker would need the actual device to read the clientId or clientSecret, which would mean we already have a security breach.
            @SuppressLint("HardwareIds") var theKey = Settings.Secure.getString(Bucket.appContext?.getContentResolver(), Settings.Secure.ANDROID_ID)
            theKey += "/clientId"
            return SecurePreferences.getStringValue(theKey, null)

        }

        fun setClientId(value: String) {
            // The only thing hardcoded here is the /clientId.  This makes it so the hacker would need the actual device to read the clientId or clientSecret, which would mean we already have a security breach.
            @SuppressLint("HardwareIds") var theKey = Settings.Secure.getString(appContext?.getContentResolver(), Settings.Secure.ANDROID_ID)
            theKey += "/clientId"
            SecurePreferences.setValue(theKey, value)
        }

        fun clientSecret(): String? {
            // The only thing hardcoded here is the /clientSecret.  This makes it so the hacker would need the actual device to read the clientId or clientSecret, which would mean we already have a security breach.
            @SuppressLint("HardwareIds") var theKey = Settings.Secure.getString(appContext?.getContentResolver(), Settings.Secure.ANDROID_ID)
            theKey += "/clientSecret"
            return SecurePreferences.getStringValue(theKey, null)
        }

        fun setClientSecret(value: String) {
            // The only thing hardcoded here is the /clientSecret.  This makes it so the hacker would need the actual device to read the clientId or clientSecret, which would mean we already have a security breach.
            @SuppressLint("HardwareIds") var theKey = Settings.Secure.getString(appContext?.getContentResolver(), Settings.Secure.ANDROID_ID)
            theKey += "/clientSecret"
            SecurePreferences.setValue(theKey, value)
        }

    }

    enum class DeploymentEnvironment {

        // Cases: (Production & Development for now)
        production, development;

        // Case URL Endpoint:
        fun bucketBaseUri(): Uri.Builder {
            val builder = Uri.Builder()
            builder.scheme("https")
            when (this) {
                production -> builder.authority(appContext?.getString(R.string.prodEndpoint))
                development -> builder.authority(appContext?.getString(R.string.devEndpoint))
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
            return this.bucketBaseUri().appendPath("login")
        }

    }
}