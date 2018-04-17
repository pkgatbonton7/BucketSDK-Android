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
```
// Java:
Bucket.setAppContext(BucketApp.appContext);
if (!BuildConfig.DEBUG) {
    Bucket.setEnvironment(Bucket.DeploymentEnvironment.Production);
}

// Kotlin
if (!BuildConfig.DEBUG) {
    Bucket.environment = Bucket.DeploymentEnvironment.Production
}
Bucket.appContext = theAppContext
```
### Getting the Bucket Amount
```Java
// Java:
long bucketAmount = Bucket.bucketAmount(789);

// Kotlin:
val bucketAmount = Bucket.bucketAmount(789)
```
### Creating a transaction
```
// You will need to use the bucketAmount function to set the transaction amount here.

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

## Author
