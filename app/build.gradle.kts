plugins {
    id("com.android.application")
}

android {
    namespace = "com.pdfmaster.reader"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pdfmaster.reader"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Core AndroidX libraries
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.activity:activity:1.8.2")
    implementation("androidx.fragment:fragment:1.6.2")

    // PDF Rendering
    implementation("com.github.mhiew:android-pdf-viewer:3.2.0-beta.3")

    // File picker
    implementation("com.github.Dhaval2404:ImagePicker:v2.1")

    // Permissions
    implementation("com.karumi:dexter:6.2.3")

    // Better permission handling
    implementation("pub.devrel:easypermissions:3.0.0")

    implementation("com.google.android.gms:play-services-ads:22.6.0")

    // Text-to-Speech is built-in Android

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
