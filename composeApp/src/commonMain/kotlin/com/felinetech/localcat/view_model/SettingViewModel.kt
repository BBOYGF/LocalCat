package com.felinetech.localcat.view_model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.felinetech.localcat.dao.UploadConfigDao
import com.felinetech.localcat.database.Database
import com.felinetech.localcat.po.UploadConfigItem
import com.felinetech.localcat.utlis.getDatabase
import com.felinetech.localcat.utlis.getNames
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
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
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd|hh:mm")

    // 提示信息
    var showMsg by mutableStateOf(false)
    var msgErr by mutableStateOf("")

    lateinit var uploadConfigDao: UploadConfigDao
    val defaultScope = CoroutineScope(Dispatchers.Default)

    init {
        val database: Database = getDatabase()
        uploadConfigDao = database.getUploadConfigItemDao()
        defaultScope.launch {
            val uploadList = uploadConfigDao.getAllUploadCon()
            ruleList.addAll(uploadList)
        }
    }

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
        if (currDate.isEmpty() || currDate == getNames(Locale.getDefault().language).date) {
            showMsg = true
            msgErr = "选择的日期为空请先选择！"
            return false
        }
        if (currTime.isEmpty() || currTime == getNames(Locale.getDefault().language).time) {
            showMsg = true
            msgErr = "选择的时间为空请先选择！"
            return false
        }
        val date = dateTimeFormat.parse("${currDate}|${currTime}")
        val uploadConfig = UploadConfigItem(
            ruleList.size + 1,
            selectedDirectory,
            selectedOption,
            date
        )



        ruleList.add(uploadConfig)
        defaultScope.launch {
            uploadConfigDao.insertUploadCon(uploadConfig)
        }
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

    /**
     * 获取文件名
     */
    fun getFileName(filePath: String): String {
        if (org.apache.commons.lang3.StringUtils.isEmpty(filePath)) {
            return ""
        }
        return File(filePath).name
    }
}
