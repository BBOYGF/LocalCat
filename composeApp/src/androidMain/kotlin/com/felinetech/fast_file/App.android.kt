package com.felinetech.fast_file

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.felinetech.fast_file.Constants.PRIVACY_URL
import com.felinetech.fast_file.utlis.getNames
import com.felinetech.fast_file.utlis.initDataService
import com.felinetech.fast_file.utlis.initKeepConnectService
import com.felinetech.fast_file.utlis.initReceiverService
import com.felinetech.fast_file.utlis.initUploadService
import com.felinetech.fast_file.view_model.HomeViewModel.updateIpAble
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.lang.StringBuilder
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun PermissionRequest() {
    val context = LocalContext.current
    val sharedPreferences =
        remember { context.getSharedPreferences("local_cat.cache", Context.MODE_PRIVATE) }
    // 读取存储的值
    var showPrivate by remember {
        mutableStateOf(
            sharedPreferences.getBoolean(
                Constants.PRIVACY,
                true
            )
        )
    }
    updateIpAble = !showPrivate
    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(), // 使用请求多个权限的协定
        onResult = { permissionsStatusMap: Map<String, Boolean> -> // 回调接收 Map
            Log.d("PermissionsDemo", "权限请求结果: $permissionsStatusMap")
            // --- 5. 处理请求结果 ---
            // 检查 Map 中是否 *所有* 权限都被授予了
            val allGranted = permissionsStatusMap.all { entry -> entry.value }


            if (allGranted) {
                Log.i("PermissionsDemo", "所有请求的权限均已授予!")
                // 可以执行需要所有权限的操作
                initService()
            } else {
                Log.w("PermissionsDemo", "部分或全部权限被拒绝")
                // 可以检查哪些权限被拒绝了
                permissionsStatusMap.forEach { (permission, isGranted) ->
                    if (!isGranted) {
                        Log.w("PermissionsDemo", "$permission 权限被拒绝")
                        // 可以针对性地提示用户或禁用功能
                    }
                }
            }
        }
    )

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.NEARBY_WIFI_DEVICES,
            Manifest.permission.ACCESS_COARSE_LOCATION,

//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.REQUEST_INSTALL_PACKAGES,
        )
    )
    val permissionsToRequest = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            arrayOf(
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.NEARBY_WIFI_DEVICES,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        } else {
            // Android 12 及以下
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE, // 请求旧的存储权限
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.NEARBY_WIFI_DEVICES,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        }
    }
    // 检查权限状态
    LaunchedEffect(permissionsState) {
        if (permissionsState.allPermissionsGranted) {
            // 所有权限均已授予
            Toast.makeText(context, "权限都已授权！", Toast.LENGTH_LONG).show()
            sharedPreferences.edit().putBoolean(Constants.PRIVACY, false).apply()
            initService()
        } else {
            // 至少有一个权限未被授予
            Toast.makeText(
                context, "权限未被授予！", Toast.LENGTH_LONG
            ).show()
            sharedPreferences.edit().putBoolean(Constants.PRIVACY, true).apply()
            showPrivate = true
            updateIpAble=false
            getTextToShowGivenPermissions(
                permissionsState.revokedPermissions,
                permissionsState.shouldShowRationale
            )
            if (!showPrivate) {
                multiplePermissionsLauncher.launch(permissionsToRequest)
            }
        }
    }
    if (showPrivate) {
        Dialog(onDismissRequest = { }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = getNames(Locale.getDefault().language).privacyPolicyPermissions,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(5.dp),
                        fontSize = TextUnit(20F, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .padding(10.dp)
                            .background(Color.White, shape = RoundedCornerShape(10.dp))
                            .verticalScroll(ScrollState(0), enabled = true)
                    ) {
                        val start = getNames(Locale.getDefault().language).policyContent1
                        val userAgreement = getNames(Locale.getDefault().language).userAgreement
                        val privacyPolicyTitle =
                            getNames(Locale.getDefault().language).privacyPolicyTitle
                        val end = getNames(Locale.getDefault().language).policyContent2
                        val annotatedText = buildAnnotatedString {
                            append(start)
                            withStyle(
                                style = SpanStyle(
                                    color = Color(0xFF0E9FF2),
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(userAgreement)
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = Color(0xFF0E9FF2),
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(privacyPolicyTitle)
                            }

                            append(end)
                        }
                        ClickableText(
                            text = annotatedText,
                            onClick = { _ ->
                                Log.d(
                                    TAG,
                                    getNames(Locale.getDefault().language).privacyPolicyPermissions
                                )
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_URL))
                                MainActivity.mainActivity.startActivity(intent)
                            }
                        )

                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                MainActivity.mainActivity.finish()
                            }, colors = ButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                disabledContainerColor = MaterialTheme.colorScheme.primary,
                                disabledContentColor = Color.Black,
                            ), shape = RoundedCornerShape(5.dp)
                        ) {
                            Text(
                                text = getNames(Locale.getDefault().language).doNot,
                            )
                        }

                        Button(
                            onClick = {
//                                permissionsState.launchMultiplePermissionRequest()
                                multiplePermissionsLauncher.launch(permissionsToRequest)
                                showPrivate = false
                                updateIpAble=true
                            },

                            colors = ButtonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = Color.White,
                                disabledContainerColor = MaterialTheme.colorScheme.tertiary,
                                disabledContentColor = Color.Black,
                            ), shape = RoundedCornerShape(5.dp)
                        ) {
                            Text(
                                text = getNames(Locale.getDefault().language).agree,
                            )
                        }
                    }
                }

            }
        }
    }
}

private fun initService() {
    initReceiverService()
    initDataService()
    initKeepConnectService()
    initUploadService()
}

@OptIn(ExperimentalPermissionsApi::class)
private fun getTextToShowGivenPermissions(
    permissions: List<PermissionState>,
    shouldShowRationale: Boolean
): String {
    val revokedPermissionsSize = permissions.size
    if (revokedPermissionsSize == 0) return ""
    val textToShow = StringBuilder()
    textToShow.append("The ")
    for (i in permissions.indices) {
        textToShow.append(permissions[i].permission)
        when {
            revokedPermissionsSize > 1 && i == revokedPermissionsSize - 2 -> {
                textToShow.append(", and ")
            }

            i == revokedPermissionsSize - 1 -> {
                textToShow.append(" ")
            }

            else -> {
                textToShow.append(", ")
            }
        }
    }
    textToShow.append(if (revokedPermissionsSize == 1) "permission is" else "permissions are")
    textToShow.append(
        if (shouldShowRationale) {
            " important. Please grant all of them for the app to function properly."
        } else {
            " denied. The app cannot function without them."
        }
    )
    return textToShow.toString()
}