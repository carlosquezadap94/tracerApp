package io.bluetrace.opentrace

import android.app.Application
import android.content.Context
import android.os.Build
import android.provider.Settings
import io.bluetrace.opentrace.streetpass.bluetoothDeviceModels.CentralDevice
import io.bluetrace.opentrace.streetpass.bluetoothDeviceModels.PeripheralDevice

class TracerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AppContext = applicationContext
    }

    companion object {

        private val TAG = "TracerApp"
        const val ORG = BuildConfig.ORG

        lateinit var AppContext: Context

        fun thisDeviceMsg(): String {
            return Settings.Secure.getString (AppContext.contentResolver, Settings.Secure.ANDROID_ID)
        }

        fun asPeripheralDevice(): PeripheralDevice {
            return PeripheralDevice(Build.MODEL, "SELF")
        }

        fun asCentralDevice(): CentralDevice {
            return CentralDevice(Build.MODEL, "SELF")
        }
    }
}
