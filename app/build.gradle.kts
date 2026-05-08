plugins {
    id("com.android.application")
    id ("com.google.gms.google-services")
}

android {
    namespace = "com.example.tidyup"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tidyup"
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
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    // AndroidX y UI
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    // Esta librería es muy antigua, si no la usas para algo muy específico,
    // podrías borrarla, pero la dejo por si acaso:
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    // --- FIREBASE (GESTIÓN LIMPIA) ---
    // 1. El BoM controla todas las versiones de Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.6.0"))

    // 2. Dependencias sin versión (el BoM decide la mejor)
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // HE ELIMINADO: firebase-installations:19.1.0 (Causaba el error de duplicados)
    // HE ELIMINADO: constraintlayout-core (Ya viene dentro de constraintlayout)

    // --- LIBRERÍAS PARA PRUEBAS (UNIT TESTS) ---
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.2.0")

    // --- LIBRERÍAS PARA PRUEBAS (INSTRUMENTED TESTS) ---
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("org.mockito:mockito-android:5.2.0")
}