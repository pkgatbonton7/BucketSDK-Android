package bucket.sdk

import android.content.Context
import android.os.Build
import bucket.sdk.callbacks.*
import bucket.sdk.models.Error
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import org.json.JSONObject
import java.util.*

class Bucket {

    companion object {

        @JvmStatic internal var tz : TimeZone = TimeZone.getTimeZone("UTC")

        @JvmStatic var appContext : Context? = null

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
//            AndroidNetworking.get(theURL)
//                    .addHeaders("countryId",countryCode)
//                    .build().getAsJSONObject(object : JSONObjectRequestListener {
//                        override fun onResponse(response: JSONObject?) {
//                            // Deal with the data:
//                            val denoms = response?.getJSONArray("denominations")
//                            usesNaturalChangeFunction = response?.getBoolean("usesNaturalChangeFunction") ?: false
//                            denoms?.let {
//                                // Create our list of denominations:
//                                val theDenoms : MutableList<Double> = ArrayList()
//                                for (i in 0..(it.length()-1)) {
//                                    theDenoms[i] = it.getDouble(i)
//                                }
//                                Bucket.denoms = theDenoms
//                            }
//                            callback?.setBillDenoms()
//                        }
//                        override fun onError(anError: ANError?) {
//                            callback?.didError(anError.bucketError)
//                        }
//                    })
        }

        @JvmStatic fun registerTerminal(countryCode: String, callback: RegisterTerminal?) {

            val retailerCode = Credentials.retailerCode()

            val terminalId = Build.SERIAL

            val json = JSONObject()
            json.put("terminalId", terminalId)

            val theURL = environment.registerTerminal.build().toString()

            theURL.httpPost().body(json.toString()).header(Pair("retailerId",retailerCode!!)).header(Pair("countryId", countryCode)).responseJson { request, response, result ->
                when (result) {
                    is Result.Failure -> {
                        val code = response.statusCode
                        val responseString = String(response.data)
                        val responseJSON = JSONObject(responseString)

                        val error = Error(responseJSON.getString("message"), responseJSON.getString("errorCode"), code)
                        callback?.didError(error)

                    }
                    is Result.Success -> {
                        val json = result.value.obj()
                        val apiKey = json.getString("apiKey")
                        val isApproved = json.getBoolean("isApproved")

                        callback?.success(isApproved)
                        // Set the terminal secret:
                        Credentials.setTerminalSecret(apiKey)
                    }
                }
            }
        }
    }
}