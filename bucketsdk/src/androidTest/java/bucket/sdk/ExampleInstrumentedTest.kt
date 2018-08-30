package bucket.sdk


import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import bucket.sdk.callbacks.BillDenomination
import bucket.sdk.callbacks.CreateTransaction
import bucket.sdk.callbacks.DeleteTransaction
import bucket.sdk.callbacks.RegisterTerminal
import bucket.sdk.models.Error
import bucket.sdk.models.Transaction
import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test fun useAppContext() {
        // Context of the app under test.

        val appContext = InstrumentationRegistry.getTargetContext()

        assertEquals("bucket.sdk", appContext.packageName)
    }

    @Test fun testRegisteringDevice() {

        Bucket.appContext = InstrumentationRegistry.getTargetContext()
        Credentials.setRetailerCode("BCKT-1")
        // Get the client id & client secret for this retailer:
        Bucket.registerTerminal("us", object : RegisterTerminal {
            override fun success(isApproved: Boolean) {
                assert(true)
            }
            override fun didError(error: Error?) {
                assertTrue(error?.message ?: "", false)
            }
        })
        Thread.sleep(5000)
    }

    @Test fun testCreateTransaction() {

        Bucket.appContext = InstrumentationRegistry.getTargetContext()

        val transaction = Transaction(0.54, 7.89, "RandomTransactionId")
        transaction.create("us", object : CreateTransaction {
            override fun transactionCreated() {
                assert(true)
            }

            override fun didError(error: Error?) {
                assertTrue(error?.message ?: "", false)
            }
        })
        Thread.sleep(5000)
    }

    @Test fun testDeleteTransaction() {

        Bucket.appContext = InstrumentationRegistry.getTargetContext()

        val transaction = Transaction(0.54, 7.89, "RandomTransactionId")
        transaction.customerCode = "us.eDZ9LBdvununS"

        transaction.delete("us", object : DeleteTransaction {
            override fun transactionDeleted() {
                assert(true)
            }
            override fun didError(error: Error?) {
                assertTrue(error?.message ?: "", false)
            }
        })

        Thread.sleep(5000)
    }

    @Test fun fetchBillDenoms() {

        Bucket.appContext = InstrumentationRegistry.getTargetContext()

        Bucket.fetchBillDenominations("us", object : BillDenomination {
            override fun setBillDenoms() {
                assertTrue(true)
            }
            override fun didError(error: Error?) {
                assertTrue(error?.message ?: "", false)
            }
        })

        Thread.sleep(5000)
    }

    @Test fun bucketAmount() {



    }
}
