plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.hoabh.chanko"
    compileSdk {
        version = release(37)
    }

    fun configuredString(name: String, fallback: String = ""): String {
        val value = providers.gradleProperty(name).orElse(fallback).get()
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\""
    }

    defaultConfig {
        applicationId = "com.hoabh.chanko"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", configuredString("CHANKO_GOOGLE_WEB_CLIENT_ID"))
        buildConfigField("String", "TERMS_URL", configuredString("CHANKO_TERMS_URL"))
        buildConfigField("String", "PRIVACY_URL", configuredString("CHANKO_PRIVACY_URL"))
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            manifestPlaceholders["appLabel"] = "Chanko Dev"
            buildConfigField("boolean", "IS_DEV_MOCK", "true")
            buildConfigField("String", "API_BASE_URL", configuredString("CHANKO_API_BASE_URL_DEV", "https://dev-api.chanko.app/api/v1/"))
        }
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".stg"
            manifestPlaceholders["appLabel"] = "Chanko Stg"
            buildConfigField("boolean", "IS_DEV_MOCK", "false")
            buildConfigField("String", "API_BASE_URL", configuredString("CHANKO_API_BASE_URL_STG", "https://staging-api.chanko.app/api/v1/"))
        }
        create("prod") {
            dimension = "environment"
            manifestPlaceholders["appLabel"] = "Chanko"
            buildConfigField("boolean", "IS_DEV_MOCK", "false")
            buildConfigField("String", "API_BASE_URL", configuredString("CHANKO_API_BASE_URL"))
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(project(":core:network"))
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":core:database"))

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Coil
    implementation(libs.coil.compose)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}