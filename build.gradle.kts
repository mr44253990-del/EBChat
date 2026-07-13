// Top-level build file
plugins {
    id("com.android.application") version "8.8.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.10" apply false
    id("com.google.devtools.ksp") version "2.0.10-1.0.24" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.google.firebase.crashlytics") version "3.0.1" apply false
}

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.8.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.10")
        classpath("com.google.gms:google-services:4.4.0")
        classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.1")
    }
}
