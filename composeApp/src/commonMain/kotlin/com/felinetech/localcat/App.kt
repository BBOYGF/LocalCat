package com.felinetech.localcat

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.felinetech.localcat.components.MenuButton
import com.felinetech.localcat.theme.LocalCatTheme
import com.felinetech.localcat.views.About
import com.felinetech.localcat.views.History
import com.felinetech.localcat.views.HomePage
import com.felinetech.localcat.views.Setting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    LocalCatTheme(
        darkTheme = false,
        dynamicColor = true
    ) {
        val navController: NavHostController = rememberNavController()
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Content(navController)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp) // 设置固定高度
                    .align(alignment = Alignment.BottomCenter)
                    .offset(y = (-10).dp)
            ) {
                Menu(navController)
            }
        }
        PermissionRequest()
    }
}

@Composable
fun Content(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "/home",
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        composable(route = "/home") {
            HomePage()
        }
        composable(route = "/setting") {
            Setting()
        }
        composable(route = "/history") {
            History()
        }
        composable(route = "/about") {
            About()
        }
    }
}

@Composable
fun Menu(navController: NavHostController) {
    val homeSelected = remember { mutableStateOf(false) }
    val settingSelected = remember { mutableStateOf(false) }
    val historySelected = remember { mutableStateOf(false) }
    val aboutSelected = remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    val buttonWidth by animateDpAsState(
        targetValue = if (isExpanded) 5.dp else 10.dp, // 点击时宽度从 100.dp 到 200.dp
        animationSpec = tween(durationMillis = 300) // 动画时长
    )

    val states: MutableList<MutableState<Boolean>> =
        mutableListOf(homeSelected, settingSelected, historySelected, aboutSelected)
    val scope = CoroutineScope(Dispatchers.Default)

    androidx.compose.material3.Surface(
        shape = RoundedCornerShape(25.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(start = buttonWidth, end = buttonWidth)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.background(androidx.compose.material3.MaterialTheme.colorScheme.primary),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MenuButton(homeSelected, Icons.Outlined.Home) {
                states.forEach { it.value = false }
                navController.navigate("/home")
                homeSelected.value = !homeSelected.value
                isExpanded = true

                scope.launch {
                    Thread.sleep(300)
                    isExpanded = false
                }
            }
            MenuButton(settingSelected, Icons.Outlined.Settings) {
                states.forEach { it.value = false }
                navController.navigate("/setting")
                settingSelected.value = !settingSelected.value
                isExpanded = true
                scope.launch {
                    Thread.sleep(300)
                    isExpanded = false
                }
            }
            MenuButton(historySelected, Icons.Outlined.List) {
                states.forEach { it.value = false }
                navController.navigate("/history")
                historySelected.value = !historySelected.value
                isExpanded = true
                scope.launch {
                    Thread.sleep(300)
                    isExpanded = false
                }
            }
            MenuButton(aboutSelected, Icons.Outlined.Person) {
                states.forEach { it.value = false }
                navController.navigate("/about")
                aboutSelected.value = !aboutSelected.value
                isExpanded = true
                scope.launch {
                    Thread.sleep(300)
                    isExpanded = false
                }
            }
        }
    }
}
@Composable
expect  fun PermissionRequest()