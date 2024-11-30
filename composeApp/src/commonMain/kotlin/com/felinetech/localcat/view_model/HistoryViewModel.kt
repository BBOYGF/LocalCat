package com.felinetech.localcat.view_model

import androidx.compose.runtime.mutableStateListOf
import com.felinetech.localcat.dao.FileChunkDao
import com.felinetech.localcat.dao.FileEntityDao
import com.felinetech.localcat.enums.UploadState
import com.felinetech.localcat.pojo.FileItemVo
import com.felinetech.localcat.utlis.filePoToFileVo
import com.felinetech.localcat.utlis.getDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 历史记录
 */
object HistoryViewModel {

    /**
     *  上传的文件列表
     */
    val uploadedFileList = mutableStateListOf<FileItemVo>()

    /**
     * 已下载的文件列表
     */
    val downloadedFileList = mutableStateListOf<FileItemVo>()

    /**
     * io协程
     */
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val uiScope = CoroutineScope(Dispatchers.Main)

    /**
     * 数据库
     */
    private val fileEntityDao: FileEntityDao
    private var fileChunkDao: FileChunkDao

    /**
     * 初始化
     */
    init {
        val database = getDatabase()
        fileEntityDao = database.getFileEntityDao()
        fileChunkDao = database.getFileChunkEntityDao()
        defaultData()
    }

    /**
     * 默认数据
     */
    private fun defaultData() {
        ioScope.launch {
            val allFile = fileEntityDao.getAllFiles()
            val uploadFileList = allFile.filter { it.uploadState == UploadState.已上传 }.map {
                filePoToFileVo(it)
            }.toList()

            val downFileList = allFile.filter { it.uploadState == UploadState.已下载 }.map {
                filePoToFileVo(it)
            }.toList()

            uiScope.launch {
                uploadedFileList.addAll(uploadFileList)
                downloadedFileList.addAll(downFileList)
            }
        }
    }


}