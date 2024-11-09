package com.felinetech.localcat.view_model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.felinetech.localcat.po.UploadConfigItem
import com.felinetech.localcat.utlis.getNames
import java.util.*

object SettingViewModel {
    /**
     * 当前打开日期
     */
    var currDate by mutableStateOf(getNames(Locale.getDefault().language).date)
    var currTime by mutableStateOf(getNames(Locale.getDefault().language).time)

    /**
     * 默认选项
     */
    var selectedOption by mutableStateOf("")

    /**
     * 当前选中目录
     */
    var selectedDirectory by mutableStateOf("")

    /**
     * 规则列表
     */
    var ruleList = mutableStateListOf<UploadConfigItem>()

    /**
     * 添加规则
     */
    fun addRule() {

    }
}
