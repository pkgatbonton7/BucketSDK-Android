package bucket.sdk.preferences

import com.chibatching.kotpref.KotprefModel

object BucketPreferences : KotprefModel(){
    var retailerCode by nullableStringPref()
    var terminalSecret by nullableStringPref()
    var retailerCountry by nullableStringPref()
    var usesNaturalChange by booleanPref(false)
}