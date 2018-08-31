package bucket.sdk

import android.net.Uri

enum class DeploymentEnvironment {

    // Cases: (Production & Development for now)
    Production, Development, Staging;

    // Case URL Endpoint:
    private fun bucketBaseUri(): Uri.Builder {
        val builder = Uri.Builder()
        builder.scheme("https")
        when (this) {
            Production -> builder.authority("prod.bucketthechange.com")
            Development -> builder.authority("dev.bucketthechange.com")
            Staging -> builder.authority("staging.bucketthechange.com")
        }
        builder.appendPath("api")
        builder.appendPath("v1")
        return builder
    }

    // PRE-BUILT ENDPOINT PATHS:
    var transaction : Uri.Builder
        get() { return bucketBaseUri().appendPath("transaction") }
        private set(value) {}

    var registerTerminal : Uri.Builder
        get() { return bucketBaseUri().appendPath("registerterminal") }
        private set(value) {}

    var billDenoms : Uri.Builder
        get() { return bucketBaseUri().appendPath("billDenoms") }
        private set(value) {}

}