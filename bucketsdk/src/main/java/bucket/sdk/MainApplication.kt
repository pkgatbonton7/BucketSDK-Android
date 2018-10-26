package bucket.sdk

import android.app.Application
import com.chibatching.kotpref.Kotpref

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Kotpref.init(this)
    }
}