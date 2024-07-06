plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.appdistribution")
}

android {
    namespace = "com.example.val_info"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.val_info"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            keyAlias = "key69"
            keyPassword = "200822"
            storeFile = file("C:/Users/Ronaldo/project/ValInfo/MyApp-release.jks")
            storePassword = "970904"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

firebaseAppDistribution {
    releaseNotesFile = "release-notes-valinfo.txt"
    testers = "nalnalfill5@gmail.com,naldo.rekasito@gmail.com"
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.1.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-storage:20.0.0")
    implementation("com.google.firebase:firebase-database:20.0.3")
    implementation ("com.google.firebase:firebase-auth:21.0.1")
    implementation("com.google.firebase:firebase-appcheck")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.firebase:firebase-auth:21.0.3")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:3.0.2")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("com.google.firebase:firebase-firestore:25.0.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
