package com.felinetech.fast_file

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import io.github.vinceglb.filekit.core.FileKit

class MainActivity : ComponentActivity() {
    companion object {
        lateinit var instance: MainActivity

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
//        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        instance = this // 设置全局实例
        FileKit.init(this)
        setContent {
            App()
        }
        // 设置状态栏颜色
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.Black.toArgb() // 将状态栏颜色设置为黑色
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
