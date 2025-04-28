package bav.petus.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
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
        onSuccess: (Location) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        val coarseLocationPermitted = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val backgroundLocationPermitted = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            coarseLocationPermitted
        }
        if (!coarseLocationPermitted || !backgroundLocationPermitted) {
            onFailure(Exception("Location access not permitted: isCoarse = $coarseLocationPermitted, isBackground = $backgroundLocationPermitted"))
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
            Log.d("cqhg43", "LOCA Success location = $location")
            onSuccess(location)
        }.addOnFailureListener { exception ->
            Log.d("cqhg43", "LOCA Failure $exception")
            onFailure(exception)
        }.addOnCanceledListener {
            Log.d("cqhg43", "LOCA Canceled")
        }.addOnCompleteListener {
            Log.d("cqhg43", "LOCA Completed")
        }
    }
}

suspend fun LocationHelper.getLocation(): Location? =
    suspendCoroutine { cont ->
        this.getCurrentLocation(
            onSuccess = { location ->
                cont.resume(location)
            },
            onFailure = { _ ->
                cont.resume(null)
            }
        )
    }