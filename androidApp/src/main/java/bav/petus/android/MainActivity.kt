package bav.petus.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import bav.petus.PetsSDK
import bav.petus.android.navigation.AppWithBottomBar
import bav.petus.core.location.LocationHelper
import bav.petus.core.location.getLocation
import bav.petus.core.time.TimeRepository
import bav.petus.viewModel.main.MainViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val locationHelper: LocationHelper by inject()
    private val timeRepo: TimeRepository by inject()
    private val petsSDK: PetsSDK by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkForCoarseLocationPermissions()

        setContent {
            MyApplicationTheme {
                val mainViewModel: MainViewModel = koinViewModel()
                val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

                uiState?.let {
                    AppWithBottomBar(
                        uiState = it,
                        onAction = mainViewModel::onAction,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            petsSDK.applicationDidBecomeActive()

            if (timeRepo.isTimeToFetchWeather()) {
                locationHelper.getLocation()?.let { location ->
                    petsSDK.retrieveWeatherInBackground(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        info = null,
                    )
                }
            }
        }
    }

    private fun checkForCoarseLocationPermissions() {
        when (ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            PackageManager.PERMISSION_GRANTED -> {
                Log.d("cqhg43" , "ACCESS_COARSE_LOCATION already granted")
            }
            PackageManager.PERMISSION_DENIED -> {
                Log.d("cqhg43" , "ACCESS_COARSE_LOCATION not yet granted")
                requestPermissions(permission = Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                Log.d("cqhg43" , "ACCESS_COARSE_LOCATION granted")
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION, false) -> {
                Log.d("cqhg43" , "ACCESS_BACKGROUND_LOCATION granted")
            }
            else -> {
                Log.d("cqhg43" , "NOT granted")
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
