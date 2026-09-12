plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.cryptovault"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.cryptovault"
        minSdk = 26
        targetSdk = 35
        versionCode = 20
        versionName = "1.0.0-rc20"
    }
    buildFeatures { compose = true; buildConfig = true }

    // Release signing is sourced from environment variables / gradle properties so no
    // secrets ever need to be committed to the repo. In CI these are populated from
    // GitHub Actions secrets (see .github/workflows/build.yml). Locally, set them in
    // your shell or in ~/.gradle/gradle.properties (never in this file or local.properties
    // if that file is committed).
    val storeFilePath = providers.gradleProperty("RELEASE_STORE_FILE").orElse(System.getenv("RELEASE_STORE_FILE") ?: "").get()
    val storePwd = providers.gradleProperty("RELEASE_STORE_PASSWORD").orElse(System.getenv("RELEASE_STORE_PASSWORD") ?: "").get()
    val keyAliasVal = providers.gradleProperty("RELEASE_KEY_ALIAS").orElse(System.getenv("RELEASE_KEY_ALIAS") ?: "").get()
    val keyPwd = providers.gradleProperty("RELEASE_KEY_PASSWORD").orElse(System.getenv("RELEASE_KEY_PASSWORD") ?: "").get()
    val hasSigningConfig = storeFilePath.isNotBlank() && storePwd.isNotBlank() && keyAliasVal.isNotBlank() && keyPwd.isNotBlank()

    if (hasSigningConfig) {
        signingConfigs {
            create("release") {
                storeFile = file(storeFilePath)
                storePassword = storePwd
                keyAlias = keyAliasVal
                keyPassword = keyPwd
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (hasSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
            // If hasSigningConfig is false, the release APK builds unsigned — you (or
            // apksigner manually) must sign it before it can be installed or published.
        }
    }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.15" }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.compose.ui:ui:1.7.8")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.8")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.trustwallet:wallet-core:4.7.2")
}
