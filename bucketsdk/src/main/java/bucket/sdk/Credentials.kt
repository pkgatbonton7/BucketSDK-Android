package bucket.sdk

import android.content.Context
import bucket.sdk.extensions.isNil
import bucket.sdk.preferences.BucketPreferences

object Credentials {
//    @JvmStatic val sharedPrefs = Bucket.appContext?.getSharedPreferences("SHAREDPREFS", Context.MODE_PRIVATE)

    @JvmStatic fun retailerCode(): String? {
        // The only thing hardcoded here is the /clientId.  This makes it so the hacker would need the actual device to read the clientId or clientSecret, which would mean we already have a security breach.
//        return sharedPrefs?.getString("RETAILER_CODE", null)
        return BucketPreferences.retailerCode
    }
    @JvmStatic fun setRetailerCode(value: String?) {
        // The only thing hardcoded here is the /clientId.  This makes it so the hacker would need the actual device to read the clientId or clientSecret, which would mean we already have a security breach.
//        val editor = sharedPrefs?.edit()
//        if (value.isNil) { editor?.remove("RETAILER_CODE") }
//        else {
//            editor?.putString("RETAILER_CODE", value)
//        }
//        editor?.apply()
        BucketPreferences.retailerCode = value
    }
    @JvmStatic fun terminalSecret(): String? {
        // The only thing hardcoded here is the /clientSecret.  This makes it so the hacker would need the actual device to read the clientId or clientSecret, which would mean we already have a security breach.
//        return sharedPrefs?.getString("TERMINAL_SECRET", null)
        return BucketPreferences.terminalSecret
    }
    @JvmStatic fun setTerminalSecret(value: String?) {
        // The only thing hardcoded here is the /clientSecret.  This makes it so the hacker would need the actual device to read the clientId or clientSecret, which would mean we already have a security breach.
//        val editor = sharedPrefs?.edit()
//        if (value.isNil) { editor?.remove("TERMINAL_SECRET") }
//        else {
//            editor?.putString("TERMINAL_SECRET", value)
//        }
//        editor?.apply()
        BucketPreferences.terminalSecret = value
    }
    @JvmStatic fun countryCode(): String? {
//        return sharedPrefs?.getString("RETAILER_COUNTRY", null)
        return BucketPreferences.retailerCountry
    }
    @JvmStatic fun setCountryCode(value : String?) {
//        val editor = sharedPrefs?.edit()
//        if (value.isNil) { editor?.remove("RETAILER_COUNTRY") }
//        else {
//            editor?.putString("RETAILER_COUNTRY", value)
//        }
//        editor?.apply()
        BucketPreferences.retailerCountry = value
    }

}