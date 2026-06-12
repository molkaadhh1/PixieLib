plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.app.mydashboard"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.app.mydashboard"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Firebase BOM to manage versions
    implementation (platform("com.google.firebase:firebase-bom:33.7.0")) // Use the latest BOM version

    // Individual Firebase libraries
    implementation ("com.google.firebase:firebase-auth")        // Firebase Authentication
    implementation ("com.google.firebase:firebase-analytics")     // Firebase Analytics
    implementation ("com.google.firebase:firebase-database")     // Firebase Realtime Database
    implementation ("com.google.firebase:firebase-firestore")    // Firebase Firestore



}
