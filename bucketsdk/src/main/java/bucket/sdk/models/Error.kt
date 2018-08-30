package bucket.sdk.models

class Error(var message: String?, var errorCode : String?, var code : Int?) {
    companion object {
        @JvmStatic val unauthorized : Error = Error("Check your retailer id & retailer secret", "Unauthorized", 401)
    }
}