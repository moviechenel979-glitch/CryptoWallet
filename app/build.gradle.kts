plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "com.cryptovault"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.cryptovault"
        minSdk = 26
        targetSdk = 35
        versionCode = 22
        versionName = "1.0.1"
        multiDexEnabled = true
        buildConfigField("boolean", "PRODUCTION", "true")
    }
    buildFeatures { compose = true; buildConfig = true }

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
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (hasSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

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
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.8")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.trustwallet:wallet-core:4.7.2")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.1.0")
}
