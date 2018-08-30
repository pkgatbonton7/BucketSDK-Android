package bucket.sdk.callbacks

import bucket.sdk.models.Error

abstract class DeleteTransaction {
    abstract fun transactionDeleted()
    abstract fun didError(error: Error?)
}