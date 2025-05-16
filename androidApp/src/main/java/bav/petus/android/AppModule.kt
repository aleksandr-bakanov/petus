package bav.petus.android

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import bav.petus.PetsSDK
import bav.petus.android.helpers.WorkManagerHelper
import bav.petus.android.ui.cemetery.CemeteryScreenViewModel
import bav.petus.android.ui.common.PetImageUseCase
import bav.petus.android.ui.common.StringResourcesUseCase
import bav.petus.android.ui.pet_creation.PetCreationScreenViewModel
import bav.petus.android.ui.pet_details.PetDetailsScreenViewModel
import bav.petus.android.ui.pet_details.PetDetailsScreenViewModelArgs
import bav.petus.android.ui.user_profile.UserProfileScreenViewModel
import bav.petus.android.ui.weather_report.WeatherReportViewModel
import bav.petus.android.ui.zoo.ZooScreenViewModel
import bav.petus.android.ui.zoo.ZooScreenViewModelArgs
import bav.petus.cache.PetsDatabase
import bav.petus.cache.getDatabaseBuilder
import bav.petus.cache.getPetsDatabase
import bav.petus.core.datastore.createDataStore
import bav.petus.core.dialog.DialogSystem
import bav.petus.core.engine.Engine
import bav.petus.core.engine.QuestSystem
import bav.petus.core.engine.UserStats
import bav.petus.core.location.LocationHelper
import bav.petus.core.time.TimeRepository
import bav.petus.network.WeatherApi
import bav.petus.repo.PetsRepository
import bav.petus.repo.WeatherRepository
import bav.petus.useCase.WeatherAttitudeUseCase
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

    single {
        LocationHelper(context = androidContext())
    }

    single {
        WorkManagerHelper(
            context = androidContext(),
        )
    }

    single<TimeRepository> {
        TimeRepository(dataStore = get())
    }

    single<PetsRepository> {
        PetsRepository(database = get())
    }

    single<WeatherRepository> {
        WeatherRepository(
            database = get(),
        )
    }

    single<WeatherAttitudeUseCase> {
        WeatherAttitudeUseCase()
    }

    single<Engine> {
        Engine(
            petsRepo = get(),
            timeRepo = get(),
            weatherRepo = get(),
            weatherAttitudeUseCase = get(),
            userStats = get(),
            questSystem = get(),
        )
    }

    single {
        PetImageUseCase(
            engine = get(),
        )
    }

    single<PetsSDK> {
        PetsSDK(
            weatherApi = get(),
            weatherRepo = get(),
            engine = get(),
            timeRepo = get(),
        )
    }

    single<DialogSystem> {
        DialogSystem(
            userStats = get(),
            questSystem = get(),
        )
    }

    single {
        StringResourcesUseCase(
            context = androidContext()
        )
    }

    single {
        UserStats(
            dataStore = get(),
        )
    }

    single {
        QuestSystem(
            dataStore = get(),
            petsRepo = get(),
            userStats = get(),
        )
    }

    viewModel { params ->
        ZooScreenViewModel(
            petsRepo = get(),
            engine = get(),
            petImageUseCase = get(),
            args = params.get<ZooScreenViewModelArgs>(),
        )
    }

    viewModel { params ->
        PetDetailsScreenViewModel(
            petsRepo = get(),
            engine = get(),
            petImageUseCase = get(),
            stringResourcesUseCase = get(),
            dialogSystem = get(),
            questSystem = get(),
            args = params.get<PetDetailsScreenViewModelArgs>(),
        )
    }

    viewModel {
        PetCreationScreenViewModel(
            engine = get(),
            userStats = get(),
            stringResourcesUseCase = get(),
        )
    }

    viewModel {
        CemeteryScreenViewModel(
            petsRepo = get(),
            engine = get(),
            petImageUseCase = get(),
        )
    }

    viewModel {
        WeatherReportViewModel(
            weatherRepo = get(),
        )
    }

    viewModel {
        MainViewModel(
            petsRepo = get(),
        )
    }

    viewModel {
        UserProfileScreenViewModel(
            userStats = get(),
        )
    }
}