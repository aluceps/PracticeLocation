package me.aluceps.practicelocation

import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class LocationPermissionHelper(
    activity: AppCompatActivity,
    private val listener: Listener,
) {
    interface Listener {
        fun state(state: PermissionState)
        fun deny()
        fun showRequestReason(action: () -> Unit)
        fun ready()
    }

    enum class PermissionState {
        DENY,
        ALLOW_ACCESS_COARSE_LOCATION,
        ALLOW_ACCESS_FINE_LOCATION,
        ALLOW_ACCESS_BACKGROUND_LOCATION,
        SATISFIED,
    }

    private var permissionState = PermissionState.DENY

    /**
     * 位置情報のリクエスト
     */
    private val locationPermissionRequest = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // おおまかな位置情報を許可された
        if (permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
            if (permissionState == PermissionState.DENY) {
                updateLocationPermission(PermissionState.ALLOW_ACCESS_COARSE_LOCATION)
            }
        }

        // 正確な位置情報を許可された
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            if (permissionState == PermissionState.ALLOW_ACCESS_COARSE_LOCATION) {
                updateLocationPermission(PermissionState.ALLOW_ACCESS_FINE_LOCATION)
            }
        }

        when (permissionState) {
            PermissionState.DENY -> {
                listener.deny()
            }
            PermissionState.ALLOW_ACCESS_FINE_LOCATION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // 常に許可にするリクエストをする
                    if (activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        // 許可してもらうためユーザに根拠を示す
                        listener.showRequestReason {
                            backgroundPermissionRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        }
                    } else {
                        backgroundPermissionRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }
                } else {
                    // 目的の処理をする
                    updateLocationPermission(PermissionState.SATISFIED)
                    listener.ready()
                }
            }
            PermissionState.ALLOW_ACCESS_BACKGROUND_LOCATION -> {
                // 目的の処理をする
                updateLocationPermission(PermissionState.SATISFIED)
                listener.ready()
            }
            PermissionState.ALLOW_ACCESS_COARSE_LOCATION,
            PermissionState.SATISFIED -> {
                // 何もしない
            }
        }
    }

    /**
     * 位置情報を "常に許可" にするためのリクエスト
     */
    private val backgroundPermissionRequest = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (permissionState == PermissionState.ALLOW_ACCESS_FINE_LOCATION) {
                updateLocationPermission(PermissionState.ALLOW_ACCESS_BACKGROUND_LOCATION)
            }
        }
    }

    /**
     * 位置情報のリクエスト
     * API 29 は権限ダイアログで "常に許可" を選択することができるが、API 30 以降は設定ページから選択する必要がある
     * https://developer.android.com/training/location/permissions#request-background-location
     */
    fun requestPermission() {
        val permissions = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q)
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            )
        else
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        locationPermissionRequest.launch(permissions)
    }

    private fun updateLocationPermission(state: PermissionState) {
        permissionState = state
        listener.state(permissionState)
    }
}
