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
                username = providers.gradleProperty("gpr.user")
                    .orElse(
                        System.getenv("GPR_USER")?.takeIf { it.isNotBlank() }
                            ?: System.getenv("GITHUB_ACTOR")?.takeIf { it.isNotBlank() }
                            ?: ""
                    )
                    .get()
                password = providers.gradleProperty("gpr.key")
                    .orElse(
                        System.getenv("GPR_KEY")?.takeIf { it.isNotBlank() }
                            ?: System.getenv("GITHUB_TOKEN")?.takeIf { it.isNotBlank() }
                            ?: ""
                    )
                    .get()
            }
        }
    }
}

rootProject.name = "CryptoVault"
include(":app")
