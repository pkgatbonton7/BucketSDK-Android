package bucket.sdk.models

class Error(var message: String?, var detail : String?, var code : Int?) {
    companion object {
        @JvmStatic val unauthorized : Error = Error("Unauthorized", "Check your retailer id & retailer secret", 401)
        @JvmStatic val unsupportedMethod : Error = Error("Unsupported API function.", "THE_METHOD", null)
    }
}