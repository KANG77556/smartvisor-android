plugins { alias(libs.plugins.android.application) }
android {
    namespace = "com.kang77556.schoolwatch.companion"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.kang77556.schoolwatch.companion"
        minSdk = 33
        targetSdk = 36
        versionCode = 2
        versionName = "1.1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    implementation(libs.wear.watchface.complications.data.source)
    testImplementation("junit:junit:4.13.2")
}
