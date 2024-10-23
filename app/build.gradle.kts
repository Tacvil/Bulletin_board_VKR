import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp") version "2.0.21-RC-1.0.25"
    kotlin("plugin.serialization") version "2.0.21-RC"
}

android {
    namespace = "com.example.bulletin_board"
    compileSdk = 34

    lint {
        disable.addAll(setOf("PlayServiceAdsVersion", "AppMetricaSdkVersion", "MobileAdsSdkVersion"))
    }

    signingConfigs {
        create("config") {
            keyAlias = "BulBord"
            keyPassword = "Esdes337"
            storeFile = file("F:\\KeyStore\\key.jks")
            storePassword = "Esdes337"
        }
    }

    defaultConfig {
        applicationId = "com.example.bulletin_board"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("config")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$projectDir/config/detekt.yml")
    baseline =
        file("$projectDir/config/baseline.xml")
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "1.8"
}

tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = "1.8"
}

dependencies {

    // Versions
    val cameraxVersion = "1.2.3"
    val pagingVersion = "3.3.2"

    // AndroidX
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-extensions:$cameraxVersion")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.fragment:fragment-ktx:1.8.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.media3:media3-database:1.4.1")
    implementation("androidx.paging:paging-runtime-ktx:$pagingVersion")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-firestore:25.1.1")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.1")
    implementation("com.google.firebase:firebase-messaging-directboot:24.0.3")
    implementation("com.google.firebase:firebase-messaging-ktx:24.0.3")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.firebaseui:firebase-ui-firestore:8.0.2")

    // Google
    implementation("com.google.android.gms:play-services-ads:23.4.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.android.gms:play-services-fido:21.1.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-compiler:2.52") // Hilt

    // Other
    implementation("com.airbnb.android:lottie:6.5.0") // Lottie
    implementation("com.github.bumptech.glide:glide:4.16.0") //Glide
    implementation("com.github.bumptech.glide:recyclerview-integration:4.16.0")
    implementation("com.google.code.gson:gson:2.11.0") // Gson
    //noinspection GradleDependency
    implementation("com.google.guava:guava:31.0-jre")
    implementation("com.jakewharton.timber:timber:5.0.1") // Timber
    implementation("com.squareup.picasso:picasso:2.8") //Picasso
    implementation("jp.wasabeef:glide-transformations:4.3.0")
    implementation("org.jetbrains.kotlin:kotlin-parcelize-runtime:2.0.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1") // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.2") // Kotlin Serialization
    //noinspection MobileAdsSdkOutdatedVersion
    implementation("com.yandex.android:mobileads:5.10.0")
    implementation("com.yandex.ads.adapter:admob-mobileads:5.10.0.0")

    // Detekt
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules-libraries:1.23.6")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules-ruleauthors:1.23.6")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
