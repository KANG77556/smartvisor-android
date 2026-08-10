plugins { alias(libs.plugins.android.application) }
android {
    enableKotlin = false
    namespace = "com.kang77556.schoolwatch.face"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.kang77556.schoolwatch.face"
        minSdk = 33
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"
    }
    buildTypes {
        debug { isMinifyEnabled = true }
        release {
            isMinifyEnabled = true
            isShrinkResources = false
        }
    }
}
