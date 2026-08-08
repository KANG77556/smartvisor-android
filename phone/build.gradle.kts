plugins { alias(libs.plugins.android.application) }
android {
    namespace = "com.kang77556.schoolwatch.phone"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.kang77556.schoolwatch"
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
    implementation("com.google.android.gms:play-services-wearable:19.0.0")
    testImplementation("junit:junit:4.13.2")
}
