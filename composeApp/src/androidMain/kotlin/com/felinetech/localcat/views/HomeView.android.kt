package com.felinetech.localcat.views

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@SuppressLint("PermissionLaunchedDuringComposition")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun PermissionRequest() {
    val context = LocalContext.current

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.NEARBY_WIFI_DEVICES,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.REQUEST_INSTALL_PACKAGES
        )
    )
    // 检查权限状态
    LaunchedEffect(permissionsState) {
        if (permissionsState.allPermissionsGranted) {
            // 所有权限均已授予
            Toast.makeText(context, "权限都已授权！", Toast.LENGTH_LONG).show()
        } else {
            // 至少有一个权限未被授予
            Toast.makeText(context, "至少有一个权限未被授予！", Toast.LENGTH_LONG).show()
        }
    }
    // 显示请求权限的按钮
    Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
        Text("Request Permissions")
    }

    // 显示权限状态
    permissionsState.permissions.forEach { permission ->
        Text(text = "${permission.permission}: ${if (permission.status.isGranted) "Granted" else "Denied"}")
    }
}