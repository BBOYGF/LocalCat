package com.felinetech.fast_file.services

import co.touchlab.kermit.Logger
import com.felinetech.fast_file.Constants.HEART_BEAT_SERVER_POST
import com.felinetech.fast_file.enums.UploadState
import com.felinetech.fast_file.interfaces.UploadService
import com.felinetech.fast_file.view_model.HistoryViewModel.uploadedFileList
import com.felinetech.fast_file.view_model.HomeViewModel
import com.felinetech.fast_file.view_model.HomeViewModel.connectedIpAdd
import com.felinetech.fast_file.view_model.HomeViewModel.fileEntityDao
import com.felinetech.fast_file.view_model.HomeViewModel.msg
import com.felinetech.fast_file.view_model.HomeViewModel.showMsg
import com.felinetech.fast_file.view_model.HomeViewModel.toBeUploadFileList
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.onUpload
import io.ktor.client.plugins.timeout
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLEncoder.encode

class DesktopUploadService: UploadService {
    /**
     * io协程
     */
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private var logger: Logger = Logger.withTag("AndroidUploadService")
    /**
     * 请求客户端
     */
    private var client: HttpClient

    init {
        client = HttpClient() {
            install(ContentNegotiation) {
                gson()
            }
            install(HttpTimeout)
        }
    }

    override fun startUpload() {
        ioScope.launch {
            val fileItemList = toBeUploadFileList.toList()
            try {
                for (fileItemVo in fileItemList) {
                    // 上传数据
                    val response = client.post(
                        "http://$connectedIpAdd:$HEART_BEAT_SERVER_POST/upload/${
                            encode(
                                fileItemVo.fileName,
                                Charsets.UTF_8
                            )
                        }"
                    ) {
                        timeout {
                            requestTimeoutMillis = 100 * 60 * 1000
                        }
                        val total = File(fileItemVo.fileFillName).length()
                        headers {
                            append(HttpHeaders.ContentLength, total.toString())
                        }
                        setBody(
                            File(fileItemVo.fileFillName).inputStream()
                        )
                        contentType(ContentType.Application.OctetStream)
                        onUpload { bytesSentTotal, contentLength ->
                            var t = contentLength
                            if (contentLength == null) {
                                t = total
                            }
                            println("Sent $bytesSentTotal bytes from $t ${bytesSentTotal.toDouble() / t!!.toDouble()}")
                            val progress =
                                (bytesSentTotal.toDouble() / t.toDouble() * 100).toInt()
                            toBeUploadFileList.indexOfFirst { vo -> vo.fileId == fileItemVo.fileId }
                                .takeIf { it != -1 }?.let {
                                    val itemVo = toBeUploadFileList[it]
                                    toBeUploadFileList[it] = itemVo.copy(percent = progress)
                                }
                            if (!HomeViewModel.startUpload) {
                                cancel()
                                return@onUpload
                            }
                        }
                    }

                    if (response.status == HttpStatusCode.OK) {
                        val responseStr = response.body<String>()
                        println("客户端上传结果：$responseStr")
                        // 下载结束后
                        toBeUploadFileList.indexOfFirst { itemVo -> itemVo.fileId == fileItemVo.fileId }
                            .takeIf { it != -1 }?.let { index ->
                                val element = toBeUploadFileList[index]
                                toBeUploadFileList.removeAt(index)
                                uploadedFileList.add(element)
                                val fileItemPo = fileEntityDao.getFileById(element.fileId)
                                fileItemPo?.let {
                                    it.uploadState = UploadState.已上传
                                    fileEntityDao.update(it)
                                }
                            }
                        // 上传成功
                    } else {
                        showMsg = true
                        msg = "上传结束！"
                        HomeViewModel.startUpload = false
                    }
                }
            } catch (e: Exception) {
                logger.e("上传异常：", e);
            }
            showMsg = true
            msg = "上传结束！"
            HomeViewModel.startUpload = false
        }
    }

    override fun stopUpload() {
        HomeViewModel.startUpload = false
    }
}