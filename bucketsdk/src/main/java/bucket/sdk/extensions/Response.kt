package bucket.sdk.extensions

import bucket.sdk.models.Error
import com.github.kittinunf.fuel.core.Response
import org.json.JSONObject

var Response.bucketError : Error
    get() {
        val responseString = String(this.data)
        val json = JSONObject(responseString)
        val error = Error(json.getString("message"), json.getString("errorCode"), this.statusCode)
        return error
    }
    private set(value) {}