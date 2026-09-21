package io.jadu.crisisprotect

import android.app.Application
import io.jadu.crisisprotect.di.appModules
import io.jadu.crisisprotect.background.RefreshScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.maplibre.android.MapLibre

class CrisisProtectApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
        startKoin {
            androidContext(this@CrisisProtectApplication)
            modules(appModules)
        }
        RefreshScheduler.schedule(this)
    }
}
