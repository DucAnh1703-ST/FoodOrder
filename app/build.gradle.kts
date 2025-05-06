plugins {
    alias(libs.plugins.android.application)
//    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.android) version "2.1.0"  // Cập nhật Kotlin version

    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.foodorder"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.foodorder"
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
    viewBinding {
        enable = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // MaterialDialog
    implementation(libs.core)

    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))

    implementation (libs.firebase.auth.ktx)
    implementation (libs.firebase.database)
    implementation (libs.firebase.messaging)

    // Gson
    implementation (libs.gson)

    // Room database
//    implementation (libs.androidx.room.runtime)
//    kapt (libs.androidx.room.compiler.v225)
    implementation (libs.androidx.room.runtime.v251)
    kapt           (libs.androidx.room.compiler.v251)

    // Retrofit
    implementation (libs.retrofit)
    implementation (libs.logging.interceptor)
    implementation (libs.converter.scalars)

    // Indicator
    implementation (libs.circleindicator)

    // Glide load image
    implementation (libs.glide)

    //event bus
    implementation (libs.eventbus)

    // JSON Parsing
    implementation (libs.gson.v286)
    implementation (libs.converter.gson)
}