package com.felinetech.fast_file.views

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.felinetech.fast_file.components.ColorBackground
import com.felinetech.fast_file.components.FileItem
import com.felinetech.fast_file.utlis.getNames
import com.felinetech.fast_file.view_model.HistoryViewModel.downloadedFileList
import com.felinetech.fast_file.view_model.HistoryViewModel.uploadedFileList
import com.felinetech.fast_file.view_model.HomeViewModel.cleanHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


@Composable
fun History() {
    var showDialog by remember { mutableStateOf(false) }
    ColorBackground()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.height(800.dp)
    ) {
        var index by remember { mutableStateOf(0) }
        Text(
            text = getNames(Locale.getDefault().language).history, color = Color.White, style = TextStyle(
                fontWeight = FontWeight.Bold, fontSize = TextUnit(20f, TextUnitType.Sp)
            )
        )
        TabRow(
            selectedTabIndex = index,
            modifier = Modifier.background(Color(0xFF018786)).border(width = 1.dp, color = Color(0xFF018786))
        ) {
            Tab(
                selected = true, onClick = {
                    index = 0
                }, modifier = Modifier.background(Color(0xFF018786))
            ) {
                Text(text = getNames(Locale.getDefault().language).downloadListRetrieved)
            }
            Tab(
                selected = false, onClick = {
                    index = 1
                }, modifier = Modifier.background(Color(0xFF018786))
            ) {
                Text(text = getNames(Locale.getDefault().language).uploadListCompleted)
            }
        }

        if (index == 0) {
            LazyColumn {
                items(downloadedFileList) { item ->
                    FileItem(item)
                }
            }
        } else {
            LazyColumn {
                items(uploadedFileList) { item ->
                    FileItem(item)
                }
            }
        }


    }
    Box(
        modifier = Modifier.padding(bottom = 60.dp, end = 15.dp).fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Button(
            onClick = {          // 清除所有历史记录
                showDialog = true
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0x000000),
                contentColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Text( getNames(Locale.getDefault().language).clearHistory, fontWeight = FontWeight.Bold)
        }
    }
    if (showDialog) {
        val defScope = CoroutineScope(Dispatchers.Default)
        var isDialogVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            isDialogVisible = true // 触发进入动画
        }

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
                Card(
                    modifier = Modifier
                        .width(300.dp)
                        .height(300.dp)

                ) {
                    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(0.2f), verticalAlignment = Alignment.CenterVertically

                        ) {
                            Text(
                                text = "提示", modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontSize = TextUnit(25f, TextUnitType.Sp),
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(0.6f), verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                getNames(Locale.getDefault().language).deleteAllMsg, modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontSize = TextUnit(20f, TextUnitType.Sp)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Button(
                                onClick = {
                                    isDialogVisible = false
                                    defScope.launch {
                                        delay(timeMillis = 450)
                                        showDialog = false
                                    }

                                    // 执行删除所有历史记录
                                    cleanHistory()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Text(getNames(Locale.getDefault().language).okText)
                            }
                            Button(onClick = {
                                isDialogVisible = false
                                defScope.launch {
                                    delay(timeMillis = 450)
                                    showDialog = false
                                }
                            }, shape = RoundedCornerShape(12.dp)) {
                                Text(getNames(Locale.getDefault().language).cancelButton)
                            }
                        }
                    }
                }
            }
        }
    }
}
