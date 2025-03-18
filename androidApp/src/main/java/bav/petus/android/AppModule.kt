package bav.petus.android

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import bav.petus.PetsSDK
import bav.petus.cache.PetsDatabase
import bav.petus.cache.getDatabaseBuilder
import bav.petus.cache.getPetsDatabase
import bav.petus.core.datastore.createDataStore
import bav.petus.core.time.TimeRepository
import bav.petus.network.WeatherApi
import bav.petus.repo.PetsRepository
import bav.petus.repo.WeatherRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<WeatherApi> {
        WeatherApi()
    }

    single<PetsDatabase> {
        getPetsDatabase(
            builder = getDatabaseBuilder(ctx = androidContext())
        )
    }

    single<DataStore<Preferences>> {
        createDataStore(context = androidContext())
    }

    single<TimeRepository> {
        TimeRepository(dataStore = get())
    }

    single<PetsRepository> {
        PetsRepository(database = get())
    }

    single<WeatherRepository> {
        WeatherRepository(
            weatherApi = get(),
            timeRepository = get(),
            dataStore = get(),
        )
    }

    single<PetsSDK> {
        PetsSDK(
            petsRepository = get(),
            weatherRepository = get(),
        )
    }

    viewModel { PetsViewModel(sdk = get()) }
}