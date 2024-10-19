package com.felinetech.localcat.pojo

// commonMain/src/commonMain/kotlin/Localization.kt

data class LocalizationData(val greeting: String)

expect class LocalizationLoader {
    fun loadLocalization(language: String): LocalizationData
}
