package bucket.sdk.callbacks

import bucket.sdk.models.Error

abstract class BillDenomination {
    abstract fun setBillDenoms()
    abstract fun didError(error: Error?)
}