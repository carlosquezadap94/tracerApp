package io.bluetrace.opentrace.domain.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.bluetrace.opentrace.ui.activity.IntroActivity

@Module
abstract class BindsModule {
    @ContributesAndroidInjector
    abstract fun bindActivity(): IntroActivity
}