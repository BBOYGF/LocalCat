package com.felinetech.localcat.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import localcat.composeapp.generated.resources.Res
import localcat.composeapp.generated.resources.animation_search
import localcat.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.io.File


// 接收动画
@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun ReceiverAnimation() {

    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
//            File("src/commonMain/composeResources/drawable/animation_search.json")
//                .run {
//                    readText(Charsets.UTF_8)
//                }
            Res.readBytes("drawable/animation_search.json").decodeToString()
        )
    }
    val progress by animateLottieCompositionAsState(composition)

    Image(
        painter = rememberLottiePainter(
            composition = composition,
            progress = { progress },
        ),
        modifier = Modifier.width(300.dp)
            .height(300.dp),
        contentDescription = "Lottie animation"
    )
}

// 扫描文件动画
@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun ScanFile() {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
//            File("src/commonMain/composeResources/drawable/search_file.json")
//                .run {
//                    readText(Charsets.UTF_8)
//                }
            Res.readBytes("drawable/search_file.json").decodeToString()
        )
    }
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = Compottie.IterateForever
    )
    Image(
        painter = rememberLottiePainter(
            composition = composition,
            progress = { progress },
        ),
        modifier = Modifier.fillMaxWidth(), contentDescription = "Lottie animation"
    )
}
