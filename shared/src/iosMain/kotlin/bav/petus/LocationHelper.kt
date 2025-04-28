package bav.petus

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorized
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.Foundation.NSError
import platform.darwin.NSObject

/**
 * This class is not used. It was an attempt to build Helper for location requests logic.
 * Turns out it's easier to do in Swift.
 */
class LocationHelper : NSObject(), CLLocationManagerDelegateProtocol {
    private val manager = CLLocationManager()

    private var onSuccess: ((Double, Double) -> Unit)? = null
    private var onFailure: (() -> Unit)? = null

    init {
        manager.delegate = this
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        println("didUpdateLocations: locations = $didUpdateLocations")
        didUpdateLocations.lastOrNull()?.let { location ->
            val loc: CLLocation = location as CLLocation
            loc.coordinate.useContents {
                println("didUpdateLocations: last location = ${this.latitude} - ${this.longitude}")
                onSuccess?.let { it(this.latitude, this.longitude) }
                onSuccess = null
            }
        }
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        println("didFailWithError: ${didFailWithError.description()}")
        onFailure?.let { it() }
        onFailure = null
    }

    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        when (manager.authorizationStatus) {
            kCLAuthorizationStatusNotDetermined -> {
                println("locationManagerDidChangeAuthorization: status = ${manager.authorizationStatus} NotDetermined")
                manager.requestWhenInUseAuthorization()
            }
            kCLAuthorizationStatusAuthorized -> {
                println("locationManagerDidChangeAuthorization: status = ${manager.authorizationStatus} Authorized")
            }
            kCLAuthorizationStatusAuthorizedAlways -> println("locationManagerDidChangeAuthorization: status = ${manager.authorizationStatus} AuthorizedAlways")
            kCLAuthorizationStatusAuthorizedWhenInUse -> {
                println("locationManagerDidChangeAuthorization: status = ${manager.authorizationStatus} AuthorizedWhenInUse")
                manager.requestAlwaysAuthorization()
            }
            kCLAuthorizationStatusDenied -> println("locationManagerDidChangeAuthorization: status = ${manager.authorizationStatus} Denied")
            kCLAuthorizationStatusRestricted -> println("locationManagerDidChangeAuthorization: status = ${manager.authorizationStatus} Restricted")
            else -> println("locationManagerDidChangeAuthorization: status = ${manager.authorizationStatus} UNKNOWN STATUS")
        }
    }

    fun requestLocation(
        onSuccess: (latitude: Double, longitude: Double) -> Unit,
        onFailure: () -> Unit,
    ) {
        this.onSuccess = onSuccess
        this.onFailure = onFailure

        manager.requestLocation()
    }
}