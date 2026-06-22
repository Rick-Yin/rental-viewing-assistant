plugins {
    id("com.android.library")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.dagger.hilt.android")
}

val sharedAssetsDir = layout.buildDirectory.dir("generated/sharedAssets")
val syncSharedAssets by tasks.registering(Copy::class) {
    from(rootProject.layout.projectDirectory.dir("../../shared/checklist")) {
        include("viewing-checklist.json", "signing-checklist.json")
    }
    from(rootProject.layout.projectDirectory.dir("../../shared/schemas")) {
        include(
            "manifest.v1.json",
            "viewing-analysis-result.v1.json",
            "signing-analysis-result.v1.json",
        )
    }
    into(sharedAssetsDir)
}

android {
    namespace = "com.rentalviewingassistant.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    sourceSets {
        getByName("main") {
            assets.srcDir(sharedAssetsDir.get().asFile)
        }
    }

    kotlin {
        jvmToolchain(21)
    }
}

tasks.matching { it.name != "syncSharedAssets" && it.name.endsWith("Assets") }.configureEach {
    dependsOn(syncSharedAssets)
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))

    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    implementation("com.google.dagger:hilt-android:2.59.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

    ksp("androidx.room:room-compiler:2.8.4")
    ksp("com.google.dagger:hilt-android-compiler:2.59.2")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
