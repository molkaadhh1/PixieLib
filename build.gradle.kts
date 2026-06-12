// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.0" apply false // Adjust based on your Gradle version
    id("com.android.library") version "8.1.0" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false // Google Services plugin
}

buildscript {
    repositories {
        google() // Ensure Google repository is included
        mavenCentral()
    }
    dependencies {
        classpath ("com.google.gms:google-services:4.4.2") // Explicitly declare the Google Services plugin
    }
}
