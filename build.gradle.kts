plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("maven-publish")
}

subprojects {
    apply(plugin = "maven-publish")

    afterEvaluate {
        if (plugins.hasPlugin("com.android.library")) {
            extensions.configure<org.gradle.api.publish.PublishingExtension> {
                publications {
                    create<MavenPublication>("release") {
                        groupId = "com.github.apptorise"
                        artifactId = "orbit-connect-${project.name}"
                        version = "1.0.0"

                        afterEvaluate {
                            from(components["release"])
                        }
                    }
                }
            }
        }
    }
}