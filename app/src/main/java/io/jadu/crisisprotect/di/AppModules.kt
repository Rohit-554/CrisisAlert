package io.jadu.crisisprotect.di

import io.jadu.crisisprotect.data.di.dataModules
import io.jadu.crisisprotect.feature.details.AndroidEventHapticController
import io.jadu.crisisprotect.feature.details.DetailsViewModel
import io.jadu.crisisprotect.feature.details.EventHapticController
import io.jadu.crisisprotect.feature.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

private val presentationModule = module {
    single<EventHapticController> { AndroidEventHapticController(androidContext()) }
    viewModel { HomeViewModel(get()) }
    viewModel { (eventId: String) -> DetailsViewModel(eventId, get(), get(), get()) }
}

/** The composition root: features and data never depend on the application module. */
val appModules = dataModules + presentationModule
