package com.felinetech.fast_file.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.ExperimentalMaterialApi
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.felinetech.fast_file.utlis.getNames
import com.felinetech.fast_file.view_model.AboutViewModel.pay
import com.felinetech.fast_file.view_model.AboutViewModel.payTypeItemList
import com.felinetech.fast_file.view_model.MainViewModel.bottomSheetVisible
import localcat.composeapp.generated.resources.Res
import localcat.composeapp.generated.resources.cat_empty
import org.jetbrains.compose.resources.painterResource
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class)
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
            offsetY.animateTo(targetValue = 450f, animationSpec = tween(500))
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
                    .height(450.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Local Cat",
                        color = MaterialTheme.colorScheme.tertiary,
                        style = TextStyle(fontSize = 35.sp),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp, start = 20.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(300.dp)
                            .padding(start = 20.dp, end = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.background(
                                color = Color.White,
                                shape = RoundedCornerShape(20.dp)
                            )
                                .fillMaxWidth().height(300.dp).padding(top = 30.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            for (payItem in payTypeItemList) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Image(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clickable {
                                                payTypeItemList.forEach { payItem ->
                                                    payItem.selected.value = false
                                                }
                                                payItem.selected.value = true
                                            },
                                        painter = painterResource(payItem.icon),
                                        contentDescription = "logo"
                                    )
                                    Text(
                                        text = payItem.title,
                                        modifier = Modifier.width(150.dp).padding(start = 20.dp)
                                            .clickable {
                                                payTypeItemList.forEach { payItem ->
                                                    payItem.selected.value = false
                                                }
                                                payItem.selected.value = true
                                            },
                                        style = TextStyle(
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        ),

                                        )
                                    Checkbox(
                                        checked = payItem.selected.value,
                                        onCheckedChange = {
                                            payTypeItemList.forEach { payItem ->
                                                payItem.selected.value = false
                                            }
                                            payItem.selected.value = true
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.tertiary),
                                        modifier = Modifier.width(70.dp)
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-150).dp) // 向上偏移 20 像素
                        ) {
                            Image(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(80.dp)
                                    .align(Alignment.TopEnd) // 靠右对齐
                                ,
                                painter = painterResource(Res.drawable.cat_empty),
                                contentDescription = "logo"
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        bottomSheetVisible = false
                        // 支付
                        pay()

                    },
                    modifier = Modifier.width(100.dp).height(50.dp).padding(bottom = 15.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text(
                        text = getNames(Locale.getDefault().language).okText,
                        color = MaterialTheme.colorScheme.primary,
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    )
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