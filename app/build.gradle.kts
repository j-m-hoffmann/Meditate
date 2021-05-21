import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
}

// System.getenv("HOME") may not work on Mac and Windows
val keystoreProperties = File(
    "${System.getenv("HOME")}/Android/keystore/",
    "meditate.properties"
).inputStream()
    .use { fileStream ->
        Properties().apply { load(fileStream) }
    }

android {
    compileSdkVersion(30)

    defaultConfig {
        applicationId = "com.gitlab.j_m_hoffmann.meditate"
        minSdkVersion(21)
        targetSdkVersion(30)
        versionCode = 2
        versionName = "1.0.1"
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
//            proguardFiles = listOf(getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro')
            signingConfig = signingConfigs.findByName("release")
        }
    }
    compileOptions {
//        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = "1.8" }
}

dependencies {
//    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.1")
//    implementation fileTree(dir: "libs", include: ["*.jar"])
    implementation(AndroidX.appCompat)
    implementation(AndroidX.core.ktx)
    implementation(AndroidX.fragmentKtx)

    implementation(AndroidX.lifecycle.commonJava8)
    implementation(AndroidX.lifecycle.liveDataKtx)
    implementation(AndroidX.lifecycle.viewModelKtx)

    implementation(AndroidX.navigation.fragmentKtx)
    implementation(AndroidX.navigation.uiKtx)

    implementation(AndroidX.preferenceKtx)

    implementation(AndroidX.room.ktx)
    kapt(AndroidX.room.compiler)

    implementation(AndroidX.vectorDrawable)

    implementation(AndroidX.work.runtimeKtx) // WorkManager

    implementation(Google.dagger)
    implementation(Google.dagger.android)
    implementation(Google.dagger.android.support)
    kapt(Google.dagger.compiler)
    kapt(Google.dagger.android.processor)

    implementation(Google.android.material)

    implementation(Kotlin.stdlib.jdk8)

    androidTestImplementation("com.google.truth:truth:1.1.2")
    androidTestImplementation(AndroidX.archCore.testing)
    androidTestImplementation(AndroidX.test.espresso.core)
    androidTestImplementation(AndroidX.test.ext.junitKtx)
    androidTestImplementation(AndroidX.test.rules)
    androidTestImplementation(AndroidX.test.runner)
    testImplementation(Testing.junit4)
}

afterEvaluate {
    tasks.withType<JavaCompile> {
        options.isDeprecation = true
        options.compilerArgs.addAll(
            listOf(
                "-Xlint:unchecked"
//                , "-Xlint:deprecation",
//                , "-Werror"
            )
        )
    }
}
