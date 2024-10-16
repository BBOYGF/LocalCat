package com.felinetech.localcat

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import localcat.composeapp.generated.resources.Res
import localcat.composeapp.generated.resources.cat9
import org.jetbrains.compose.resources.painterResource

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Local Cat",
        icon = painterResource(Res.drawable.cat9),
        state = WindowState(width = 500.dp, height = 1000.dp),
        resizable = false
    ) {
        App()
    }
}