package com.felinetech.localcat.view_model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.felinetech.localcat.po.UploadConfigItem
import com.felinetech.localcat.utlis.getNames
import java.text.SimpleDateFormat
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
     * 日期格式化
     */
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")

    // 提示信息
    var showMsg by mutableStateOf(false)
    var msgErr by mutableStateOf("")

    /**
     * 添加规则
     */
    fun addRule(): Boolean {
        if (selectedDirectory.isEmpty()) {
            showMsg = true
            msgErr = "选择的目录为空！"
            return false
        }
        if (selectedOption.isEmpty()) {
            showMsg = true
            msgErr = "选择的类型为空请先选择！"
            return false
        }
        if (currDate.isEmpty()) {
            showMsg = true
            msgErr = "选择的日期为空请先选择！"
            return false
        }
        ruleList.add(UploadConfigItem(ruleList.size + 1, selectedDirectory, selectedOption, dateFormat.parse(currDate)))
        return true
    }

    /**
     * 设置日期
     */
    fun setDate(long: Long?) {
        if (long == null) {
            return
        }
        currDate = dateFormat.format(Date(long))
    }
}
