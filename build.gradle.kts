plugins {
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidTest) apply false
    alias(libs.plugins.baselineProfile) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room3) apply false
}

subprojects {
    configurations.configureEach {
        resolutionStrategy.eachDependency {
            if (
                requested.group in
                    setOf(
                        "androidx.compose.ui",
                        "androidx.compose.foundation",
                        "androidx.compose.runtime",
                        "androidx.compose.animation",
                    )
            ) {
                useVersion(rootProject.libs.versions.compose.get())
                because("Keep the app, uwuSDK and scripta on the same Compose implementation")
            }
        }
    }
}
