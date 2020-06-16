package io.bluetrace.opentrace.domain.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class ModuloApp {
    @Provides
    fun provideApplicationContext(application: Application): Context {
        return application.applicationContext
    }
}