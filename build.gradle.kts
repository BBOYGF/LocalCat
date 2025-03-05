plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false

//    id("org.jetbrains.compose-hot-reload") version "v1.0.0-dev.33.5"
//    id("org.jetbrains.compose.hot-reload") version "1.0.0-dev-65"
    id("org.jetbrains.compose.hot-reload") version "1.0.0-alpha01"
}