import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
}

description = "Snappy compression library for Kotlin Multiplatform"
group = "org.chorus-oss"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    linuxX64()
    mingwX64()

    // TODO: Add more supported platforms
    // Open an issue if you want a platform to be added.

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.kotlinx.io)

                implementation(libs.varlen)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }

    mavenPublishing {
        publishToMavenCentral()
        signAllPublications()

        coordinates(
            group.toString(),
            "snappy",
            version.toString()
        )

        pom {
            name = "Snappy"
            description = project.description
            inceptionYear = "2025"
            url = "https://github.com/Chorus-OSS/Snappy"
            licenses {
                license {
                    name = "The Apache License, Version 2.0"
                    url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    distribution = "repo"
                }
            }
            developers {
                developer {
                    id = "omniacdev"
                    name = "OmniacDev"
                    url = "https://github.com/OmniacDev"
                    email = "omniacdev@chorus-oss.org"
                }
            }
            scm {
                url = "https://github.com/Chorus-OSS/Snappy"
                connection = "scm:git:git://github.com/Chorus-OSS/Snappy.git"
                developerConnection = "scm:git:ssh://github.com/Chorus-OSS/Snappy.git"
            }
            issueManagement {
                system = "GitHub Issues"
                url = "https://github.com/Chorus-OSS/Snappy/issues"
            }
        }

        configure(
            KotlinMultiplatform(
                javadocJar = JavadocJar.Dokka("dokkaHtml"),
                sourcesJar = true,
                androidVariantsToPublish = emptyList(),
            )
        )
    }
}