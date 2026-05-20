import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

val localPropsFile = rootProject.file("local.properties")

val generateBuildConfig = tasks.register("generateBuildConfig") {
    notCompatibleWithConfigurationCache("reads local.properties at execution time")
    val outDir = layout.buildDirectory.dir("generated/buildconfig/ca/arzook/shared").get().asFile
    val plistFile = rootProject.file("iosApp/iosApp/GoogleService-Info.plist")
    inputs.file(localPropsFile)
    outputs.dir(outDir)
    outputs.file(plistFile)
    doLast {
        val props = Properties().apply {
            localPropsFile.takeIf { it.exists() }?.inputStream()?.use { load(it) }
        }
        outDir.mkdirs()
        File(outDir, "BuildConfig.kt").writeText("""
            package ca.arzook.shared
            object BuildConfig {
                const val GOOGLE_CLIENT_ID_ANDROID = "${props["GOOGLE_CLIENT_ID_ANDROID"] ?: ""}"
                const val GOOGLE_CLIENT_ID_IOS = "${props["GOOGLE_CLIENT_ID_IOS"] ?: ""}"
                const val GOOGLE_CLIENT_ID_WEB = "${props["GOOGLE_CLIENT_ID_WEB"] ?: ""}"
                const val GOOGLE_CLIENT_SECRET_WEB = "${props["GOOGLE_CLIENT_SECRET_WEB"] ?: ""}"
                const val RECAPTCHA_SITE_KEY = "${props["RECAPTCHA_SITE_KEY"] ?: ""}"
                const val DEV_EMAIL = "${props["DEV_EMAIL"] ?: ""}"
                const val DEV_PASSWORD = "${props["DEV_PASSWORD"] ?: ""}"
            }
        """.trimIndent())

        val iosClientId = props["GOOGLE_CLIENT_ID_IOS"] ?: ""
        val reversedClientId = iosClientId.toString().split(".").reversed().joinToString(".")
        plistFile.writeText("""
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
            <plist version="1.0">
            <dict>
                <key>CLIENT_ID</key>
                <string>$iosClientId</string>
                <key>REVERSED_CLIENT_ID</key>
                <string>$reversedClientId</string>
                <key>PLIST_VERSION</key>
                <string>1</string>
                <key>BUNDLE_ID</key>
                <string>ca.arzook.iosapp</string>
            </dict>
            </plist>
        """.trimIndent())
    }
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions { jvmTarget.set(JvmTarget.JVM_1_8) }
            }
        }
    }

    val xcf = XCFramework()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
            xcf.add(this)
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(layout.buildDirectory.dir("generated/buildconfig"))
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.animation)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.websockets)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
                implementation("io.github.vinceglb:filekit-compose:0.8.8")
                implementation(libs.coil.compose)
                implementation(libs.coil.network)
            }
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.android)
            implementation(libs.androidx.activity.compose)
            implementation(libs.youtube.player)
            implementation(libs.google.signin)
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services.auth)
            implementation(libs.googleid)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }

    targets.all {
        compilations.all {
            compileTaskProvider.configure { dependsOn(generateBuildConfig) }
        }
    }
}

android {
    namespace = "ca.arzook.arzook"
    compileSdk = 35
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
dependencies {
}
