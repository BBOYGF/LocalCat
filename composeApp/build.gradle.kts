import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)

    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop")
//    macosX64("macos") // macOS 平台

    sourceSets {
        val desktopMain by getting
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(compose.components.resources)
            implementation("com.airbnb.android:lottie-compose:4.2.0")
            implementation("com.google.code.gson:gson:2.11.0")
            implementation("com.google.accompanist:accompanist-permissions:0.36.0")
            implementation ("org.slf4j:slf4j-android:1.7.36")
            runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
            runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)

            implementation("org.jetbrains.compose.material3:material3:1.7.3")

            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.7.0-alpha07")
            implementation("com.github.YarikSOffice:lingver:1.3.0")
            implementation("com.google.code.gson:gson:2.11.0")
            // 日志
            implementation ("org.slf4j:slf4j-api:2.0.3") // 确保使用最新的 SLF4J API


            // 工具类
            implementation("org.apache.commons:commons-lang3:3.15.0")
            implementation ("com.blankj:utilcodex:1.31.1")

            // 网络ktor
            implementation("io.ktor:ktor-client-cio:3.0.2")
            implementation("io.ktor:ktor-client-core-jvm:3.0.2")
            implementation("io.ktor:ktor-client-content-negotiation-jvm:3.0.2")

            implementation("io.ktor:ktor-serialization-gson-jvm:3.0.2")
            implementation("io.ktor:ktor-server-content-negotiation-jvm:3.0.2")

            implementation("io.ktor:ktor-server-netty:3.0.2") // netty 服务
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(compose.components.resources)
            runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.9.0")
            // tollie Desktop 用
            implementation("io.github.alexzhirkevich:compottie:2.0.0-rc01")
            implementation("io.github.alexzhirkevich:compottie-dot:2.0.0-rc01")
            implementation("io.github.alexzhirkevich:compottie-network:2.0.0-rc01")
            implementation("io.github.alexzhirkevich:compottie-resources:2.0.0-rc01")
            implementation ("ch.qos.logback:logback-classic:1.4.5") // 使用最新的 Logback 版本
            implementation("com.google.code.gson:gson:2.11.0")
        }


        // Adds common test dependencies
        commonTest.dependencies {
            implementation(kotlin("test"))
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }

    }

}

android {
    namespace = "com.felinetech.localcat"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.felinetech.localcat"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/*"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {


    debugImplementation(compose.uiTooling)

}

compose.desktop {
    application {
        mainClass = "com.felinetech.localcat.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "localcat"
            packageVersion = "1.0.0"
        }
    }

}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    ksp(libs.room.compiler)
}