package com.felinetech.localcat.utlis

import androidx.compose.runtime.Composable
import com.felinetech.localcat.database.Database
import java.io.File

expect fun getLocalIp(): String


expect fun getDatabase(): Database

expect fun getFileByDialog(): File?