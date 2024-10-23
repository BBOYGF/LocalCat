package com.felinetech.localcat.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.felinetech.localcat.components.ColorBackground
import com.felinetech.localcat.utlis.getNames
import com.felinetech.localcat.view_model.MainViewModel


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
            text = getNames("ch").homePageTitle,
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
                .padding(start = 5.dp, end = 5.dp),
            color = Color(0x99ffffff)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text("切换为：")
                Text("接收者")
                Switch(checked = turnState, onCheckedChange = {
                    //当进行切换操作时，更改状态
                    MainViewModel.turnFun(it)
                }, colors = SwitchDefaults.colors(uncheckedBorderColor = Color(0x00ffffff)))
                Text("发送者")
                Text(text = "网络:")
                Text(text = "404_5")
            }
        }
        if (turnState) {
            Sender(!MainViewModel.sendState)
        } else {
            Receiver(!MainViewModel.receiveState)
        }

    }
}

