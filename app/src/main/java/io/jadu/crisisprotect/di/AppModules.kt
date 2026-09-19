package io.jadu.crisisprotect.di

import androidx.room.Room
import io.jadu.crisisprotect.data.local.CrisisProtectDatabase
import io.jadu.crisisprotect.data.remote.UsgsEarthquakeService
import io.jadu.crisisprotect.data.repository.OfflineFirstDisasterRepository
import io.jadu.crisisprotect.domain.repository.DisasterRepository
import io.jadu.crisisprotect.feature.details.DetailsViewModel
import io.jadu.crisisprotect.feature.home.HomeViewModel
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

private const val UsgsBaseUrl = "https://earthquake.usgs.gov/"

val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
    single {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .callTimeout(20, TimeUnit.SECONDS)
            .build()
    }
    single {
        Retrofit.Builder()
            .baseUrl(UsgsBaseUrl)
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }
    single<UsgsEarthquakeService> { get<Retrofit>().create(UsgsEarthquakeService::class.java) }
}

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            CrisisProtectDatabase::class.java,
            "crisis_protect.db",
        ).build()
    }
    single { get<CrisisProtectDatabase>().disasterEventDao() }
}

val repositoryModule = module {
    single<DisasterRepository> { OfflineFirstDisasterRepository(get(), get()) }
}

val viewModelModule = module {
    viewModel { HomeViewModel(get()) }
    viewModel { (eventId: String) -> DetailsViewModel(eventId, get()) }
}

val appModules = listOf(networkModule, databaseModule, repositoryModule, viewModelModule)
