import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
}

// System.getenv("HOME") may not work on Mac and Windows
val keystoreProperties = File(
    "${System.getenv("HOME")}/android/keystore/binaural-beats-2/",
    "keystore.properties"
).inputStream()
    .use {
        Properties().apply { load(it) }
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

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.incremental"] = "true"
            }
        }
    }
    signingConfigs {
        create("release") {
            keyAlias(keystoreProperties["keyAlias"] as String)
            keyPassword(keystoreProperties["keyPassword"] as String)
            storeFile(file(keystoreProperties["storePath"] as String))
            storePassword(keystoreProperties["storePassword"] as String)
        }
    }
    buildFeatures {
        dataBinding = true
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.fragment:fragment-ktx:1.2.5")
//    implementation "androidx.legacy:legacy-support-v4:1.0.0"

    // Lifecycle
    val lifecycle_version = "2.2.0"
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")

    // Navigation
    val navigation_version = "2.3.3"
    implementation("androidx.navigation:navigation-fragment-ktx:$navigation_version")
    implementation("androidx.navigation:navigation-ui-ktx:$navigation_version")

    implementation("androidx.preference:preference-ktx:1.1.1")

    // Room
    val room_version = "2.2.6"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.4.0")

    implementation("androidx.vectordrawable:vectordrawable:1.1.0")
    implementation("com.google.android.material:material:1.2.1")

    // Dagger
    val dagger_version = "2.26"
    implementation("com.google.dagger:dagger:$dagger_version")
    kapt("com.google.dagger:dagger-compiler:$dagger_version")
    implementation("com.google.dagger:dagger-android:$dagger_version")
    implementation("com.google.dagger:dagger-android-support:$dagger_version")
    kapt("com.google.dagger:dagger-android-processor:$dagger_version")

    // Tests
    testImplementation("junit:junit:4.13.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("androidx.test:runner:1.3.0")
    androidTestImplementation("androidx.test:rules:1.3.0")
    androidTestImplementation("com.google.truth:truth:1.0.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
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
