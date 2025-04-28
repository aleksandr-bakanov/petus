package bav.petus

import bav.petus.network.WeatherApi
import bav.petus.repo.PetsRepository
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.component.KoinComponent
import platform.BackgroundTasks.BGAppRefreshTask
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGTask
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate
import platform.Foundation.dateByAddingTimeInterval

/**
 * https://stackoverflow.com/questions/78937051/bgapprefreshtask-handler-not-being-called-despite-proper-setup-in-ios-app
 *
 * Result:
 *
 * After thorough research, I discovered that there is no guaranteed solution for ensuring
 * the execution of background tasks in iOS. The iOS system uses an internal prioritization
 * mechanism that depends heavily on the current device state and user behavior. Consequently,
 * it is not advisable to rely on background tasks for handling critical business cases,
 * as iOS makes its own decision on whether to execute the task based on several factors.
 *
 * Some key factors influencing iOS's decision-making process include:
 *
 * 1) User Activity: If the user is not a frequent user of the app, iOS may deprioritize or even
 * skip the scheduled background task execution.
 *
 * 2) Manual App Termination: If the user forcefully kills the app, any scheduled background
 * tasks will not be executed.
 *
 * 3) Low Power Mode: When Low Power Mode is enabled, iOS may delay or prevent background task
 * execution to conserve battery life.
 *
 * 4) Battery Level: If the device's battery is low, iOS may choose to delay or skip the task
 * in order to optimize power consumption.
 *
 * These factors, among others, illustrate the inherent uncertainty in relying on background tasks
 * for time-sensitive operations in iOS.
 *
 */

/**
 * This class is not used. It was an attempt to build Helper for background tasks logic.
 * Turns out it's easier (or is possible at all) to do in Swift.
 */
class BackgroundTaskHelper(
    private val locationHelper: LocationHelper,
    private val weatherApi: WeatherApi,
    private val petsRepository: PetsRepository,
): KoinComponent {

    fun registerBackgroundTask() {
        BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
            identifier = TASK_IDENTIFIER,
            usingQueue = null,
        ) { task: BGTask? ->
            handleAppRefresh(task as BGAppRefreshTask)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    fun scheduleAppRefresh(earliestBeginDate: NSDate? = null) {
        val date = earliestBeginDate ?: NSDate().dateByAddingTimeInterval(ti = 60.0 * 60.0)

        val request = BGAppRefreshTaskRequest(TASK_IDENTIFIER)
        request.earliestBeginDate = date

        BGTaskScheduler.sharedScheduler.submitTaskRequest(request, null)
    }

    private fun handleAppRefresh(task: BGAppRefreshTask) {
        // Schedule a new refresh task.
        scheduleAppRefresh()

        locationHelper.requestLocation(
            onSuccess = { latitude: Double, longitude: Double ->
                //weatherApi.getWeather()
            },
            onFailure = {
                task.setTaskCompletedWithSuccess(success = true)
            }
        )
    }

    companion object {
        const val TASK_IDENTIFIER = "bav.petus.weather_update"
    }
}





