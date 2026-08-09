plugins { alias(libs.plugins.android.application) }
android {
    namespace = "com.kang77556.schoolwatch.companion"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.kang77556.schoolwatch"
        minSdk = 33
        targetSdk = 36
        versionCode = 3
        versionName = "1.2.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    implementation(libs.wear.watchface.complications.data.source)
    implementation("com.google.android.gms:play-services-wearable:19.0.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20250517")
}
