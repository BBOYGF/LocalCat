package com.felinetech.fast_file

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform