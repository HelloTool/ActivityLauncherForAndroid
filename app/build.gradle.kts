import com.android.build.api.variant.impl.capitalizeFirstChar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Locale
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
}

android {
    namespace = "io.gitee.jesse205.activitylauncher"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.gitee.jesse205.activitylauncher"
        minSdk = 9
        targetSdk = 33
        versionCode = 1
        versionName = "0.1.0"
    }
    val localSigningConfig = if (rootProject.file("keystore.properties").exists()) {
        val keystoreProperties = Properties().apply { load(rootProject.file("keystore.properties").inputStream()) }
        signingConfigs.create("local") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    } else {
        signingConfigs.getByName("debug")
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = localSigningConfig
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = ".debug"
        }
    }

    sourceSets.configureEach {
        assets.srcDir(layout.buildDirectory.dir("generated/assets/$name"))
    }
    applicationVariants.configureEach {
        val variantName = name
        val processAssetsTemplate =
            tasks.register("process${variantName.capitalizeFirstChar()}AssetsTemplate", Copy::class) {
                from("src/main/assets_template")
                into(layout.buildDirectory.dir("generated/assets/$variantName"))
                filter(
                    org.apache.tools.ant.filters.ReplaceTokens::class, "tokens" to mapOf(
                        "applicationId" to applicationId
                    )
                )
                rename { fileName ->
                    fileName.replace("_template", "")
                }
            }
        preBuildProvider.get().dependsOn(processAssetsTemplate)
    }

    applicationVariants.configureEach {
        outputs.configureEach {
            this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            outputFileName = mutableListOf<String>().apply {
                add(
                    rootProject.name.split("-").joinToString("") { projectNamePart ->
                        projectNamePart.replaceFirstChar { it.titlecase(Locale.US) }
                    }
                )
                add("v${defaultConfig.versionName}")
                if (flavorName.isNotEmpty()) {
                    add(flavorName)
                }
                if (buildType.name != "release") {
                    add(buildType.name)
                }
            }.joinToString("_") + ".apk"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget("11")
    }
}

dependencies {
    implementation(libs.androidx.annotation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}