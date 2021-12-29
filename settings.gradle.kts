enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    plugins {
        id("com.github.johnrengelman.shadow") version "7.1.2"
        id("org.cadixdev.licenser") version "0.6.1"
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
        maven {
            url = uri("https://libraries.minecraft.net")
        }
    }
}

rootProject.name = "serverwrecker"

setOf(
    "version_1_7",
    "version_1_8",
    "version_1_9",
    "version_1_10",
    "version_1_11",
    "version_1_12",
    "version_1_13",
    "version_1_14",
    "version_1_15",
    "version_1_16",
    "version_1_17",
    "version_1_18",
).forEach {
    setupSWSubproject(it)
}

setupSWSubproject("common")
setupSWSubproject("core")
setupSWSubproject("protocol")

fun setupSWSubproject(name: String) {
    setupSubproject("serverwrecker-$name") {
        projectDir = file(name)
    }
}

inline fun setupSubproject(name: String, block: ProjectDescriptor.() -> Unit) {
    include(name)
    project(":$name").apply(block)
}