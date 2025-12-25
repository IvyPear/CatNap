plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.catnap"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.catnap"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
}

dependencies {
    // MPAndroidChart - ổn định, hỗ trợ AndroidX
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
    // Material Design
    implementation("com.google.android.material:material:1.11.0")

    implementation("com.github.lzyzsd:circleprogress:1.2.1")

    // AndroidX core libraries (từ libs.versions.toml)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

// LOẠI BỎ TRIỆT ĐỂ SUPPORT LIBRARY CŨ ĐỂ TRÁNH DUPLICATE CLASS
configurations.all {
    exclude(group = "com.android.support")
    exclude(group = "android.support")
}