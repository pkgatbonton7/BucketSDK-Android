package bucket.sdk

import android.content.Context
import bucket.sdk.callbacks.*
import bucket.sdk.extensions.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlin.collections.ArrayList

class Bucket {

    companion object {

        @JvmStatic internal var tz : TimeZone = TimeZone.getTimeZone("UTC")
        @JvmStatic internal var df : SimpleDateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
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

            // TODO:  We need an endpoint to return the natural change denominations for the country.

//            var theURL = environment.billDenoms(retailerId,)
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
                                        val theDenoms:MutableList<Double> = ArrayList()
                                        for (j in 0..(theds.length()-1)) {
                                            theDenoms.add(j, theds.getDouble(j))
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
}