
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }
    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ktor-realtime-graphql"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":ktor-realtime"))
            }
        }
        val androidMain by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.androidpoet.ktor.realtime.graphql"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}

mavenPublishing {

    coordinates("io.github.androidpoet", "ktor-realtime-graphql", "1.0.0")

    pom {
        name.set("ktor-realtime-graphql")
        description.set("Protocol module for ktor-realtime-graphql in KtorBoost realtime.")
        inceptionYear.set("2026")
        url.set("https://github.com/AndroidPoet/KtorBoost/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("ranbirk66")
                name.set("Ranbir Singh")
                url.set("https://github.com/androidpoet/")
            }
        }
        scm {
            url.set("https://github.com/AndroidPoet/KtorBoost/")
            connection.set("scm:git:git://github.com/AndroidPoet/KtorBoost.git")
            developerConnection.set("scm:git:ssh://github.com/AndroidPoet/KtorBoost.git")
        }
    }
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    dependsOn(":ktor-realtime:transformIosMainCInteropDependenciesMetadataForIde")
    dependsOn(":ktor-boost:transformIosMainCInteropDependenciesMetadataForIde")
}
