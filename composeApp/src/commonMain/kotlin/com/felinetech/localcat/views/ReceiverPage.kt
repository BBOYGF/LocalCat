package com.felinetech.localcat.views

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.felinetech.localcat.components.ClientItem
import com.felinetech.localcat.components.FileItem
import com.felinetech.localcat.components.ReceiverAnimation
import com.felinetech.localcat.theme.borderColor
import com.felinetech.localcat.utlis.getNames
import com.felinetech.localcat.view_model.HomeViewModel.clickReceiverButton
import com.felinetech.localcat.view_model.HomeViewModel.clineList
import com.felinetech.localcat.view_model.HomeViewModel.receiverAnimation
import com.felinetech.localcat.view_model.HomeViewModel.receiverButtonTitle
import com.felinetech.localcat.view_model.HomeViewModel.toBeDownloadFileList
import com.felinetech.localcat.view_model.MainViewModel
import org.slf4j.LoggerFactory
import java.util.*


/**
 * 接收者
 */
@Composable
fun Receiver(turnState: Boolean) {
    val start = remember { Animatable(if (turnState) 180f else 0f) }
    val deepStart = remember { Animatable(if (turnState) 0.6f else 1f) }
    val receiverBT by receiverButtonTitle.collectAsState()
    val receiverAnima by receiverAnimation.collectAsState()

    val logger = remember { LoggerFactory.getLogger("测试") }

    if (turnState) {
        LaunchedEffect(false) {
            deepStart.animateTo(
                targetValue = 0.55f,
                animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing)
            )
            deepStart.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing)
            )
        }
        LaunchedEffect(false) {
            start.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
                rotationY = start.value,
                scaleX = deepStart.value,
                scaleY = deepStart.value
            )
    ) {
        // 当前链接的发送者和当前接收的文件
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp),

            ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp), color = Color(0x00ffffff), shape = RoundedCornerShape(5.dp)
            ) {
                if (receiverAnima) {
                    ReceiverAnimation()
                }
            }
            Text(text = getNames(Locale.getDefault().language).senderOfTheConnection)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .border(1.dp, color = borderColor, shape = RoundedCornerShape(5.dp)), color = Color(0x99ffffff),
                shape = RoundedCornerShape(5.dp)
            ) {
                LazyColumn {
                    items(clineList) { item ->
                        ClientItem(item)
                    }
                }
            }
            Text(text = getNames(Locale.getDefault().language).currentlyReceivingFiles)

            LazyColumn {
                items(toBeDownloadFileList.size) { index ->
                    val item = toBeDownloadFileList.getOrNull(index)
                    if (item != null) {
                        FileItem(item = item)
                    } else {
                        Text("无效项")
                    }
                }
            }

        }
        // 开始上传按钮层
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(alignment = Alignment.BottomCenter)
                .offset(y = (-70).dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { clickReceiverButton() }) {
                Text(
                    text = receiverBT,
                    modifier = Modifier.width(100.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    MainViewModel.receiveState = true
    MainViewModel.sendState = false
}

