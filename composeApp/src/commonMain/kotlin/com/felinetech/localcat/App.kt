package com.felinetech.localcat

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.felinetech.localcat.components.BottomSheetPar
import com.felinetech.localcat.components.MenuButton
import com.felinetech.localcat.theme.LocalCatTheme
import com.felinetech.localcat.utlis.getNames
import com.felinetech.localcat.view_model.MainViewModel.msgPair
import com.felinetech.localcat.view_model.MainViewModel.showDialog
import com.felinetech.localcat.views.About
import com.felinetech.localcat.views.History
import com.felinetech.localcat.views.HomePage
import com.felinetech.localcat.views.SettingView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import localcat.composeapp.generated.resources.Res
import localcat.composeapp.generated.resources.cat_empty
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.util.*

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

        BottomSheetPar()

        if (showDialog) {
            MessageDialog()
        }
    }
}

@Composable
private fun MessageDialog() {
    val defScope = CoroutineScope(Dispatchers.Default)
    var isDialogVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isDialogVisible = true // 触发进入动画
    }
    // 背景遮罩
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.1f)) // 半透明黑色背景
            .clickable { // 点击背景关闭弹窗
                isDialogVisible = false
                defScope.launch {
                    delay(timeMillis = 450)
                    showDialog = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isDialogVisible,
            enter = fadeIn(animationSpec = tween(500)) + scaleIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(500)) + scaleOut(animationSpec = tween(500))
        ) {
            Column(
                modifier = Modifier
                    .padding(30.dp)
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(size = 10.dp)
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = msgPair.first,
                        color = MaterialTheme.colorScheme.tertiary,
                        style = TextStyle(fontSize = 35.sp),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp, start = 20.dp).weight(1f)
                    )

                }
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(300.dp).padding(start = 20.dp, end = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.background(
                                color = Color.White,
                                shape = RoundedCornerShape(5.dp)
                            )
                                .fillMaxWidth().height(100.dp).padding(top = 30.dp),
                        ) {
                            Text(
                                modifier = Modifier.fillMaxSize(),
                                textAlign = TextAlign.Center,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                text = msgPair.second,
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(x = 100.dp, y = (-60).dp) // 向上偏移 20 像素
                        ) {
                            Image(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(80.dp)
                                    .align(Alignment.Center),
                                painter = painterResource(Res.drawable.cat_empty),
                                contentDescription = "logo"
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        isDialogVisible = false
                        defScope.launch {
                            delay(timeMillis = 450)
                            showDialog = false
                        }
                    },
                    modifier = Modifier.width(100.dp).height(50.dp).padding(bottom = 15.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text(
                        text = getNames(Locale.getDefault().language).okText,
                        color = MaterialTheme.colorScheme.primary,
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
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
            SettingView()
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
            modifier = Modifier.background(MaterialTheme.colorScheme.primary),
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
expect fun PermissionRequest()