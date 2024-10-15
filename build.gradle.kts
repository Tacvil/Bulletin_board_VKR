buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.20")
    }
}

plugins {
    id("com.android.application") version "8.7.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21-RC" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.6" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1" apply false
    id("com.google.dagger.hilt.android") version "2.52" apply false
    id("com.google.devtools.ksp") version "2.0.21-RC-1.0.25" apply false
}
