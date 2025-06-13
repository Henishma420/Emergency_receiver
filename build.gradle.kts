// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript { // Add this block
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.gms:google-services:4.4.0") // Or the latest version
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
}