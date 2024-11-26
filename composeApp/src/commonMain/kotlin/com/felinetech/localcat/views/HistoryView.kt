package com.felinetech.localcat.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.window.Dialog
import com.felinetech.localcat.components.ColorBackground
import com.felinetech.localcat.components.FileItem
import com.felinetech.localcat.enums.FileType
import com.felinetech.localcat.enums.UploadState
import com.felinetech.localcat.pojo.FileItemVo
import com.felinetech.localcat.utlis.getNames
import com.felinetech.localcat.view_model.HomeViewModel.cleanHistory
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
        val scanFileList = mutableListOf<FileItemVo>()
        val scanFileList2 = mutableListOf<FileItemVo>()
        for (i in 1..10) {
            scanFileList.add(FileItemVo("$i", FileType.doc文档, "$i", UploadState.待上传, 10, i.toLong()))
        }
        for (i in 1..5) {
            scanFileList2.add(FileItemVo("$i", FileType.doc文档, "$i", UploadState.待上传, 10, i.toLong()))
        }
        if (index == 0) {
            LazyColumn {
                items(scanFileList) { item ->
                    FileItem(item)
                }
            }
        } else {
            LazyColumn {
                items(scanFileList2) { item ->
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
            Text("清空历史记录", fontWeight = FontWeight.Bold)
        }
    }
    if (showDialog) {
        Dialog(onDismissRequest = {
            println("隐藏弹窗")
            showDialog = false
        }) {
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
                            "是否删除所有记录？", modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = TextUnit(20f, TextUnitType.Sp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            , horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Button(onClick = {
                            showDialog = false
                            // 执行删除所有历史记录
                            cleanHistory()
                        },shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) {
                            Text( getNames(Locale.getDefault().language).okText)
                        }
                        Button(onClick = {
                            showDialog = false
                        }, shape = RoundedCornerShape(12.dp)) {
                            Text( getNames(Locale.getDefault().language).cancelButton)
                        }
                    }
                }
            }
        }
    }
}
