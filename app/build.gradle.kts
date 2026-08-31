plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)

    // Firebase
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.health.calculator.bmi.tracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.healthmetrics.tracker"

        minSdk = 26
        targetSdk = 36

        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            /*
             * IMPORTANT:
             * Do NOT hardcode real signing passwords here.
             *
             * Configure them through:
             * - environment variables
             * - local.properties
             * - CI secrets
             *
             * Add the actual configuration only on your local/CI environment.
             */
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        isCoreLibraryDesugaringEnabled = true
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(
                org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {

    coreLibraryDesugaring(
        "com.android.tools:desugar_jdk_libs:2.1.4"
    )

    // ---------------------------------------------------------
    // Android
    // ---------------------------------------------------------

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.process)

    implementation(libs.androidx.activity.compose)

    // ---------------------------------------------------------
    // Compose
    // ---------------------------------------------------------

    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    implementation(libs.androidx.material3)

    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.compose.animation)

    // ---------------------------------------------------------
    // Navigation
    // ---------------------------------------------------------

    implementation(libs.androidx.navigation.compose)

    // ---------------------------------------------------------
    // Hilt
    // ---------------------------------------------------------

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.hilt.navigation.compose)

    // ---------------------------------------------------------
    // DataStore
    // ---------------------------------------------------------

    implementation(libs.androidx.datastore.preferences)

    // ---------------------------------------------------------
    // Gson
    // ---------------------------------------------------------

    implementation("com.google.code.gson:gson:2.10.1")

    // ---------------------------------------------------------
    // Room
    // ---------------------------------------------------------

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    ksp(libs.androidx.room.compiler)

    // ---------------------------------------------------------
    // WorkManager
    // ---------------------------------------------------------

    implementation(libs.androidx.work.runtime.ktx)

    // ---------------------------------------------------------
    // Google Drive
    // ---------------------------------------------------------

    implementation(libs.google.play.services.auth)
    implementation(libs.google.api.client.android)
    implementation(libs.google.api.services.drive)
    implementation(libs.google.http.client.gson)

    // ---------------------------------------------------------
    // Images
    // ---------------------------------------------------------

    implementation(libs.coil.compose)

    // ---------------------------------------------------------
    // QR
    // ---------------------------------------------------------

    implementation(libs.zxing.core)

    // ---------------------------------------------------------
    // Health Connect
    // ---------------------------------------------------------

    implementation(libs.androidx.health.connect.client)

    // ---------------------------------------------------------
    // Firebase AI Logic
    // ---------------------------------------------------------

    implementation(
        platform("com.google.firebase:firebase-bom:34.18.0")
    )

    implementation("com.google.firebase:firebase-ai")

    // Real production protection
    releaseImplementation(
        "com.google.firebase:firebase-appcheck-playintegrity"
    )

    // Emulator/development only
    debugImplementation(
        "com.google.firebase:firebase-appcheck-debug"
    )

    // ---------------------------------------------------------
    // Testing
    // ---------------------------------------------------------

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    androidTestImplementation(
        platform(libs.androidx.compose.bom)
    )

    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}