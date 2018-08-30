package bucket.sdk


import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
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
    @Test
    fun useAppContext() {
        // Context of the app under test.

        val appContext = InstrumentationRegistry.getTargetContext()

        assertEquals("bucket.sdk", appContext.packageName)
    }

    @Test
    fun registerTerminal() {


    }

//    fun testCreatingTransaction() {
//        Bucket.appContext = InstrumentationRegistry.getTargetContext()
//
//        // Get the client id & client secret for this retailer:
//        val retailerId = "6644211a-c02a-4413-b307-04a11b16e6a4"
//        val retailerSecret = "9IlwMxfQLaOvC4R64GdX/xabpvAA4QBpqb1t8lJ7PTGeR4daLI/bxw=="
//
//        val trans = Transaction(0, "ll", 700)
//
//        val jsonBody = trans.toJSON()
//
//        val url = Bucket.environment.transaction(retailerId).build().toString()
//
//        val build = AndroidNetworking.post(url)
//                .setContentType("application/json; charset=UTF-8")
//                .addHeaders("x-functions-key", retailerSecret)
//                .addJSONObjectBody(jsonBody)
//                .build()
//
//        build.getAsJSONObject(object : JSONObjectRequestListener {
//            override fun onResponse(response: JSONObject?) {
//
//            }
//            override fun onError(anError: ANError?) {
//                assert(false)
//                if (anError.isNil) {
//                    println("NULL ERROR")
//                } else if (anError.bucketError.isNil) {
//                    println("BUCKET ERROR NULL")
//                }
//
//            }
//        })
//
////        val jsonBody = JSONObject()
////
////        try {
////            jsonBody.put("amount", 0)
////            jsonBody.put("terminalId", "C030UQ71150503")
////            jsonBody.put("totalTransactionAmount", 700)
////            jsonBody.put("intervalId", 20180717)
////            jsonBody.put("clientTransactionId", "llll")
////        } catch (e: JSONException) {
////            e.printStackTrace()
////        }
////
////        val url = Bucket.environment.transaction(retailerId).build().toString()
////
////        AndroidNetworking.post(url)
////                .setContentType("application/json; charset=UTF-8")
////                .addHeaders("x-functions-key", retailerSecret)
////                .addJSONObjectBody(jsonBody)
////                .build()
////                .getAsJSONObject(object : JSONObjectRequestListener {
////                    override fun onResponse(response: JSONObject) {
////
////                    }
////
////                    override fun onError(anError: ANError?) {
////
////                    }
////                })
//    }
//
//    @Test
//    fun testCreateTransaction() {
//
//        val appContext = InstrumentationRegistry.getTargetContext()
//        Bucket.appContext = appContext
//        val trans = Bucket.Transaction(0, "lll", 700)
//        trans.create(object : Bucket.Callbacks.CreateTransaction() {
//            override fun transactionCreated() {
//                Log.e("", "")
//            }
//
//            override fun didError(error: Bucket.Error?) {
//                Log.e("", "")
//            }
//        })
//    }
//
//    @Test
//    fun testRegisterDevice() {
//        Bucket.appContext = InstrumentationRegistry.getTargetContext()
//
//        Bucket.Credentials.setRetailerId("6644211a-c02a-4413-b307-04a11b16e6a4")
//
//        Bucket.registerDevice(object : Bucket.Callbacks.RegisterTerminal() {
//            override fun deviceIsApproved() {
//                assertTrue(true)
//            }
//
//            override fun deviceWasRegistered() {
//                assertTrue(true)
//            }
//
//            override fun didError(error: Bucket.Error?) {
//                if (error != null) {
//                    Log.d("Error", error.message)
//                }
//
//                assertFalse(false)
//            }
//        })
//
//    }
//
//    //    @Test
//    //    public void testSettingClientSecret() {
//    //        Bucket.setAppContext(InstrumentationRegistry.getTargetContext());
//    //
//    //        String retailerSecret = "RandomClientSecret";
//    //        Bucket.Credentials.setClientSecret(retailerSecret);
//    //        assertEquals(retailerSecret, Bucket.Credentials.clientSecret());
//    //    }
//
//    //    @Test
//    //    public void testCreateTransaction() {
//    //
//    //        Bucket.setAppContext(InstrumentationRegistry.getTargetContext());
//    //
//    //        Bucket.Credentials.setClientSecret("9IlwMxfQLaOvC4R64GdX/xabpvAA4QBpqb1t8lJ7PTGeR4daLI/bxw==");
//    //        Bucket.Credentials.setClientId("6644211a-c02a-4413-b307-04a11b16e6a4");
//    //
//    //        Bucket.Transaction trans = new Bucket.Transaction(78, "MyClientTransId");
//    //
//    //        Log.d("BUCKET",String.valueOf(Bucket.getEnvironment()));
//    //
//    //        trans.create(new Bucket.Callbacks.CreateTransaction() {
//    //            @Override public void transactionCreated() {
//    //                assertTrue(true);
//    //            }
//    //            @Override public void didError(@Nullable Bucket.Error error) {
//    //
//    //                fail();
//    //            }
//    //        });
//    //    }
//
//>>>>>>> 97a596d3ba9b58956bb97ae32dcb24d511b568d4
}
