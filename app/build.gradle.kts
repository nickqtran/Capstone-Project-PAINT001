plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.dokka")
}

android {
    buildFeatures { compose = true }
    namespace = "com.example.painlessprep"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.painlessprep"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }


}

dependencies {
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation ("androidx.camera:camera-core:1.3.0")
    implementation ("androidx.camera:camera-camera2:1.3.0")
    implementation ("androidx.camera:camera-lifecycle:1.3.0")
    implementation ("androidx.camera:camera-view:1.3.0")
    implementation(libs.androidx.constraintlayout)
    implementation("org.opencv:opencv:4.12.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.ui.tooling)
}

tasks.dokkaHtml {
    dokkaSourceSets {
        configureEach {
            documentedVisibilities.set(
                setOf(org.jetbrains.dokka.DokkaConfiguration.Visibility.PUBLIC)
            )
            suppressInheritedMembers.set(true)
            skipDeprecated.set(true)

            perPackageOption {
                matchingRegex.set(".*")
                suppress.set(true)
            }

            perPackageOption {
                matchingRegex.set("com\\.example\\.painlessprep\\..*")
                suppress.set(false)
            }
        }
    }
}