import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(compose.components.resources)
            implementation("com.airbnb.android:lottie-compose:4.2.0")
            implementation("com.google.code.gson:gson:2.11.0")
            implementation("com.google.accompanist:accompanist-permissions:0.36.0")
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

            implementation("org.jetbrains.compose.material3:material3-desktop:1.6.11")
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.7.0-alpha07")

            implementation ("com.github.YarikSOffice:lingver:1.3.0")

            implementation("com.google.code.gson:gson:2.11.0")

            runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
            runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")


        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(compose.components.resources)
            // tollie Desktop 用
            implementation("io.github.alexzhirkevich:compottie:2.0.0-rc01")
            implementation("io.github.alexzhirkevich:compottie-dot:2.0.0-rc01")
            implementation("io.github.alexzhirkevich:compottie-network:2.0.0-rc01")
            implementation("io.github.alexzhirkevich:compottie-resources:2.0.0-rc01")

            implementation("com.google.code.gson:gson:2.11.0")



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
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {


//    implementation(libs.androidx.foundation.desktop)
    debugImplementation(compose.uiTooling)

}

compose.desktop {
    application {
        mainClass = "com.felinetech.localcat.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.felinetech.localcat"
            packageVersion = "1.0.0"
        }
    }

}

