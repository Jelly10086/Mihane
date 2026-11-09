import org.gradle.api.Project

object ProjectConfig {
    const val APP_NAME = "mihane"
    const val PACKAGE_NAME = "rinn.rei.mihane"
    const val VERSION_NAME = "1.3.0"

    object Android {
        const val TARGET_SDK = 37
        const val MIN_SDK = 31
        const val COMPILE_SDK = 37
        const val COMPILE_SDK_MINOR = 2
    }
}

private const val FALLBACK_VERSION_CODE = 1

/** CI使用递增版本号，本地沿用提交数 */
fun Project.getGitVersionCode(): Int {
    providers.environmentVariable("CI_VERSION_CODE").orNull?.let { value ->
        return requireNotNull(value.toIntOrNull()?.takeIf { it in 1..2_100_000_000 }) {
            "CI_VERSION_CODE must be an integer between 1 and 2100000000"
        }
    }
    return runCatching {
            providers
                .exec {
                    commandLine("git", "rev-list", "--count", "HEAD")
                    isIgnoreExitValue = true
                }
                .standardOutput
                .asText
                .get()
                .trim()
                .toInt()
        }
        .getOrElse {
            logger.warn(
                "git rev-list failed (${it.message}); versionCode falls back to $FALLBACK_VERSION_CODE"
            )
            FALLBACK_VERSION_CODE
        }
}
