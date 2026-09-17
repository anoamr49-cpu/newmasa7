plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val buildVersionCode = providers.gradleProperty("versionCode").orNull?.toIntOrNull() ?: 1
val buildVersionName = providers.gradleProperty("versionName").orNull ?: "1.2.0"

android {
    namespace = "com.iraqia.amr"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.iraqia.amr"
        minSdk = 24
        targetSdk = 36
        versionCode = buildVersionCode
        versionName = buildVersionName
    }

    // التوقيع يتم من Codemagic فقط. لا يتم وضع أي مفتاح أو كلمة مرور داخل GitHub.
    signingConfigs {
        create("release") {
            if (System.getenv("CI") == "true") {
                storeFile = file(System.getenv("CM_KEYSTORE_PATH"))
                storePassword = System.getenv("CM_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("CM_KEY_ALIAS")
                keyPassword = System.getenv("CM_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            if (System.getenv("CI") == "true") {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            applicationIdSuffix = ""
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources.excludes += setOf("META-INF/*.kotlin_module")
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    // التطبيق مبني بمكوّنات أندرويد الأصلية بدون مكتبات خارجية لضمان أخف حجم وأعلى استقرار
}
