[![](https://jitpack.io/v/buckettech/BucketSDK-Android.svg)](https://jitpack.io/#buckettech/BucketSDK-Android)

# BucketSDK-Android
This is the BucketSDK for Android.  Here is where you can create transactions & other important features of the Bucket API pre-wrapped for Android.

## Requirements
You will need to be registered with Bucket & have obtained a Retailer Account.  You will need to have received a clientId & clientSecret in order to access Bucket's API.

## Installation

BucketSDK is available through [JitPack](https://jitpack.io). To install
it, simply add the following line to your gradle file:

```gradle
implementation "com.github.buckettech:BucketSDK-Android:$bucketSDKVersion"
```

## Usage
Using the BucketSDK, you will be able to use either Java or Kotlin to access the functions that you will need.
### Setting the app context & app environment
```Java
// Java:
Bucket.setAppContext(BucketApp.appContext);
if (!BuildConfig.DEBUG) {
    Bucket.setEnvironment(Bucket.DeploymentEnvironment.Production);
}
```
```Kotlin
// Kotlin
if (!BuildConfig.DEBUG) {
    Bucket.environment = Bucket.DeploymentEnvironment.Production
}
Bucket.appContext = theAppContext
```

### Setting & retrieving your retailer id & retailer secret:
````Java
// Java:
// Setter:
Bucket.Credentials.setRetailerId("RetailerId");
Bucket.Credentials.setRetailerSecret("RetailerSecret");

// Getter:
String retailerId = Bucket.Credentials.retailerId();
String retailerSecret = Bucket.Credentials.retailerSecret();
````

```kotlin
// Kotlin:
// Setter:
Bucket.Credentials.setRetailerId("RetailerId")
Bucket.Credentials.setRetailerSecret("RetailerSecret")

// Getter:
val retailerId = Bucket.Credentials.retailerId()
val retailerSecret = Bucket.Credentials.retailerSecret()
```

### Setting your currency code:
SGD (Singapore) & USD (USA) currencies are currently supported.
```Java
// Java:
Bucket.fetchBillDenominations("USD", new Bucket.Callbacks.BillDenomination() {
    @Override public void setBillDenoms() {
        
    }
    @Override public void didError(Bucket.Error error) {
        
    }
});
```

```kotlin
// Kotlin:
Bucket.fetchBillDenominations("USD", object : Bucket.Callbacks.BillDenomination() {
    override fun setBillDenoms() {
    
    }
    override fun didError(error: Bucket.Error?) {
                        
    }
})
```

### Getting the Bucket Amount
```Java
// Java:
long bucketAmount = Bucket.bucketAmount(789);

// Kotlin:
val bucketAmount = Bucket.bucketAmount(789)
```
### Creating a transaction
You will need to use the bucketAmount function to set the transaction amount here.
```Java
// Java:
Bucket.Transaction tr = new Bucket.Transaction(90, "XCFRTDSFGGOL");
tr.create(new Bucket.Callbacks.CreateTransaction() {
    @Override public void transactionCreated() {
        // The transaction was successfully created!
    }
    @Override public void didError(VolleyError volleyError) {
        // There was an error.
    }
});
```
```Kotlin
// Kotlin:
val transaction = Bucket.Transaction(789, "XCFRTDSFGGOL")
transaction.create(object : Bucket.Callbacks.CreateTransaction() {
    override fun transactionCreated() {
        // Yay the transaction was successfully created.
    }
    override fun didError(error: VolleyError?) {
    // Oh no - we had an error :(
    }
})
```

### Closing the start-to-end of day
```Java
// Java:
Bucket.close(interval, new Bucket.Callbacks.CloseInterval() {
    @Override public void closedInterval(String intervalId) {
        // The interval has been closed!
    }
    @Override public void didError(VolleyError volleyError) {
        // There was an error.
    }
});
```

```kotlin
// Kotlin:
Bucket.close(interval, object : Bucket.Callbacks.CloseInterval() {
    override fun closedInterval(intervalId: String) {
        // The interval has been closed!
    }
    override fun didError(error: Bucket.Error?) {
        //TODO: Handle the error:
    }
})
```

## Author
