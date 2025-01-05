package com.felinetech.localcat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.felinetech.localcat.po.UploadConfigItem
import com.felinetech.localcat.pojo.ClientVo
import com.felinetech.localcat.pojo.FileItemVo
import com.felinetech.localcat.pojo.ServicePo
import com.felinetech.localcat.utlis.getNames
import com.felinetech.localcat.view_model.SettingViewModel.getFileName
import localcat.composeapp.generated.resources.Res
import localcat.composeapp.generated.resources.folder_black
import org.jetbrains.compose.resources.painterResource
import java.util.Locale

/**
 * 文件列表 Item
 */
@Composable
fun FileItem(item: FileItemVo) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(4.dp),
        color = Color(0x99ffffff),
        shape = RoundedCornerShape(5.dp),
        shadowElevation = 10.dp
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color(0x99ffffff))
                .fillMaxWidth()

        ) {
            Icon(
                painter = painterResource(item.fileType.imgId),
                contentDescription = "图片",
                modifier = Modifier
                    .background(Color(0x99ffffff))
                    .width(40.dp)
                    .height(40.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(Color(0x99ffffff))
                    .weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(0.5f)
                ) {
                    Text(
                        text = item.fileName,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 5.dp)
                    )
                    Text(text = "待上传", modifier = Modifier.padding(end = 5.dp))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(0.5f)
                ) {
                    LinearProgressIndicator(
                        progress = item.percent / 100f,
                        modifier = Modifier.weight(1f), color = Color.Black
                    )
                    Text(
                        text = "${item.fileSize / 1024 / 1024}M",
                        modifier = Modifier.padding(start = 2.dp)
                    )
                    Text(
                        text = "${item.percent}%",
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun ServerItem(servicePo: ServicePo, connectFun: (service: ServicePo) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(4.dp),
        color = Color(0x99ffffff),
        shape = RoundedCornerShape(5.dp),
        shadowElevation = 5.dp
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color.White)
                .fillMaxWidth()
        ) {
            Text(
                text = "${servicePo.id}",
                modifier = Modifier.width(20.dp),
                textAlign = TextAlign.Center
            )
            Text(text = servicePo.ip, modifier = Modifier.weight(1f))
            Text(
                text = servicePo.buttonState.name,
                modifier = Modifier.padding(end = 5.dp)
                    .clickable {
                        println("$servicePo 被点击")
                        connectFun(servicePo)
                    },
                color = MaterialTheme.colorScheme.tertiary
            )
            Text(
                text = servicePo.connectStatus.name,
                modifier = Modifier.padding(end = 5.dp),

                )
        }
    }
}

@Composable
fun ClientItem(clientVo: ClientVo) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(4.dp),
        color = Color(0x99ffffff),
        shape = RoundedCornerShape(5.dp),
        shadowElevation = 5.dp
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color.White)
                .fillMaxWidth()
        ) {
            Text(
                text = "${clientVo.number}",
                modifier = Modifier.width(20.dp),
                textAlign = TextAlign.Center
            )
            Text(text = clientVo.ip, modifier = Modifier.weight(1f))
            Text(text = clientVo.connectStatus.name, modifier = Modifier.width(60.dp))
        }
    }
}


@Composable
fun RuleItem(
    item: UploadConfigItem,
    edit: (item: UploadConfigItem) -> Unit,
    delete: (item: UploadConfigItem) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(5.dp), shape = RoundedCornerShape(5.dp), color = Color(0x99ffffff)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 5.dp, end = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.folder_black),
                contentDescription = "文件规则"
            )
            Text(text = getFileName(item.listeningDir), modifier = Modifier.weight(1f))
            Text(
                text = getNames(Locale.getDefault().language).edit,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .clickable {
                        edit(item)
                    }
            )
            Text(
                text = getNames(Locale.getDefault().language).delete,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.clickable {
                    delete(item)
                }
            )
        }
    }
}
