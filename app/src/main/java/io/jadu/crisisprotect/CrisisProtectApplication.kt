package io.jadu.crisisprotect

import android.app.Application
import io.jadu.crisisprotect.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CrisisProtectApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CrisisProtectApplication)
            modules(appModules)
        }
    }
}
