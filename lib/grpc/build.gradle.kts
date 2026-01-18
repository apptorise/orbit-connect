plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.apptorise.orbit.connect.grpc"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(project(":lib:core"))

    api(libs.grpc.okhttp)
    api(libs.grpc.stub)
    api(libs.grpc.kotlin.stub)

    implementation(libs.kotlinx.coroutines.core)

    implementation("com.google.protobuf:protobuf-java-util:3.25.1")
    implementation(libs.androidx.core.ktx)
}