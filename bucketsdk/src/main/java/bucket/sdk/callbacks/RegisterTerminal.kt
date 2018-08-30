package bucket.sdk.callbacks

import bucket.sdk.models.Error

abstract class RegisterTerminal {
    abstract fun success(isApproved: Boolean)
    abstract fun didError(error: Error?)
}