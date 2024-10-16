package com.felinetech.localcat.enums

import localcat.composeapp.generated.resources.Res
import localcat.composeapp.generated.resources.img
import org.jetbrains.compose.resources.DrawableResource

/**
 * 文件类型
 */
enum class FileType(val imgId: DrawableResource, val suffix: String, val showName: String) {
    jpg图片(Res.drawable.img, "jpg", "图片"),
    jpeg图片(Res.drawable.img, "jpeg", "图片"),
    png图片(Res.drawable.img, "png", "图片"),
    mp4视频(Res.drawable.img, "mp4", "视频"),
    doc文档(Res.drawable.img, "doc", "文档"),
}