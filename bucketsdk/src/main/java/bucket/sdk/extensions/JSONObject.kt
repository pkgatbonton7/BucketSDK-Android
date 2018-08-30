package bucket.sdk.extensions

import org.json.JSONObject
import java.net.URL

internal fun JSONObject?.getURL(name : String): URL? {
    if (this.isNil) return null
    val stringVal = this!!.getString(name)
    return URL(stringVal)
}

internal operator fun JSONObject.set(s: String, value: String?) {
    if (value.isNil) {
        this.remove(s)
    } else {
        this.put(s,value)
    }
}

internal operator fun JSONObject.set(s: String, value: Double?) {
    if (value.isNil) {
        this.remove(s)
    } else {
        this.put(s,value)
    }
}

internal operator fun JSONObject.set(s: String, value: Int?) {
    if (value.isNil) {
        this.remove(s)
    } else {
        this.put(s,value)
    }
}

internal operator fun JSONObject.set(s: String, value: URL?) {
    if (value.isNil) {
        this.remove(s)
    } else {
        this.put(s,value)
    }
}