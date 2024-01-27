plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.bulletin_board"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.bulletin_board"
        minSdk = 21
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
                "proguard-rules.pro"
            )
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

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation ("androidx.activity:activity-ktx:1.8.2")
    implementation ("androidx.fragment:fragment-ktx:1.6.2")

    implementation ("com.squareup.picasso:picasso:2.8")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

//    implementation  ("io.ak1.pix:piximagepicker:1.6.3")

    //firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("jp.wasabeef:glide-transformations:4.3.0")

    implementation ("com.google.android.gms:play-services-ads:22.6.0")
    implementation ("com.yandex.android:mobileads:5.10.0")
    implementation ("com.yandex.ads.adapter:admob-mobileads:5.10.0.0")

    //billing
    implementation("com.android.billingclient:billing-ktx:6.1.0")
    //lotili
    implementation ("com.airbnb.android:lottie:6.3.0")
    implementation ("org.jetbrains.kotlin:kotlin-parcelize-runtime:1.7.20")

    val camerax_version = "1.2.3"
    // CameraX core library using camera2 implementation
    implementation ("androidx.camera:camera-camera2:$camerax_version")
    // CameraX Lifecycle Library
    implementation ("androidx.camera:camera-lifecycle:$camerax_version")
    // CameraX View class
    implementation ("androidx.camera:camera-view:1.2.3")
    // If you want to additionally use the CameraX Extensions library
    implementation ("androidx.camera:camera-extensions:1.2.3")
    implementation ("com.google.guava:guava:31.0-jre")
    //Glide
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation ("com.github.bumptech.glide:recyclerview-integration:4.12.0")
    // Skip this if you don't want to use integration libraries or configure Glide.
//    kapt "com.github.bumptech.glide:compiler:4.12.0"
//    implementation ('com.github.bumptech.glide:recyclerview-integration:4.12.0') {
//        // Excludes the support library because it's already included by Glide.
//        transitive = false
//    }
}