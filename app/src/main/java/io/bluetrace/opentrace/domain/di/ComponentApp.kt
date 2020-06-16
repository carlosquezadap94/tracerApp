package io.bluetrace.opentrace.domain.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import io.bluetrace.opentrace.TracerApp
import javax.inject.Singleton

@Singleton
@Component(
    modules = [AndroidInjectionModule::class,
        ModuloApp::class,
        BindsModule::class]
)
abstract class ComponentApp {
    abstract fun inject(application: TracerApp)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        fun build(): ComponentApp
    }

}