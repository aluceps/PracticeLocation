package me.aluceps.practicelocation

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class LocationPermissionHelper(
    activity: AppCompatActivity,
    private val listener: Listener,
) {
    interface Listener {
        fun updated(state: PermissionState)
    }

    enum class PermissionState {
        DENY,
        ALLOW_ACCESS_COARSE_LOCATION,
        ALLOW_ACCESS_FINE_LOCATION,
        ALLOW_ACCESS_BACKGROUND_LOCATION,
        SATISFIED,
    }

    private var permissionState = PermissionState.DENY
    val state get() = permissionState

    private val locationPermissionRequest = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
            if (permissionState == PermissionState.DENY) {
                updateLocationPermission(PermissionState.ALLOW_ACCESS_COARSE_LOCATION)
            }
        }
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            if (permissionState == PermissionState.ALLOW_ACCESS_COARSE_LOCATION) {
                updateLocationPermission(PermissionState.ALLOW_ACCESS_FINE_LOCATION)
            }
        }

        when (permissionState) {
            PermissionState.DENY -> {
                // 許可を求める
                AlertDialog.Builder(activity)
                    .setTitle("位置情報の許可")
                    .setMessage("この機能を使うために必要なので有効にしてください")
                    .setPositiveButton("はい", null)
                    .show()
            }
            PermissionState.ALLOW_ACCESS_FINE_LOCATION -> {
                // 常に許可のリクエストをする
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        AlertDialog.Builder(activity)
                            .setTitle("位置情報の許可")
                            .setMessage("より便利にするため位置情報を常に許可するよう設定してください")
                            .setPositiveButton("はい") { _, _ -> backgroundPermissionRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION) }
                            .setNegativeButton("いいえ", null)
                            .show()
                    } else {
                        backgroundPermissionRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }
                } else {
                    // 目的の処理をする
                    Toast.makeText(activity, "必要な権限が揃いました (2)", Toast.LENGTH_SHORT).show()
                    updateLocationPermission(PermissionState.SATISFIED)
                }
            }
            PermissionState.ALLOW_ACCESS_BACKGROUND_LOCATION -> {
                // 目的の処理をする
                Toast.makeText(activity, "必要な権限が揃いました (1)", Toast.LENGTH_SHORT).show()
                updateLocationPermission(PermissionState.SATISFIED)
            }
            else -> {
                // 何もしない
            }
        }
    }

    // "常に許可する" を有効にするためのリクエスト
    private val backgroundPermissionRequest = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (permissionState == PermissionState.ALLOW_ACCESS_FINE_LOCATION) {
                updateLocationPermission(PermissionState.ALLOW_ACCESS_BACKGROUND_LOCATION)
            }
        }
    }

    fun requestPermission() {
        val permissions = when (Build.VERSION.SDK_INT) {
            Build.VERSION_CODES.Q -> arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            )
            else -> arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        }
        locationPermissionRequest.launch(permissions)
    }

    private fun updateLocationPermission(state: PermissionState) {
        permissionState = state
        listener.updated(permissionState)
    }
}
