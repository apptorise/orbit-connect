plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    id("maven-publish")
}

subprojects {
    apply(plugin = "maven-publish")

    afterEvaluate {
        if (plugins.hasPlugin("com.android.library")) {
            extensions.configure<PublishingExtension> {
                publications {
                    create<MavenPublication>("release") {
                        groupId = "com.github.apptorise"
                        artifactId = "orbit-connect-${project.name}"
                        version = "1.0.24"

                        afterEvaluate {
                            from(components["release"])
                        }
                    }
                }
            }
        }
    }
}