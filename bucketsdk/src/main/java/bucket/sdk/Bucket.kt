package bucket.sdk

import android.content.Context
import android.os.Build
import bucket.sdk.callbacks.*
import bucket.sdk.extensions.*
import org.json.JSONObject
import java.util.*
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlin.collections.ArrayList

class Bucket {

    companion object {

        @JvmStatic internal var tz : TimeZone = TimeZone.getTimeZone("UTC")

        @JvmStatic var appContext : Context? = null
            set(value) {
                // Make this a regular setter:
                field = value
                if (!value.isNil) {
                    AndroidNetworking.initialize(value)
                }
            }

        @JvmStatic var environment : DeploymentEnvironment = DeploymentEnvironment.Development
        @JvmStatic private var denoms : List<Double> = listOf(100.00, 50.00, 20.00, 10.00, 5.00, 2.00, 1.00)
        @JvmStatic private var usesNaturalChangeFunction : Boolean
            get() { return Credentials.sharedPrefs?.getBoolean("USES_NATURAL_CHANGE", false) ?: false }
            set(value) {
                val editor = Credentials.sharedPrefs?.edit()
                editor?.putBoolean("USES_NATURAL_CHANGE", value)
                editor?.apply()
            }

        @JvmStatic fun bucketAmount(changeDueBack: Double): Double {
            var bucketAmount = changeDueBack
            if (usesNaturalChangeFunction) {
                // Make sure this is ordered by the amount
                val filteredDenoms = denoms.filter { it <= changeDueBack }

                // These values should already be descended from 10000 down to 100.
                filteredDenoms.forEach { denomination ->
                    bucketAmount = (bucketAmount % denomination)
                }

            } else {
                while (bucketAmount > 1.00) bucketAmount = (bucketAmount % 1.00)

            }
            return bucketAmount
        }

        @JvmStatic fun fetchBillDenominations(countryCode: String, callback: BillDenomination?) {

            val theURL = environment.billDenoms.build().toString()
            AndroidNetworking.get(theURL)
                    .addHeaders("countryId",countryCode)
                    .build().getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            // Deal with the data:
                            val denoms = response?.getJSONArray("denominations")
                            usesNaturalChangeFunction = response?.getBoolean("usesNaturalChangeFunction") ?: false
                            denoms?.let {
                                // Create our list of denominations:
                                val theDenoms : MutableList<Double> = ArrayList()
                                for (i in 0..(it.length()-1)) {
                                    theDenoms[i] = it.getDouble(i)
                                }
                                Bucket.denoms = theDenoms
                            }
                            callback?.setBillDenoms()
                        }
                        override fun onError(anError: ANError?) {
                            callback?.didError(anError.bucketError)
                        }
                    })
        }

        @JvmStatic fun registerTerminal(countryCode: String, callback: RegisterTerminal?) {

            val retailerCode = Credentials.retailerCode()

            val terminalId = Build.SERIAL

            val json = JSONObject()
            json.put("terminalId", terminalId)

            val theURL = environment.registerTerminal.build().toString()
            AndroidNetworking.post(theURL)
                    .addJSONObjectBody(json)
                    .addHeaders("countryId",countryCode)
                    .addHeaders("retailerId", retailerCode!!)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            // Deal with the data:
                            val isApproved = response?.getBoolean("isActivated") ?: false
                            val terminalSecret = response?.getString("apiKey")
                            callback?.success(isApproved)

                            // Write the terminal Api Key:
                            Credentials.setTerminalSecret(terminalSecret)
                        }
                        override fun onError(anError: ANError?) {
                            callback?.didError(anError.bucketError)
                        }
                    })
        }
    }
}