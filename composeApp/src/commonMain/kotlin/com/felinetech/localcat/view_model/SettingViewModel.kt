package com.felinetech.localcat.view_model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.felinetech.localcat.Constants.CACHE_FILE
import com.felinetech.localcat.Constants.SAVE_FILE
import com.felinetech.localcat.dao.UploadConfigDao
import com.felinetech.localcat.database.Database
import com.felinetech.localcat.po.UploadConfigItem
import com.felinetech.localcat.utlis.Settings
import com.felinetech.localcat.utlis.createAppDir
import com.felinetech.localcat.utlis.createSettings
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
     * 当前id
     */
    var rId: Int? = null

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

    var showReluDialog by mutableStateOf(false)

    /**
     * 日期格式化
     */
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd|hh:mm")

    /**
     * 提示信息
     */
    var showMsg by mutableStateOf(false)
    var msgErr by mutableStateOf("")

    private var uploadConfigDao: UploadConfigDao
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val uiScope = CoroutineScope(Dispatchers.Main)

    /**
     * 保存位置
     */
    var savedPosition by mutableStateOf("./file")


    /**
     * 缓存位置
     */
    var cachePosition by mutableStateOf("./cache")

    /**
     * 配置工具
     */
    private var settings: Settings? = null


    init {
        val database: Database = getDatabase()
        uploadConfigDao = database.getUploadConfigItemDao()
        // 设置默认文件保存目录
        val localCatFile = createAppDir("local_cat")
        val configFile = File(localCatFile, "config")
        if (!configFile.exists()) {
            configFile.mkdir()
        }
        settings = createSettings()
        var savePath = settings?.getSetting(SAVE_FILE)
        if (savePath == null) {
            val saveFile = File(localCatFile, "save")
            if (!saveFile.exists()) {
                saveFile.mkdirs()
            }
            savePath = saveFile.absolutePath
            settings?.saveSetting(SAVE_FILE, savePath)
        }
        savedPosition = savePath!!

        // 设置缓存保存目录
        var cachePath = settings?.getSetting(CACHE_FILE)
        if (cachePath == null) {
            val cacheFile = File(localCatFile, "cache")
            if (!cacheFile.exists()) {
                cacheFile.mkdirs()
            }
            cachePath = cacheFile.absolutePath
            settings?.saveSetting(CACHE_FILE, cachePath)
        }
        cachePosition = cachePath!!
        defaultData()
    }

    fun defaultData() {
        ioScope.launch {
            val uploadList = uploadConfigDao.getAllUploadCon()
            uiScope.launch {
                ruleList.addAll(uploadList)
            }
        }
    }

    /**
     * 添加规则
     */
    fun addRule(): Boolean {
        val uploadConfig = UploadConfigItem(null, "", "", Date())
        if (!setupConfig(uploadConfig)) {
            return false
        }
        ruleList.add(uploadConfig)
        ioScope.launch {
            uploadConfigDao.insertUploadCon(uploadConfig)
            rId = uploadConfig.id
        }
        return true
    }

    private fun setupConfig(uploadConfig: UploadConfigItem): Boolean {
        if (selectedDirectory.isEmpty()) {
            showMsg = true
            msgErr = "目录不能为空！"
            return false
        }
        if (selectedOption.isEmpty()) {
            showMsg = true
            msgErr = "类型不能为空！"
            return false
        }
        if (currDate.isEmpty() || currDate == getNames(Locale.getDefault().language).date) {
            showMsg = true
            msgErr = "日期不能为空！"
            return false
        }
        if (currTime.isEmpty() || currTime == getNames(Locale.getDefault().language).time) {
            showMsg = true
            msgErr = "时间不能为空！"
            return false
        }
        val date = dateTimeFormat.parse("${currDate}|${currTime}")
        uploadConfig.apply {
            listeningDir = selectedDirectory
            matchingRule = selectedOption
            startDate = date
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

    /**
     * 编辑规则
     */
    fun editConfig(item: UploadConfigItem) {
        val dataStr = dateTimeFormat.format(item.startDate)
        val split = dataStr.split("|")
        currDate = split[0]
        currTime = split[1]
        selectedOption = item.matchingRule
        selectedDirectory = item.listeningDir
        showReluDialog = true
        rId = item.id
    }

    /**
     * 保存item
     */
    fun saveConfig(): Boolean {
        val uploadConfig = ruleList.first { it.id == rId }
        if (!setupConfig(uploadConfig)) {
            return false
        }
        ioScope.launch {
            uploadConfigDao.updateUploadCon(uploadConfig)
        }
        return true
    }

    /**
     * 删除规则
     */
    fun deleteConfig(item: UploadConfigItem) {
        ioScope.launch {
            uploadConfigDao.deleteUploadCon(item)
        }
        ruleList.remove(item)
    }

    /**
     * 默认值
     */
    fun defaultValue() {
        rId = null
        selectedOption = ""
        selectedDirectory = ""
        currDate = getNames(Locale.getDefault().language).date
        currTime = getNames(Locale.getDefault().language).time
    }

    /**
     * 更新文件保存路径
     */
    fun updateSaveFile(path: String) {
        settings?.saveSetting(SAVE_FILE, path)
        savedPosition = path
    }

    /**
     * 更新缓存文件保存路径
     */
    fun updateCacheFile(path: String) {
        settings?.saveSetting(CACHE_FILE, path)
        cachePosition = path
    }

}
