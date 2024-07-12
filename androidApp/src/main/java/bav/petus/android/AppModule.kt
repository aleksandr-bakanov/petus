package bav.petus.android

import bav.petus.PetsSDK
import bav.petus.cache.AndroidDatabaseDriverFactory
import bav.petus.network.WeatherApi
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<WeatherApi> { WeatherApi() }
    single<PetsSDK> {
        PetsSDK(
            databaseDriverFactory = AndroidDatabaseDriverFactory(androidContext()),
            api = get()
        )
    }

    viewModel { PetsViewModel(sdk = get()) }
}