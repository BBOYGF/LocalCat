package com.felinetech.fast_file.enums


import localcat.composeapp.generated.resources.*
import localcat.composeapp.generated.resources.Res
import localcat.composeapp.generated.resources.doc
import localcat.composeapp.generated.resources.img
import localcat.composeapp.generated.resources.video_mp4
import org.jetbrains.compose.resources.DrawableResource

/**
 * 文件类型
 */
enum class FileType(val imgId: DrawableResource, val suffix: String, val showName: String) {
    jpg图片(Res.drawable.img, "jpg", "图片"),
    jpeg图片(Res.drawable.img, "jpeg", "图片"),
    png图片(Res.drawable.img, "png", "图片"),
    mp4视频(Res.drawable.video_mp4, "mp4", "视频"),
    doc文档(Res.drawable.doc, "doc", "文档"),
    APK(Res.drawable.APK, "apk", "安装包"),
    AAB(Res.drawable.APK, "aab", "安装包"),
    DMG(Res.drawable.APK, "dmg", "安装包"),
}