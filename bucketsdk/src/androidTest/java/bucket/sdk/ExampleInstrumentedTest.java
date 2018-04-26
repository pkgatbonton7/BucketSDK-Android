package bucket.sdk;

import android.content.Context;
import android.net.Uri;
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
    public void testSettingClientId() {
        Bucket.setAppContext(InstrumentationRegistry.getTargetContext());

        String retailerId = "RandomRetailerId";
        Bucket.Credentials.setClientId(retailerId);
        assertEquals(retailerId, Bucket.Credentials.clientId());

    }

    @Test
    public void testSettingClientSecret() {
        Bucket.setAppContext(InstrumentationRegistry.getTargetContext());

        String retailerSecret = "RandomClientSecret";
        Bucket.Credentials.setClientSecret(retailerSecret);
        assertEquals(retailerSecret, Bucket.Credentials.clientSecret());
    }

//    @Test public void testCreateTransaction() {
//
//        Bucket.setAppContext(InstrumentationRegistry.getTargetContext());
//        Bucket.Transaction trans = new Bucket.Transaction(78, "MyClientTransId");
//
//        trans.create(new Bucket.Callbacks.CreateTransaction() {
//            @Override public void transactionCreated() {
//                assertTrue(true);
//            }
//            @Override public void didError(@Nullable Bucket.Error error) {
//                if (error != null) {
//                    Log.d("Error", String.valueOf(error.getCode()));
//                }
//                fail();
//            }
//        });
//    }

}
