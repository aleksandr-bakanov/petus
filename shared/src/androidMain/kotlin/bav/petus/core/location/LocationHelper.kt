package bav.petus.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocationHelper(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fun getCurrentLocation(
        onSuccess: (Location?) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        val coarseLocationPermitted = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!coarseLocationPermitted) {
            onFailure(Exception("Location access not permitted"))
            return
        }

        fusedLocationClient.getCurrentLocation(
            CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setGranularity(Granularity.GRANULARITY_COARSE)
                .setMaxUpdateAgeMillis(0)
                .build(),
            null,
        ).addOnSuccessListener { location ->
            if (location != null) {
                onSuccess(location)
            } else {
                onFailure(Exception("Received null location in success listener"))
            }
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }.addOnCanceledListener {
            onFailure(Exception("LOCA canceled"))
        }.addOnCompleteListener {

        }
    }
}

suspend fun LocationHelper.getLocation(): Location? =
    suspendCoroutine { cont ->
        this.getCurrentLocation(
            onSuccess = { location ->
                cont.resume(location)
            },
            onFailure = { error ->
                cont.resume(null)
            }
        )
    }