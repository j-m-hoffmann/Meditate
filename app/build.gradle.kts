import java.util.Properties

plugins {
    id("com.android.application")
    id("dagger.hilt.android.plugin")
    kotlin("android")
    kotlin("kapt")
}

// System.getenv("HOME") may not work on Mac and Windows
val keystoreProperties = Properties().apply {
    load(File("${System.getenv("HOME")}/Android/keystore/meditate.properties").inputStream())
}

android {
    compileSdkVersion(30)

    defaultConfig {
        applicationId = "com.gitlab.j_m_hoffmann.meditate"
        minSdkVersion(21)
        targetSdkVersion(30)
        versionCode = 14
        versionName = "1.1.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        kapt {
            arguments { arg("room.incremental", "true") }

            correctErrorTypes = true
        }
    }
    signingConfigs {
        create("release") {
            keyAlias(keystoreProperties["keyAlias"] as String)
            keyPassword(keystoreProperties["keyPassword"] as String)
            storeFile(file(keystoreProperties["storeFile"] as String))
            storePassword(keystoreProperties["storePassword"] as String)
        }
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
    buildTypes {
        named("debug") {
            isDefault = true
            isMinifyEnabled = false
            isShrinkResources = false
        }
        named("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = "1.8" }
}

val versionDagger: String by project

dependencies {
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.2")
    androidTestImplementation("androidx.test:core-ktx:1.3.0")
    androidTestImplementation("androidx.test:rules:1.3.0")
    androidTestImplementation("androidx.test:runner:1.3.0")
    androidTestImplementation("com.google.truth:truth:1.1.2")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    implementation("androidx.appcompat:appcompat:1.3.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.core:core-ktx:1.5.0")
    implementation("androidx.fragment:fragment-ktx:1.3.4")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.3.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.room:room-ktx:2.3.0")
    implementation("androidx.vectordrawable:vectordrawable:1.1.0")
    implementation("androidx.work:work-runtime-ktx:2.5.0")
    implementation("com.google.android.material:material:1.3.0")
    implementation("com.google.dagger:hilt-android:$versionDagger")
    implementation(kotlin("stdlib-jdk8"))

    kapt("androidx.room:room-compiler:2.3.0")
    kapt("com.google.dagger:hilt-compiler:$versionDagger")

    testImplementation("junit:junit:4.13.2")
}

afterEvaluate {
    tasks.withType<JavaCompile> {
        options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
    }
}
