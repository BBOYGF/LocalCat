package com.felinetech.localcat

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform