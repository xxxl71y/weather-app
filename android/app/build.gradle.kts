plugins {
    id("com.android.application")
}

android {
    namespace = "weather.now"
    compileSdk = 34

    defaultConfig {
        applicationId = "weather.now"
        minSdk = 24
        targetSdk = 34
        versionCode = 10
        versionName = "2.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.browser:browser:1.7.0")
}
