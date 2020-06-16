package io.bluetrace.opentrace

import android.app.Application
import android.content.Context
import android.os.Build
import android.provider.Settings
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.bluetrace.opentrace.domain.di.DaggerComponentApp
import io.bluetrace.opentrace.streetpass.bluetoothDeviceModels.CentralDevice
import io.bluetrace.opentrace.streetpass.bluetoothDeviceModels.PeripheralDevice
import javax.inject.Inject

class TracerApp : Application(), HasAndroidInjector {

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun onCreate() {
        super.onCreate()
        AppContext = applicationContext


        DaggerComponentApp.builder().application(this)
            .build().inject(this)
    }

    companion object {
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

    override fun androidInjector() = androidInjector
}
