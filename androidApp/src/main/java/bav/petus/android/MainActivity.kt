package bav.petus.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: PetsViewModel

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                Log.d("cqhg43" , "ACCESS_COARSE_LOCATION granted")
                checkForBackgroundLocationPermission()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION, false) -> {
                Log.d("cqhg43" , "ACCESS_BACKGROUND_LOCATION granted")
            }
            else -> {
                Log.d("cqhg43" , "NOT granted")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkForCoarseLocationPermissions()

        setContent {
            viewModel = koinViewModel<PetsViewModel>()
            App(
                viewModel = viewModel,
                requestBackgroundLocationPermission = {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                        requestPermissions(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }
                }
            )
        }
    }

    private fun checkForCoarseLocationPermissions() {
        when (ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            PackageManager.PERMISSION_GRANTED -> {
                Log.d("cqhg43" , "ACCESS_COARSE_LOCATION already granted")
                checkForBackgroundLocationPermission()
            }
            PackageManager.PERMISSION_DENIED -> {
                val shouldShowRationale = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
                Log.d("cqhg43" , "ACCESS_COARSE_LOCATION not yet granted; shouldShowRationale = $shouldShowRationale")
                requestPermissions(permission = Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
    }

    private fun checkForBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            when (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            ) {
                PackageManager.PERMISSION_GRANTED -> {
                    Log.d("cqhg43" , "ACCESS_BACKGROUND_LOCATION already granted")
                }
                PackageManager.PERMISSION_DENIED -> {
                    val shouldShowRationale = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    Log.d("cqhg43" , "ACCESS_BACKGROUND_LOCATION not yet granted; shouldShowRationale = $shouldShowRationale")
                    if (shouldShowRationale) {
                        viewModel.updateRationale(value = true)
                    }
                    else {
                        requestPermissions(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }
                }
            }
        }
    }

    private fun requestPermissions(permission: String) {
        // Before you perform the actual permission request, check whether your app
        // already has the permissions, and whether your app needs to show a permission
        // rationale dialog. For more details, see Request permissions:
        // https://developer.android.com/training/permissions/requesting#request-permission
        locationPermissionRequest.launch(
            arrayOf(permission)
        )
    }
}
