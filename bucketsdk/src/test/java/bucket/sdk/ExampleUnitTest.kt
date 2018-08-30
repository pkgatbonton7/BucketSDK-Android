package bucket.sdk

import bucket.sdk.callbacks.CreateTransaction
import bucket.sdk.models.Error
import bucket.sdk.models.Transaction
import org.json.JSONObject
import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, (2 + 2).toLong())
    }

    @Test
    fun testDictionaryTHing() {
        var json = JSONObject()
//        json["amount"] = 9.02

        println(json["amount"])

    }

    @Test
    fun createTransaction() {

        val transaction = Transaction(0.54, 7.46,"ClientTransactionId")
        transaction.create(object : CreateTransaction() {
            override fun transactionCreated() {
                assert(true)
            }
            override fun didError(error: Error?) {
                assert(false)
            }
        })
    }
}