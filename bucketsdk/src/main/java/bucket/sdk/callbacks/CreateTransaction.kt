package bucket.sdk.callbacks

import bucket.sdk.models.Error

interface CreateTransaction {
    fun transactionCreated()
    fun didError(error: Error)
}