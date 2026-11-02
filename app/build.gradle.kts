plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.final_for_ind"
    compileSdk = 37 // Standardized for stability

    defaultConfig {
        applicationId = "com.example.final_for_ind"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ✅ NEW — Server config
        buildConfigField("String", "BASE_URL", "\"http://192.168.18.57:8090/\"")
        buildConfigField("String", "WS_BASE_URL", "\"ws://192.168.18.57:8090/\"")

        // ✅ NEW — Agora App ID (Voice Call ke liye)
        buildConfigField("String", "AGORA_APP_ID", "\"56000a15a97e4ac1af999850b86435ac\"")
    }

    buildTypes {
        debug {
            // ✅ NEW — Debug ke liye (local testing)
            buildConfigField("String", "BASE_URL", "\"http://192.168.18.57:8090/\"")
            buildConfigField("String", "WS_BASE_URL", "\"ws://192.168.18.57:8090/\"")
        }
        release {
            // ⚠️ Production pe ye change karo (domain ready hone pe)
            buildConfigField("String", "BASE_URL", "\"https://api.yourdomain.com/\"")
            buildConfigField("String", "WS_BASE_URL", "\"wss://api.yourdomain.com/\"")

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

    buildFeatures {
        compose = true
        buildConfig = true         // ✅ NEW — REQUIRED for BuildConfig
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.remote.creation.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation("androidx.compose.material:material-icons-extended:1.7.6")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.agora.rtc:voice-sdk:4.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.firebase:firebase-analytics")
}