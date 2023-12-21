@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    id("com.google.firebase.appdistribution")
}

android {
    namespace = "com.example.algotapes2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.algotapes2"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.activity)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)
    implementation(libs.firebase.config)
    implementation(libs.firebase.inappmessaging.display)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.glide)
    annotationProcessor (libs.compiler)
    implementation (libs.viewpager2)
    implementation (libs.circleimageview)
    implementation (libs.picasso)
    implementation (libs.sdp.android)
    implementation (libs.firebase.ui.database)
    implementation (libs.picasso)
    // To recognize Latin script
    implementation (libs.text.recognition)
    

    // To recognize Chinese script
    implementation (libs.text.recognition.chinese)

    // To recognize Devanagari script
    implementation (libs.text.recognition.devanagari)

    // To recognize Japanese script
    implementation (libs.text.recognition.japanese)

    // To recognize Korean script
    implementation (libs.text.recognition.korean)

    // Camerax implementation
    implementation (libs.camera.core)
    implementation (libs.camera.camera2)
    implementation (libs.camera.lifecycle)
    implementation (libs.camera.video)
    implementation (libs.camera.view)
    implementation (libs.camera.extensions)

    // google ml kit live labeling implementation
    implementation (libs.image.labeling)

    implementation ("com.google.mlkit:object-detection:17.0.0")


}