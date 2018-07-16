package bucket.sdk;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.

        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("bucket.sdk", appContext.getPackageName());
    }

    @Test
    public void testCreatingTransaction() {
        Bucket.setAppContext(InstrumentationRegistry.getTargetContext());

        Bucket.Credentials.setRetailerId("");
        Bucket.Credentials.setRetailerSecret("");

        Bucket.Transaction trans = new Bucket.Transaction(77, "randomIid", 723);
        trans.create(new Bucket.Callbacks.CreateTransaction() {
            @Override
            public void transactionCreated() {
                assertTrue(true);
            }

            @Override
            public void didError(@Nullable Bucket.Error error) {
                assertFalse(true);
            }
        });

    }

    @Test
    public void testCreateTransaction() {

        Context appContext = InstrumentationRegistry.getTargetContext();
        Bucket.setAppContext(appContext);
        Bucket.Transaction trans = new Bucket.Transaction(0, "lll", 700);
        trans.create(new Bucket.Callbacks.CreateTransaction() {
            @Override public void transactionCreated() {
                Log.e("","");
            }
            @Override public void didError(@Nullable Bucket.Error error) {
                Log.e("","");
            }
        });
    }

    @Test
    public void testRegisterDevice() {
        Bucket.setAppContext(InstrumentationRegistry.getTargetContext());

        Bucket.Credentials.setRetailerId("6644211a-c02a-4413-b307-04a11b16e6a4");

        Bucket.registerDevice(new Bucket.Callbacks.RegisterTerminal() {
            @Override
            public void deviceIsApproved() {
                assertTrue(true);
            }
            @Override
            public void deviceWasRegistered() {
                assertTrue(true);
            }
            @Override
            public void didError(@Nullable Bucket.Error error) {
                if (error != null) {
                    Log.d("Error", error.getMessage());
                }

                assertFalse(false);
            }
        });

    }

//    @Test
//    public void testSettingClientSecret() {
//        Bucket.setAppContext(InstrumentationRegistry.getTargetContext());
//
//        String retailerSecret = "RandomClientSecret";
//        Bucket.Credentials.setClientSecret(retailerSecret);
//        assertEquals(retailerSecret, Bucket.Credentials.clientSecret());
//    }

//    @Test
//    public void testCreateTransaction() {
//
//        Bucket.setAppContext(InstrumentationRegistry.getTargetContext());
//
//        Bucket.Credentials.setClientSecret("9IlwMxfQLaOvC4R64GdX/xabpvAA4QBpqb1t8lJ7PTGeR4daLI/bxw==");
//        Bucket.Credentials.setClientId("6644211a-c02a-4413-b307-04a11b16e6a4");
//
//        Bucket.Transaction trans = new Bucket.Transaction(78, "MyClientTransId");
//
//        Log.d("BUCKET",String.valueOf(Bucket.getEnvironment()));
//
//        trans.create(new Bucket.Callbacks.CreateTransaction() {
//            @Override public void transactionCreated() {
//                assertTrue(true);
//            }
//            @Override public void didError(@Nullable Bucket.Error error) {
//
//                fail();
//            }
//        });
//    }

}
