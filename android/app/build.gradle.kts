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
        versionCode = 38
        versionName = "3.0"
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

// Inject APP_VERSION into index.html and sync assets
val syncAssets = tasks.register("syncAssets") {
    doLast {
        val version = android.defaultConfig.versionName
        val assetDir = file("src/main/assets")
        assetDir.mkdirs()

        // Copy index.html with version injected
        val html = file("../../index.html").readText()
            .replace("__APP_VERSION__", version)
        file("$assetDir/index.html").writeText(html)

        // Copy src/ modules
        val srcDir = file("../../src")
        val srcAssetDir = file("$assetDir/src")
        srcAssetDir.mkdirs()
        srcDir.listFiles()?.forEach { f ->
            file("$srcAssetDir/${f.name}").writeText(f.readText())
        }
    }
}
tasks.named("preBuild") { dependsOn(syncAssets) }

dependencies {
    implementation("androidx.browser:browser:1.7.0")
    implementation("androidx.work:work-runtime:2.9.0")
}
