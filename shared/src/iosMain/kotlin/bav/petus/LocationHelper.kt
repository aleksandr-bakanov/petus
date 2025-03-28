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

val locationManagerDelegate = LocationManagerDelegate()

fun initLocationManager() {
    val manager = CLLocationManager()
    manager.delegate = locationManagerDelegate
}

class LocationManagerDelegate : NSObject(), CLLocationManagerDelegateProtocol {
    @OptIn(ExperimentalForeignApi::class)
    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        println("didUpdateLocations: locations = $didUpdateLocations")
        didUpdateLocations.lastOrNull()?.let { location ->
            val loc: CLLocation = location as CLLocation
            loc.coordinate.useContents {
                println("didUpdateLocations: last location = ${this.latitude} - ${this.longitude}")
            }
        }
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        println("didFailWithError: ${didFailWithError.description()}")
    }

    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        when (manager.authorizationStatus) {
            kCLAuthorizationStatusNotDetermined -> {
                println("locationManagerDidChangeAuthorization: status = ${manager.authorizationStatus} NotDetermined")
                manager.requestWhenInUseAuthorization()
            }
            kCLAuthorizationStatusAuthorized -> {
                println("locationManagerDidChangeAuthorization: status = ${manager.authorizationStatus} Authorized")
                manager.requestLocation()
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
}