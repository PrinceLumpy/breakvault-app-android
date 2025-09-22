plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.googleKsp)
    alias(libs.plugins.androidxRoom)
    alias(libs.plugins.kotlin.serialization) // Added Kotlin Serialization plugin

}

android {
    namespace = "com.princelumpy.breakvault" // Updated namespace
    compileSdk = 36 // Keeping compileSdk high is generally fine

    defaultConfig {
        applicationId = "com.princelumpy.breakvault" // CRITICAL: Updated applicationId
        minSdk = 24
        targetSdk = 34 // Updated to a more common stable API for production
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true // CRITICAL: Enabled minification for release
            // isShrinkResources = true // Recommended: Add this to shrink resources
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.navigation.compose) // Corrected from .compose.ui

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.lifecycle.viewmodelKtx)
    implementation(libs.androidx.lifecycle.livedataKtx)

    implementation(libs.kotlinx.serialization.json) // Added Kotlinx Serialization JSON library

    // Test Implementations - Ensure these are not accidentally used in release code
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // Remove androidx.compose.ui, androidx.compose.ui.graphics, androidx.compose.ui.tooling.preview if only for preview
    // Retained for now as they might be used directly, but usually covered by Material3 or BOM
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
}
