package bucket.sdk.extensions

import org.json.JSONObject
import java.net.URL

fun JSONObject?.getURL(name : String): URL? {
    if (this.isNil) return null
    val stringVal = this!!.getString(name)
    return URL(stringVal)
}