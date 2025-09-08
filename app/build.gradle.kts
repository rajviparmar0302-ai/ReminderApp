plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Firebase plugin
}

android {
    namespace = "com.example.reminderapp" // change if your package name is different
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.reminderapp" // must match your manifest package
        minSdk = 24
        targetSdk = 34
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

    // Ensure Java 8+ compatibility
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Firebase BOM (Bill of Materials) – keeps all Firebase versions compatible
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.4.0")
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    // (Optional) for testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")


    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:34.2.0"))

    // Add the dependency for the Realtime Database library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-database:20.3.0")
//    implementation platform('com.google.firebase:firebase-bom:32.7.2')
//    implementation ('com.google.firebase:firebase-auth')

    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.google.firebase:firebase-auth:22.3.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

}
