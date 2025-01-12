package com.felinetech.localcat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.felinetech.localcat.view_model.MainViewModel.bottomSheetVisible

@Composable
fun BottomSheetPar() {

    val sheetState = rememberModalBottomSheetState(
        if (bottomSheetVisible) ModalBottomSheetValue.Expanded else ModalBottomSheetValue.Hidden
    )

    // 自定义动画：先快后慢
    val offsetY = remember { Animatable(0f) } // 初始位置在屏幕下方

    // 监听 ViewModel 中的状态变化
    LaunchedEffect(bottomSheetVisible) {
        if (bottomSheetVisible) {
            sheetState.show()
            offsetY.animateTo(targetValue = 0f, animationSpec = tween(500))
        } else {
            sheetState.hide()
            offsetY.animateTo(targetValue = 300f, animationSpec = tween(500))
        }
    }

    // 监听 sheetState 的变化并更新 ViewModel
    LaunchedEffect(sheetState.isVisible) {
        bottomSheetVisible = sheetState.isVisible
    }

    ModalBottomSheetLayout(

        sheetState = sheetState,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(color = MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
                ,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("This is a bottom sheet")
                Button(onClick = { bottomSheetVisible = false }) {
                    Text("Close")
                }
            }
        },
        scrimColor = Color(0x00000000),
        sheetBackgroundColor = Color(0x00000000),
        sheetElevation = 5.dp,
        modifier = Modifier
            .offset(y = (offsetY.value).dp)
    ) {

    }

}