package bucket.sdk.models

// Bucket Packages:
import android.os.Build
import bucket.sdk.*
import bucket.sdk.annotations.*
import bucket.sdk.callbacks.*
import bucket.sdk.extensions.*

import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONObject
import java.net.URL

class Transaction(var amount: Double, var totalTransactionAmount : Double, var clientTransactionId: String) {

    // This is the primary key for the transaction in our db, as annotated:
    @PrimaryKey
    var bucketTransactionId : String? = null
    var customerCode                    : String?  = null
    var qrCodeContent                   : URL? = null

    // The rest of these fields are specified by the retailer:

    var locationId                      : String? = null

    private fun updateWith(updateJSON: JSONObject?) {

        if (updateJSON.isNil) return

        this.customerCode = updateJSON!!.getString("customerCode")
        this.bucketTransactionId = updateJSON.getString("bucketTransactionId")
        this.qrCodeContent = updateJSON.getURL("qrCodeContent")
        this.totalTransactionAmount = updateJSON.getDouble("totalTransactionAmount")
    }

    internal fun toJSON(): JSONObject {

        val obj = JSONObject()

        // We will always set the amount & clientTransactionId when sending the JSON:
        obj.put("amount", amount)
        obj.put("clientTransactionId", clientTransactionId)
        obj.put("totalTransactionAmount", totalTransactionAmount)

        // The fields that may not be set:
        if (!locationId.isNil) { obj.put("locationId", locationId!!) }
        if (!customerCode.isNil) { obj.put("customerCode", customerCode!!) }
        if (!qrCodeContent.isNil) { obj.put("qrCodeContent", qrCodeContent!!) }


        return obj
    }

    fun delete(customerCode: String, countryCode : String, callback: DeleteTransaction?) {

        // Get the client id & client secret for this retailer:
        val retailerCode = Credentials.retailerCode()
        val terminalSecret = Credentials.terminalSecret()

        var shouldIReturn = false
        if (retailerCode.isNullOrEmpty() || terminalSecret.isNullOrEmpty()) {
            shouldIReturn = true
            callback?.didError(Error.unauthorized)
        }

        if (shouldIReturn) return

        val jsonBody = this.toJSON()

        val url = Bucket.environment.transaction.appendPath(customerCode).build().toString()

        val build = AndroidNetworking.delete(url)
                .setContentType("application/json; charset=UTF-8")
                .addHeaders("x-functions-key", terminalSecret!!)
                .addHeaders("retailerId", retailerCode!!)
                .addHeaders("countryId", countryCode)
                .addHeaders("terminalId", Build.SERIAL)
                .addJSONObjectBody(jsonBody)
                .build()

        build.getAsJSONObject(object : JSONObjectRequestListener {
            override fun onResponse(response: JSONObject?) {
                this@Transaction.updateWith(response)
                callback?.transactionDeleted()
            }
            override fun onError(anError: ANError?) {
                callback?.didError(anError?.bucketError)
            }
        })
    }

    fun create(countryCode : String, callback: CreateTransaction?) {

        // Get the client id & client secret for this retailer:
        val retailerCode = Credentials.retailerCode()
        val terminalSecret = Credentials.terminalSecret()

        var shouldIReturn = false
        if (retailerCode.isNullOrEmpty() || terminalSecret.isNullOrEmpty()) {
            shouldIReturn = true
            callback?.didError(Error.unauthorized)
        }

        if (shouldIReturn) return

        val jsonBody = this.toJSON()

        val url = Bucket.environment.transaction.build().toString()

        val build = AndroidNetworking.post(url)
                .setContentType("application/json; charset=UTF-8")
                .addHeaders("x-functions-key", terminalSecret!!)
                .addHeaders("retailerId", retailerCode)
                .addHeaders("terminalId", Build.SERIAL)
                .addHeaders("countryId", countryCode)
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