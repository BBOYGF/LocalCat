package com.felinetech.localcat.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.felinetech.localcat.components.ColorBackground
import localcat.composeapp.generated.resources.Res
import localcat.composeapp.generated.resources.money
import org.jetbrains.compose.resources.painterResource


@Composable
fun About() {
    var openDialog by remember { mutableStateOf(false) }

    ColorBackground()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "设置",
            color = Color.White,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = TextUnit(20f, TextUnitType.Sp)
            )
        )
        Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = "关于",
            modifier = Modifier
                .width(200.dp)
                .height(200.dp)
                .offset(y = -30.dp), tint = MaterialTheme.colorScheme.surfaceTint
        )
        Text(
            text = "         FastCat 快猫 是一款非常方便的设备间传输数据的软件，它可帮助用户轻松将多个设备中的文件自动传输到电脑端，该软件分为Android移动端和Win桌面端，通过局域网可将多个手机内容随时备份到桌面指定目录，软件支持断点续传可将大文件按照1M大小分块然后再上传到到电脑端，上传中断后下次可继续传输，该软件还支持自动发现新文件并上传功能，用户可以设置好要上传目录的过滤条件，软件会自动扫描当前目录是否有新的文件产生如果有将会自动上传到电脑端。该软件适用于对经常需要将手机数据上传到电脑端处理的用户，它可帮助您减少大量琐碎工作，让您将注意力集中到更多更重要的事情上。",
            style = TextStyle(
                fontSize = TextUnit(12f, TextUnitType.Sp),
                color = Color.White,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.offset(y = -30.dp)
        )
        Button(onClick = {

        }) {
            Text(
                text = "下载Android版本",
                modifier = Modifier.width(150.dp),
                textAlign = TextAlign.Center,

                )
        }
        Button(onClick = {

        }) {
            Text(
                text = "问题反馈",
                modifier = Modifier.width(150.dp),
                textAlign = TextAlign.Center
            )
        }

        Button(onClick = {
            openDialog = true
        }) {
            Text(
                text = "输入注册码成为会员",
                modifier = Modifier.width(150.dp),
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = "本软件及其相关",
            color = MaterialTheme.colorScheme.tertiary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable {

            }
        )
    }

    // 弹窗
    if (openDialog) {
        var text by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { openDialog = false }) {
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .height(300.dp), colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                        .background(Color.White), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("使用订单号注册码", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(20.dp))
                    Icon(
                        painter = painterResource(Res.drawable.money),
                        contentDescription = "图片", modifier = Modifier
                            .height(80.dp)
                            .width(80.dp), tint = MaterialTheme.colorScheme.surfaceTint
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("请输入注册码：")
                        BasicTextField(
                            value = text,
                            onValueChange = {
                                text = it
                                println("修改后的text是：$text")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(25.dp)
                                .padding(start = 10.dp)
                                .background(Color(0xffeeeeee)),
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                            ),
                            singleLine = true,
                        )
                        println("重新构造")
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = { openDialog = false }) {
                            Text("注册")
                        }
                        Button(onClick = { openDialog = false }) {
                            Text(text = "取消")
                        }
                    }
                }
            }
        }
    }

}