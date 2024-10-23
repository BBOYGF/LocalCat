package com.felinetech.localcat.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.felinetech.localcat.components.ColorBackground
import com.felinetech.localcat.components.FileItem
import com.felinetech.localcat.enums.FileType
import com.felinetech.localcat.enums.UploadState
import com.felinetech.localcat.pojo.FileItemVo
import com.felinetech.localcat.utlis.getNames
import java.util.Locale


@Composable
fun History() {
    ColorBackground()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.height(800.dp)
    ) {
        var index by remember { mutableStateOf(0) }
        Text(
            text = getNames(Locale.getDefault().language).history, color = Color.White,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = TextUnit(20f, TextUnitType.Sp)
            )
        )
        TabRow(
            selectedTabIndex = index,
            modifier = Modifier
                .background(Color(0xFF018786))
                .border(width = 1.dp, color = Color(0xFF018786))
        ) {
            Tab(
                selected = true, onClick = {
                    index = 0
                }, modifier = Modifier.background(Color(0xFF018786))
            ) {
                Text(text = getNames(Locale.getDefault().language).downloadListRetrieved)
            }
            Tab(
                selected = false, onClick =
                {
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

}
