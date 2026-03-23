//val jvmTar:

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.kotlin.compose)
}
//val sourceCompatibility by extra(JavaVersion.VERSION_17)

android {
    namespace = "com.example.minimal.adhan"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.minimal.adhan"
        minSdk = 26
//        targetSdkVersion(rootProject.extra["36"] as Int) // Android 8.0 (Good baseline for modern widgets)
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions{
        jvmTarget = "17"
    }


//    compileOptions {
////        jvmTarget = "1.8"
//    }

    buildFeatures {
        compose = true
    }

    lint {
        baseline = file("lint-baseline.xml")
    }

    buildToolsVersion = "36.1.0"
    compileSdkMinor = 1
//    compileSdkMinor = 1
}

dependencies {
    // Android Core & Compose UI
    implementation("androidx.core:core-ktx:1.3.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.activity:activity-compose:1.3.0")
    implementation(platform("androidx.compose:compose-bom:2026.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")

    // Location & DataStore
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("androidx.datastore:datastore-preferences:1.2.1")

    // Prayer Times Engine (Java Library)
    // implementation("com.batoulapps.adhan:adhan-java:1.2.1")
    implementation("com.batoulapps.adhan:adhan:1.2.1")
    implementation(libs.androidx.monitor)
    // Testing
    testImplementation("junit:junit:4.13.2")
}