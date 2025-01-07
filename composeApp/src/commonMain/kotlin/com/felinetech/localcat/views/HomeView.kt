package com.felinetech.localcat.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.felinetech.localcat.components.ColorBackground
import com.felinetech.localcat.theme.borderColor
import com.felinetech.localcat.utlis.getNames
import com.felinetech.localcat.view_model.HomeViewModel.netWork
import com.felinetech.localcat.view_model.HomeViewModel.updateIpAddress
import com.felinetech.localcat.view_model.MainViewModel
import com.felinetech.localcat.view_model.MainViewModel.receiveState
import com.felinetech.localcat.view_model.MainViewModel.sendState
import com.felinetech.localcat.view_model.MainViewModel.turnFun
import java.util.Locale


@Composable
fun HomePage() {
    val turnState by MainViewModel.turnState.collectAsState()
    ColorBackground()
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = getNames(Locale.getDefault().language).homePageTitle,
            color = Color.White,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = TextUnit(20f, TextUnitType.Sp)
            )
        )
        Surface(
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(start = 5.dp, end = 5.dp)
                .border(1.dp, color = borderColor, shape = RoundedCornerShape(5.dp))
            ,
            color = Color(0x99ffffff)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text(getNames(Locale.getDefault().language).switchTo)
                Text(getNames(Locale.getDefault().language).recipient)
                Switch(checked = turnState, onCheckedChange = {
                    //当进行切换操作时，更改状态
                    turnFun(it)
                }, colors = SwitchDefaults.colors(uncheckedBorderColor = Color(0x00ffffff)))
                Text(getNames(Locale.getDefault().language).sender)
                Text(text = getNames(Locale.getDefault().language).network)
                Text(text = netWork)
            }
        }
        Box(
            modifier = Modifier.weight(1f)
                .fillMaxSize()
        ) {
            if (turnState) {
                Sender(!sendState)
            } else {
                Receiver(!receiveState)
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                updateIpAddress()
                println("生命周期开始===")

            } else if (event == Lifecycle.Event.ON_STOP) {
                println("生命周期结束")
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


}
