package com.felinetech.localcat.views


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.felinetech.localcat.components.ColorBackground
import com.felinetech.localcat.components.RuleItem
import com.felinetech.localcat.enums.FileType
import com.felinetech.localcat.theme.borderColor
import com.felinetech.localcat.utlis.getNames
import com.felinetech.localcat.view_model.SettingViewModel.addRule
import com.felinetech.localcat.view_model.SettingViewModel.cachePosition
import com.felinetech.localcat.view_model.SettingViewModel.currDate
import com.felinetech.localcat.view_model.SettingViewModel.currTime
import com.felinetech.localcat.view_model.SettingViewModel.defaultValue
import com.felinetech.localcat.view_model.SettingViewModel.deleteConfig
import com.felinetech.localcat.view_model.SettingViewModel.editConfig
import com.felinetech.localcat.view_model.SettingViewModel.getFileName
import com.felinetech.localcat.view_model.SettingViewModel.msgErr
import com.felinetech.localcat.view_model.SettingViewModel.ruleList
import com.felinetech.localcat.view_model.SettingViewModel.saveConfig
import com.felinetech.localcat.view_model.SettingViewModel.savedPosition
import com.felinetech.localcat.view_model.SettingViewModel.selectedDirectory
import com.felinetech.localcat.view_model.SettingViewModel.selectedOption
import com.felinetech.localcat.view_model.SettingViewModel.setDate
import com.felinetech.localcat.view_model.SettingViewModel.showMsg
import com.felinetech.localcat.view_model.SettingViewModel.showReluDialog
import com.felinetech.localcat.view_model.SettingViewModel.updateCacheFile
import com.felinetech.localcat.view_model.SettingViewModel.updateSaveFile
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import localcat.composeapp.generated.resources.Res
import localcat.composeapp.generated.resources.folder_gray
import org.jetbrains.compose.resources.painterResource
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingView() {

    // 文件保存路径
    val launcherSave = rememberDirectoryPickerLauncher(
        title = "Pick a directory",
        initialDirectory = savedPosition,
    ) { directory ->
        println("选择的保存目录是$directory")
        directory?.let {
            updateSaveFile(it.path.toString())
        }
    }
    // 缓存保存位置
    val launcherCache = rememberDirectoryPickerLauncher(
        title = "Pick a directory",
        initialDirectory = cachePosition
    ) { directory ->
        directory?.let {
            updateCacheFile(it.path.toString())
            println("选择的缓存目录是$directory")
        }
    }
    val launcherConfig = rememberDirectoryPickerLauncher(
        title = "Pick a directory",
//        initialDirectory = "c:\\"
    ) { directory ->
        directory?.let {
            selectedDirectory = it.path.toString()
            println("选择的规则目录是$directory")
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var eidt by remember { mutableStateOf(false) }
    val dataPickerState = rememberDatePickerState()
    ColorBackground()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = getNames(Locale.getDefault().language).setting,
            color = Color.White,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = TextUnit(20f, TextUnitType.Sp)
            ),

            )
        Text(
            text = getNames(Locale.getDefault().language).sendSettings,
            style = TextStyle(fontSize = TextUnit(13f, TextUnitType.Sp)),
            modifier = Modifier.fillMaxWidth()
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .border(1.dp, color = borderColor, shape = RoundedCornerShape(5.dp))
            ,
            color = Color(0x99ffffff),
            shape = RoundedCornerShape(5.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(ruleList) { item ->
                        RuleItem(item, {
                            eidt = true
                            editConfig(it)
                        }, {
                            deleteConfig(it)
                        })
                    }

                }
                Box {
                    // 添加规则按钮
                    Button(onClick = {
                        eidt = false
                        defaultValue()
                        showReluDialog = true
                    }) {
                        Text(text = getNames(Locale.getDefault().language).addRules)
                    }
                }
            }


        }
        Text(
            text = getNames(Locale.getDefault().language).receiveSettings,
            style = TextStyle(fontSize = TextUnit(13f, TextUnitType.Sp)),
            modifier = Modifier.fillMaxWidth()
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .border(1.dp, color = borderColor, shape = RoundedCornerShape(5.dp))
            ,
            color = Color(0x99ffffff),
            shape = RoundedCornerShape(5.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .align(alignment = Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getNames(Locale.getDefault().language).saveLocation,
                        modifier = Modifier
                            .height(40.dp)
                            .padding(3.dp)
                    )
                    Text(
                        text = savedPosition, modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .padding(top = 3.dp, bottom = 3.dp)
                            .border(
                                width = 2.dp,
                                color = Color(0xaaffffff),
                                shape = RoundedCornerShape(5.dp)
                            )
                            .background(color = Color(0x99ffffff))
                    )
                    Button(
                        onClick = {
                            launcherSave.launch()
                        },
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.Black,
                            containerColor = Color.White
                        ),
                        modifier = Modifier
                            .height(40.dp)
                            .padding(3.dp)

                    ) {
                        Text(text = getNames(Locale.getDefault().language).setting)
                    }
                }
                Row {
                    Text(
                        text = getNames(Locale.getDefault().language).cacheSave, modifier = Modifier
                            .height(40.dp)
                            .padding(3.dp)
                    )
                    Text(
                        text = cachePosition, modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .padding(top = 3.dp, bottom = 3.dp)
                            .border(
                                width = 2.dp,
                                color = Color(0xaaffffff),
                                shape = RoundedCornerShape(5.dp)
                            )
                            .background(color = Color(0x99ffffff))
                    )
                    Button(
                        onClick = {

                            launcherCache.launch()
                        },
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.Black,
                            containerColor = Color.White
                        ),
                        modifier = Modifier
                            .height(40.dp)
                            .padding(3.dp)

                    ) {
                        Text(text = getNames(Locale.getDefault().language).setting)
                    }
                }
            }
        }

    }
    var showTimePicker by remember { mutableStateOf(false) }
    val currentTime = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )


    // 添加规则弹窗
    if (showReluDialog) {

        Dialog(
            onDismissRequest = {
                showReluDialog = false
            },
        ) {
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .height(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    Text(
                        text = getNames(Locale.getDefault().language).ruleSetting,
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = getNames(Locale.getDefault().language).filterDirectory)
                        Text(
                            text = getFileName(selectedDirectory), modifier = Modifier
                                .width(100.dp)
                                .border(1.dp, Color.Black, shape = RoundedCornerShape(2.dp))
                        )
                        // 选择目录被点击
                        IconButton(onClick = {
                            launcherConfig.launch()

                        }) {
                            Icon(
                                painter = painterResource(Res.drawable.folder_gray),
                                contentDescription = "",
                                modifier = Modifier
                                    .width(30.dp)
                                    .height(30.dp)
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = getNames(Locale.getDefault().language).fileExtension)

                        val suffixList = remember {
                            mutableStateOf(
                                FileType.entries.map { "*." + it.suffix }.toList()
                            )
                        }
                        ComboBox(suffixList)
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = getNames(Locale.getDefault().language).afterWhatTime)
                        // 什么日期后
                        Text(
                            text = currDate,
                            modifier = Modifier
                                .width(90.dp)
                                .clickable {
                                    showDatePicker = true
                                }, textAlign = TextAlign.Center
                        )
                        // 什么时间后
                        Text(
                            text = currTime,
                            modifier = Modifier
                                .width(80.dp)
                                .clickable {
                                    showTimePicker = true
                                }, textAlign = TextAlign.Center
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp), horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                // 如果有一个没选就提醒
                                if (eidt) {
                                    showReluDialog = !saveConfig()
                                } else {
                                    showReluDialog = !addRule()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = getNames(Locale.getDefault().language).okText)
                        }
                        Button(
                            onClick = { showReluDialog = false },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = getNames(Locale.getDefault().language).cancelButton)
                        }
                    }

                }

            }

        }
    }
    // 显示时间
    if (showTimePicker) {
        Dialog(
            onDismissRequest = { showTimePicker = false },
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) {
                Column() {
                    TimePicker(
                        state = timePickerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp), horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = {
                            showTimePicker = false
                            currTime = "${timePickerState.hour}:${timePickerState.minute}"
                        }) {
                            Text(text = getNames(Locale.getDefault().language).okText)
                        }
                        Button(onClick = { showTimePicker = false }) {
                            Text(text = getNames(Locale.getDefault().language).cancelButton)
                        }
                    }
                }
            }

        }
    }
    // 显示日期
    if (showDatePicker) {
        Dialog(
            onDismissRequest = { showDatePicker = false },
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp)
            ) {
                Column() {
                    DatePicker(
                        state = dataPickerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(460.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp), horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = {
                            showDatePicker = false
                            setDate(dataPickerState.selectedDateMillis)
                        }) {
                            Text(text = getNames(Locale.getDefault().language).okText)
                        }
                        Button(onClick = { showDatePicker = false }) {
                            Text(text = getNames(Locale.getDefault().language).cancelButton)
                        }
                    }
                }

            }
        }
    }

    if (showMsg) {
        Dialog(
            onDismissRequest = { showMsg = false },
        ) {
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .height(300.dp)

            ) {
                Column(
                    modifier = Modifier
                        .width(350.dp)
                        .height(350.dp)
                        .background(Color.White),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(0.2f), verticalAlignment = Alignment.CenterVertically

                    ) {
                        Text(
                            text = "提示", modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = TextUnit(25f, TextUnitType.Sp),
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(0.6f), verticalAlignment = Alignment.CenterVertically

                    ) {
                        Text(
                            text = msgErr, modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = TextUnit(20f, TextUnitType.Sp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Button(modifier = Modifier.fillMaxWidth().padding(5.dp),
                            shape = RoundedCornerShape(2.dp),
                            onClick = {
                                showMsg = false
                            }) {
                            Text(text = getNames(Locale.getDefault().language).okText)
                        }
                    }
                }
            }
        }
    }

    // 设置生命周期
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_CREATE) {
                println("生命创建")
            } else if (event == Lifecycle.Event.ON_START) {
                println("生命周期开始")
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComboBox(items: MutableState<List<String>>) {
    var expanded by remember { mutableStateOf(false) }
    //  用于实现下拉菜单
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        // 显示当前选择的选项
        TextField(
            readOnly = true,
            value = selectedOption,
            onValueChange = {},
            label = { Text("选择类型") },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Filled.Done else Icons.Filled.ArrowDropDown,
                    contentDescription = null
                )
            },
            modifier = Modifier
                .width(140.dp)
                .menuAnchor() // 使菜单与文本框对齐
            ,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        // 下拉菜单
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            for (text in items.value) {
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        selectedOption = text// 更新选中的选项
                        expanded = false // 关闭菜单
                    }
                )
            }

        }
    }
}

