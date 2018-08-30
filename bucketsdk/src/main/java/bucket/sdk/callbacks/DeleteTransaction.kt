package bucket.sdk.callbacks

import bucket.sdk.models.Error

interface DeleteTransaction {
    fun transactionDeleted()
    fun didError(error: Error)
}