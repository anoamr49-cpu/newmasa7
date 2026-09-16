plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("dev.flutter.flutter-gradle-plugin")
}
android {
    namespace = "com.iraqia.amr"
    compileSdk = flutter.compileSdkVersion
    defaultConfig {
        applicationId = "com.iraqia.amr"
        minSdk = 23
        targetSdk = flutter.targetSdkVersion
        versionCode = 1
        versionName = "1.0.0"
    }
}
flutter { source = "../.." }
