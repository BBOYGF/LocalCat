package com.felinetech.localcat.components

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


@Composable
fun MenuButton(
    flag: MutableState<Boolean>,
    img: ImageVector,
    clickButton: () -> Unit
) {
    val darkColor = MaterialTheme.colorScheme.primary
    val secondaryColor= MaterialTheme.colorScheme.secondary
    val backgroundColor = remember { Animatable(darkColor) }
    val foregroundColor = remember { Animatable(secondaryColor) }

    LaunchedEffect(flag.value) {
        backgroundColor.animateTo(
            targetValue = if (flag.value) {
                secondaryColor
            } else {
                darkColor
            },
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )

    }
    LaunchedEffect(flag.value) {
        foregroundColor.animateTo(
            targetValue = if (flag.value) {
                darkColor
            } else {
                secondaryColor
            },
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor.value,
        modifier = Modifier
            .size(40.dp)
    ) {
        IconButton(
            onClick = clickButton,
        ) {
            Icon(
                imageVector = img,
                contentDescription = "Home",
                tint = foregroundColor.value
            )
        }
    }
}