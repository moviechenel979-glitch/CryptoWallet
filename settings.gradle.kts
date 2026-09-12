pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages-TrustWallet"
            url = uri("https://maven.pkg.github.com/trustwallet/wallet-core")
            credentials {
                username = providers.gradleProperty("gpr.user").getOrElse(System.getenv("GPR_USER") ?: "")
                password = providers.gradleProperty("gpr.key").getOrElse(System.getenv("GPR_KEY") ?: "")
            }
        }
    }
}

rootProject.name = "CryptoVault"
include(":app")
