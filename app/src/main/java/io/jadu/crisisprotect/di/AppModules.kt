package io.jadu.crisisprotect.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.jadu.crisisprotect.data.local.CrisisProtectDatabase
import io.jadu.crisisprotect.data.remote.UsgsEarthquakeService
import io.jadu.crisisprotect.data.remote.EonetEventService
import io.jadu.crisisprotect.data.remote.WeatherService
import io.jadu.crisisprotect.data.repository.OfflineFirstDisasterRepository
import io.jadu.crisisprotect.data.repository.OpenMeteoWeatherRepository
import io.jadu.crisisprotect.domain.repository.DisasterRepository
import io.jadu.crisisprotect.domain.repository.WeatherRepository
import io.jadu.crisisprotect.feature.details.DetailsViewModel
import io.jadu.crisisprotect.feature.home.HomeViewModel
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

private const val UsgsBaseUrl = "https://earthquake.usgs.gov/"
private const val EonetBaseUrl = "https://eonet.gsfc.nasa.gov/"
private const val OpenMeteoBaseUrl = "https://api.open-meteo.com/"

private val DatabaseMigration1To2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE disaster_events ADD COLUMN description TEXT")
        database.execSQL("ALTER TABLE disaster_events ADD COLUMN upstreamSource TEXT")
        database.execSQL("ALTER TABLE disaster_events ADD COLUMN upstreamSourceUrl TEXT")
        database.execSQL("ALTER TABLE disaster_events ADD COLUMN isSaved INTEGER NOT NULL DEFAULT 0")
    }
}

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
    single(named("usgsRetrofit")) {
        Retrofit.Builder()
            .baseUrl(UsgsBaseUrl)
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }
    single<UsgsEarthquakeService> { get<Retrofit>(named("usgsRetrofit")).create(UsgsEarthquakeService::class.java) }
    single(named("eonetRetrofit")) { Retrofit.Builder().baseUrl(EonetBaseUrl).client(get()).addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType())).build() }
    single<EonetEventService> { get<Retrofit>(named("eonetRetrofit")).create(EonetEventService::class.java) }
    single(named("weatherRetrofit")) { Retrofit.Builder().baseUrl(OpenMeteoBaseUrl).client(get()).addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType())).build() }
    single<WeatherService> { get<Retrofit>(named("weatherRetrofit")).create(WeatherService::class.java) }
}

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            CrisisProtectDatabase::class.java,
            "crisis_protect.db",
        ).addMigrations(DatabaseMigration1To2).build()
    }
    single { get<CrisisProtectDatabase>().disasterEventDao() }
}

val repositoryModule = module {
    single<DisasterRepository> { OfflineFirstDisasterRepository(get(), get(), get()) }
    single<WeatherRepository> { OpenMeteoWeatherRepository(get()) }
}

val viewModelModule = module {
    viewModel { HomeViewModel(get()) }
    viewModel { (eventId: String) -> DetailsViewModel(eventId, get(), get()) }
}

val appModules = listOf(networkModule, databaseModule, repositoryModule, viewModelModule)
