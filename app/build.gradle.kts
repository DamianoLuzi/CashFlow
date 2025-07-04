import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}
android {
    namespace = "com.example.exptrackpm"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.exptrackpm"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"${localProperties["SUPABASE_URL"]}\""
        )
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            "\"${localProperties["SUPABASE_ANON_KEY"]}\""
        )
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Lifecycle dependency
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.androidx.library)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.ui.test.junit4.android)
    implementation(libs.androidx.work.testing)
    implementation(libs.androidx.navigation.testing.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel")
    implementation("androidx.room:room-runtime:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")
    //kapt("androidx.room:room-compiler:2.5.2")
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    //YCharts
    implementation ("co.yml:ycharts:2.1.0")
    //Supabase storage
    implementation("io.github.jan-tennert.supabase:storage-kt:3.1.4")
    // Ktor client (required by Supabase-kt)
    implementation("io.ktor:ktor-client-android:3.1.3")
    implementation("io.ktor:ktor-client-content-negotiation:3.1.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.3")
    // Coil for image loading
    implementation("io.coil-kt.coil3:coil-compose:3.2.0")
    implementation ("androidx.work:work-runtime-ktx:2.10.2")

    // Robolectric environment
    testImplementation ("androidx.test:core:1.6.1")
    //  Mockito framework
    testImplementation ("org.mockito:mockito-core:5.18.0")
    // mockito-kotlin
    testImplementation ("org.mockito.kotlin:mockito-kotlin:5.2.1")
    //  Mockk framework
    testImplementation ("io.mockk:mockk:1.14.4")
    testImplementation ("io.mockk:mockk-android:1.14.4")
    testImplementation ("io.mockk:mockk-agent:1.14.4")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    // Robolectric for JVM Android testing
    testImplementation ("junit:junit:4.13.2")
    testImplementation ("org.robolectric:robolectric:4.15.1")
    //testImplementation ("androidx.test.ext:junit:1.1.5")
    //androidTestImplementation ("androidx.test.espresso:espresso-core:3.3.0")
    testImplementation ("org.mockito:mockito-inline:5.2.0")
    testImplementation ("androidx.test:core-ktx:1.6.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // Now, declare your test dependencies WITHOUT versions, as the BOM will provide them
    //androidTestImplementation ("androidx.test.ext:junit:1.2.1")
    //androidTestImplementation ("androidx.test.espresso:espresso-core:3.6.1")
}

configurations.all {
    resolutionStrategy {
        force("androidx.test.ext:junit:1.2.1")
        force ("androidx.test.espresso:espresso-core:3.6.1")
    }
}