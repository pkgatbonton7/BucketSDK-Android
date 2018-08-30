package bucket.sdk.callbacks

import bucket.sdk.models.Error

abstract class CreateTransaction {
    abstract fun transactionCreated()
    abstract fun didError(error: Error?)
}