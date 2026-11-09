plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "org.uwuaosp.compose"
    compileSdk = 37
    defaultConfig { minSdk = 31 }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    api(libs.compose.foundation)
    api(libs.compose.animation)
    api(libs.compose.ui)
    api(libs.material3)
    api(libs.material.icons.extended)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
        )
    }
}

androidComponents.onVariants { variant ->
    variant.sources.kotlin?.addStaticSourceDirectory("../third_party/uwu-sdk/uwuCompose/src")
    variant.sources.res?.addStaticSourceDirectory("../third_party/uwu-sdk/uwuCompose/res")
}
