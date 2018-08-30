package bucket.sdk.callbacks

import bucket.sdk.models.Error

interface BillDenomination {
    fun setBillDenoms()
    fun didError(error: Error)
}