import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.skie)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget("11")
        }
    }
    
    listOf(
//        iosX64(), // Exclude ios x64 target
        iosArm64(),
//        iosSimulatorArm64() // Exclude ios simulator target
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = false
        }
    }

    sourceSets {
        all {
            // From KMP-ObservableViewModel
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.core)
            implementation(libs.room.runtime)

            implementation(libs.sqlite.bundled)
            implementation(libs.datastore.preferences)

            api(libs.kmp.observable.viewmodel)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.android)
            implementation(libs.datastore.preferences)
            implementation(libs.play.services.location)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.datastore.preferences)

            // Only one of these 3 should be enabled to be able to compile for iOS (strange)
//            implementation(libs.room.common.iossimulatorarm64)
//            implementation(libs.room.common.iosx64)
            implementation(libs.room.common.iosarm64)

            implementation(libs.sqlite.bundled)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }

    configurations.implementation {
        exclude(group = "com.intellij", module = "annotations")
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.room.compiler)
    add("kspAndroid", libs.room.compiler)
//    add("kspIosSimulatorArm64", libs.room.compiler) // Exclude ios simulator target
//    add("kspIosX64", libs.room.compiler) // Exclude ios x64 target
    add("kspIosArm64", libs.room.compiler)
}

android {
    namespace = "bav.petus"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}
