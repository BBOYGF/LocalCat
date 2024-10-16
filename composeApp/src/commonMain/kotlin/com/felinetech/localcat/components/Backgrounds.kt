package com.felinetech.localcat.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import localcat.composeapp.generated.resources.Res
import localcat.composeapp.generated.resources.cat_empty
import org.jetbrains.compose.resources.painterResource

// 创建一个垂直渐变色
val gradientBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFF018786), Color(0xFFFFFFFF)) // 渐变色从红色到蓝色
)

@Composable
fun ColorBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        ImageBackground()
    }
}

@Composable
fun ImageBackground() {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            // todo 后期再解决这个填满父容器的问题
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 700.dp, max = 800.dp)
                .padding(start = 100.dp, end = 100.dp),
            painter = painterResource(Res.drawable.cat_empty),
            contentDescription = "背景图片"
        )
    }
}
