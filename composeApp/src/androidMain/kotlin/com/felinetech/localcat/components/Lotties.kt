package com.felinetech.localcat.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.felinetech.localcat.R
import org.jetbrains.compose.resources.ExperimentalResourceApi


@Composable
actual fun ReceiverAnimation() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation_search))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = Int.MAX_VALUE,
        speed = 2f
    )
    LottieAnimation(composition = composition, progress = progress)
}

// 扫描文件动画
@Composable
actual fun ScanFile() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.search_file))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = Int.MAX_VALUE,
        speed = 2f
    )
    LottieAnimation(
        composition = composition,
        progress = progress,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
actual fun WaitingAnimation() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.search_file))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = Int.MAX_VALUE,
        speed = 2f
    )
    LottieAnimation(
        composition = composition,
        progress = progress,
        modifier = Modifier.fillMaxWidth()
    )
}