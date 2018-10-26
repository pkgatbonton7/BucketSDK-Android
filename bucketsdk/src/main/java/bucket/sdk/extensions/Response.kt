package bucket.sdk.extensions

import bucket.sdk.models.Error
import com.github.kittinunf.fuel.core.Response
import org.json.JSONObject

var Response.bucketError : Error
    get() {
        val responseString = String(this.data)
        if (!responseString.isEmpty()) {
            // Empty string
            val json = JSONObject(responseString)
            val error = Error(json.getString("message"), json.getString("errorCode"), this.statusCode)
            return error
        } else {
            return Error("Unknown API Error", "Unknown Error Code", this.statusCode)
        }
    }
    private set(value) {}