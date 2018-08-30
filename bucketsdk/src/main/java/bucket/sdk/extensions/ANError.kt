package bucket.sdk.extensions

import bucket.sdk.models.Error
import com.androidnetworking.error.ANError
import org.json.JSONObject

var ANError?.bucketError : Error?
    get() {
        if (this.isNil) return null
        println(this!!.errorCode)
        val code = this.errorCode
        if (code == 401) return Error.unauthorized
        else if (code == 400) {
            val json = JSONObject(this.errorBody)
            val message = json.getString("message")
            return Error(message, message, code)
        } else {
            return Error(this.errorBody, this.errorDetail, this.errorCode)
        }
    }
    private set(value) {  }