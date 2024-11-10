package com.felinetech.localcat.views


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.felinetech.localcat.components.ColorBackground
import com.felinetech.localcat.components.RuleItem
import com.felinetech.localcat.enums.FileType
import com.felinetech.localcat.utlis.getNames
import com.felinetech.localcat.view_model.SettingViewModel.currDate
import com.felinetech.localcat.view_model.SettingViewModel.currTime
import com.felinetech.localcat.view_model.SettingViewModel.ruleList
import com.felinetech.localcat.view_model.SettingViewModel.selectedDirectory
import com.felinetech.localcat.view_model.SettingViewModel.selectedOption
import localcat.composeapp.generated.resources.Res
import localcat.composeapp.generated.resources.folder_gray
import org.jetbrains.compose.resources.painterResource
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingView() {
    var showReluDialog by remember { mutableStateOf(false) }

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
                .height(500.dp),
            color = Color(0x99ffffff),
            shape = RoundedCornerShape(5.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(ruleList) { item ->
                        RuleItem(item)
                    }

                }
                Box {
                    // 添加规则按钮
                    Button(onClick = {
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
                .height(200.dp),
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
                        text = getNames(Locale.getDefault().language).saveLocation, modifier = Modifier
                            .height(40.dp)
                            .padding(3.dp)
                    )
                    Text(
                        text = "/", modifier = Modifier
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
                        onClick = { /*TODO*/ },
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
                        text = "/", modifier = Modifier
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
                        onClick = { /*TODO*/ },
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

    var showDatePicker by remember { mutableStateOf(false) }
    val dataPickerState = rememberDatePickerState()

    // 添加规则弹窗
    if (showReluDialog) {
        Dialog(
            onDismissRequest = { showReluDialog = false },
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
                    Text(text = getNames(Locale.getDefault().language).ruleSetting)
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = getNames(Locale.getDefault().language).filterDirectory)
                        Text(
                            text = selectedDirectory, modifier = Modifier
                                .width(100.dp)
                                .border(1.dp, Color.Black, shape = RoundedCornerShape(2.dp))
                        )
                        Icon(
                            painter = painterResource(Res.drawable.folder_gray),
                            contentDescription = "",
                            modifier = Modifier
                                .width(30.dp)
                                .height(30.dp)
                        )
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
                        Text(
                            text = currDate,
                            modifier = Modifier
                                .width(80.dp)
                                .clickable {
                                    showDatePicker = true
                                }, textAlign = TextAlign.Center
                        )
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
                        Button(onClick = { showReluDialog = false }) {
                            Text(text = getNames(Locale.getDefault().language).okText)
                        }
                        Button(onClick = { showReluDialog = false }) {
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
                            currDate = "${dataPickerState.selectedDateMillis}:"
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
            label = { Text("选择选项") },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Filled.Done else Icons.Filled.ArrowDropDown,
                    contentDescription = null
                )
            },
            modifier = Modifier
                .width(130.dp)
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

