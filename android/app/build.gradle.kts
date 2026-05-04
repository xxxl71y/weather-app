plugins {
    id("com.android.application")
}

android {
    namespace = "weather.now"
    compileSdk = 34

    signingConfigs {
        create("fixed") {
            storeFile = file("../weather.keystore")
            storePassword = "android01"
            keyAlias = "weather"
            keyPassword = "android01"
        }
    }

    defaultConfig {
        applicationId = "weather.now"
        minSdk = 24
        targetSdk = 34
        versionCode = 34
        versionName = "2.12.4"
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("fixed")
        }
        release {
            signingConfig = signingConfigs.getByName("fixed")
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
    implementation("androidx.work:work-runtime:2.9.0")
}
