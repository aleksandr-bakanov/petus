package bav.petus.android

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import bav.petus.PetsSDK
import bav.petus.android.helpers.WorkManagerHelper
import bav.petus.viewModel.cemetery.CemeteryScreenViewModel
import bav.petus.android.ui.common.StringResourcesUseCase
import bav.petus.viewModel.petCreation.PetCreationScreenViewModel
import bav.petus.viewModel.petDetails.PetDetailsScreenViewModel
import bav.petus.viewModel.petDetails.PetDetailsScreenViewModelArgs
import bav.petus.viewModel.userProfile.UserProfileScreenViewModel
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
import bav.petus.repo.HistoryRepository
import bav.petus.repo.PetsRepository
import bav.petus.repo.WeatherRepository
import bav.petus.useCase.PetImageUseCase
import bav.petus.useCase.WeatherAttitudeUseCase
import bav.petus.viewModel.dialog.DialogScreenViewModel
import bav.petus.viewModel.dialog.DialogScreenViewModelArgs
import bav.petus.viewModel.main.MainViewModel
import bav.petus.viewModel.weatherReport.WeatherReportViewModel
import bav.petus.viewModel.zoo.ZooScreenViewModel
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
            historyRepo = get(),
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

    single {
        HistoryRepository(
            database = get(),
        )
    }

    viewModel {
        ZooScreenViewModel()
    }

    viewModel { params ->
        PetDetailsScreenViewModel(
            args = params.get<PetDetailsScreenViewModelArgs>(),
        )
    }

    viewModel { params ->
        val stringResourcesUseCase: StringResourcesUseCase = get()
        DialogScreenViewModel(
            args = DialogScreenViewModelArgs(
                petId = params.get<Long>(),
                convertStringIdToString = stringResourcesUseCase::getString,
            )
        )
    }

    viewModel {
        PetCreationScreenViewModel()
    }

    viewModel {
        CemeteryScreenViewModel()
    }

    viewModel {
        WeatherReportViewModel()
    }

    viewModel {
        MainViewModel()
    }

    viewModel {
        UserProfileScreenViewModel()
    }
}