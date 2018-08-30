package bucket.sdk.callbacks

import bucket.sdk.models.Error

interface RegisterTerminal {
    fun success(isApproved: Boolean)
    fun didError(error: Error?)
}