import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp") version "2.0.20-1.0.24"
}

android {
    namespace = "com.example.bulletin_board"
    compileSdk = 34

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

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "1.8"
}

tasks.withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>().configureEach {
    jvmTarget = "1.8"
}

// Определение конфигурации ktlint, если она не существует
val ktlint by configurations.creating

// Задача ktlintCheck
val ktlintCheck by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Проверка стиля кода Kotlin"
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args(
        "**/src/**/*.kt",
        "**.kts",
        "!**/build/**",
    )
}

tasks.check {
    dependsOn(ktlintCheck)
}

// Задача ktlintFormat
tasks.register<JavaExec>("ktlintFormat") {
    group = "verification"
    description = "Проверка и форматирование стиля кода Kotlin"
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
    args(
        "-F",
        "**/src/**/*.kt",
        "**.kts",
        "!**/build/**",
    )
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("androidx.activity:activity-ktx:1.9.1")
    implementation("androidx.fragment:fragment-ktx:1.8.2")

    implementation("com.squareup.picasso:picasso:2.8")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

//    implementation  ("io.ak1.pix:piximagepicker:1.6.3")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-compiler:2.52")

    // Detekt
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules-libraries:1.23.6")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules-ruleauthors:1.23.6")

    // ktlint
    ktlint("com.pinterest.ktlint:ktlint-cli:1.3.1") {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }

    // firebase
    implementation("com.google.firebase:firebase-firestore:25.1.0")
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("com.google.firebase:firebase-messaging-ktx:24.0.1")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging-directboot:24.0.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("jp.wasabeef:glide-transformations:4.3.0")

    implementation("com.google.android.gms:play-services-ads:23.1.0")
    implementation("com.yandex.android:mobileads:5.10.0")
    implementation("com.yandex.ads.adapter:admob-mobileads:5.10.0.0")

    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    // billing
    implementation("com.android.billingclient:billing-ktx:7.0.0")
    // lotili
    implementation("com.airbnb.android:lottie:6.5.0")
    implementation("org.jetbrains.kotlin:kotlin-parcelize-runtime:2.0.20")

    val camerax_version = "1.2.3"
    // CameraX core library using camera2 implementation
    implementation("androidx.camera:camera-camera2:$camerax_version")
    // CameraX Lifecycle Library
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    // CameraX View class
    implementation("androidx.camera:camera-view:1.2.3")
    // If you want to additionally use the CameraX Extensions library
    implementation("androidx.camera:camera-extensions:1.2.3")
    implementation("com.google.guava:guava:31.0-jre")
    // Glide
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.github.bumptech.glide:recyclerview-integration:4.16.0")
    // Skip this if you don't want to use integration libraries or configure Glide.
//    kapt "com.github.bumptech.glide:compiler:4.12.0"
//    implementation ('com.github.bumptech.glide:recyclerview-integration:4.12.0') {
//        // Excludes the support library because it's already included by Glide.
//        transitive = false
//    }
}
