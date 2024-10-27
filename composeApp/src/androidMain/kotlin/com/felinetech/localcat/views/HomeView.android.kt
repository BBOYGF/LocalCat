package com.felinetech.localcat.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.felinetech.localcat.Constants.PRIVACY
import com.felinetech.localcat.utlis.getNames
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@SuppressLint("PermissionLaunchedDuringComposition")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun PermissionRequest() {
    val context = LocalContext.current
    val sharedPreferences =
        remember { context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE) }
    // 读取存储的值
    var showPrivate by remember { mutableStateOf(sharedPreferences.getBoolean(PRIVACY, true)) }

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
//            sharedPreferences.edit().putBoolean(PRIVACY, false).apply()
        } else {
            // 至少有一个权限未被授予
            Toast.makeText(context, "至少有一个权限未被授予！", Toast.LENGTH_LONG).show()
//            sharedPreferences.edit().putBoolean(PRIVACY, true).apply()
        }
    }
    // 显示请求权限的按钮
//    Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
//        Text("Request Permissions")
//    }

    // 显示权限状态
//    permissionsState.permissions.forEach { permission ->
//        Text(text = "${permission.permission}: ${if (permission.status.isGranted) "Granted" else "Denied"}")
//    }
    if (showPrivate) {
        Dialog(onDismissRequest = { }) {
            Card(
                modifier = Modifier.fillMaxWidth()
                    .height(300.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "隐私政策及权限",
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(5.dp),
                        fontSize = TextUnit(20F, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .height(220.dp)
                            .padding(10.dp)
                            .background(Color.White, shape = RoundedCornerShape(10.dp))
                            .verticalScroll(ScrollState(0), enabled = true)
                    ) {
                        Text(
                            getNames(Locale.getDefault().language).privacyPolicy,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {

                            },
                            colors = ButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                disabledContainerColor = MaterialTheme.colorScheme.primary,
                                disabledContentColor = Color.Black,
                            ), shape = RoundedCornerShape(5.dp)
                        ) {
                            Text(
                                text = "不同意",
                            )
                        }

                        Button(
                            onClick = {
                                permissionsState.launchMultiplePermissionRequest()
                                showPrivate = false
                                sharedPreferences.edit().putBoolean(PRIVACY, false).apply()
                            },

                            colors = ButtonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = Color.White,
                                disabledContainerColor = MaterialTheme.colorScheme.tertiary,
                                disabledContentColor = Color.Black,
                            ), shape = RoundedCornerShape(5.dp)
                        ) {
                            Text(
                                text = "同意",
                            )
                        }
                    }
                }

            }
        }
    }
}