plugins {

    id("com.android.application")

    id("org.jetbrains.kotlin.android")

    id("com.google.gms.google-services")
}

android {

    namespace = "com.example.campusbustracker"

    compileSdk = 35

    defaultConfig {

        applicationId = "com.example.campusbustracker"

        minSdk = 24

        targetSdk = 35

        versionCode = 1

        versionName = "1.0"
    }

    compileOptions {

        sourceCompatibility =
            JavaVersion.VERSION_11

        targetCompatibility =
            JavaVersion.VERSION_11
    }
}

kotlin {

    jvmToolchain(11)
}

dependencies {

    implementation(
        "androidx.core:core-ktx:1.13.1"
    )

    implementation(
        "androidx.appcompat:appcompat:1.7.0"
    )

    implementation(
        "com.google.android.material:material:1.12.0"
    )

    implementation(
        "androidx.constraintlayout:constraintlayout:2.1.4"
    )

    implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
    implementation("com.google.firebase:firebase-database-ktx:21.0.0")
    implementation("com.google.firebase:firebase-storage-ktx:21.0.1")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.google.android.libraries.places:places:3.5.0")
    implementation(
        "com.google.maps.android:android-maps-utils:3.8.2"
    )

    testImplementation(
        "junit:junit:4.13.2"
    )

    androidTestImplementation(
        "androidx.test.ext:junit:1.2.1"
    )

    androidTestImplementation(
        "androidx.test.espresso:espresso-core:3.6.1"
    )
}