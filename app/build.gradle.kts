plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.glowgirls"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.glowgirls"
        minSdk = 24
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
//    implementation("androidx.compose.material3:material3:1.2.0") // or latest version
    implementation("androidx.compose.material3:material3-window-size-class:1.2.0") // or latest version

    implementation("androidx.compose.foundation:foundation:1.5.0")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
    implementation("com.google.firebase:firebase-storage-ktx:21.0.0")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.0.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation ("com.google.accompanist:accompanist-coil:0.15.0" )
    implementation( "androidx.compose.runtime:runtime-livedata:1.5.0" )
    implementation ("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")
    implementation ("com.patrykandpatrick.vico:compose:1.6.3") // Use the latest version
    implementation ("com.patrykandpatrick.vico:compose-m3:1.6.3" )// For Material 3 styling
    implementation ("com.patrykandpatrick.vico:core:1.6.3")// For image loading
    implementation ("com.airbnb.android:lottie-compose:6.1.0")
    implementation ("androidx.compose.ui:ui:1.7.0")
    implementation( "androidx.compose.material3:material3:1.3.0")// Ensure compatible UI version
}