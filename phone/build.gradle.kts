plugins { alias(libs.plugins.android.application) }
android {
    namespace = "com.kang77556.schoolwatch.phone"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.kang77556.schoolwatch.phone"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    testImplementation("junit:junit:4.13.2")
}
