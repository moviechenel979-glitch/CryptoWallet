plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
}

gradle.buildFinished { result ->
    result.failure?.let { failure ->
        val chain = generateSequence(failure) { it.cause }
            .map { throwable -> "" + throwable.javaClass.simpleName + ": " + (throwable.message ?: "") }
            .joinToString(" | ")
            .replace("%", "%25")
            .replace("\r", "%0D")
            .replace("\n", "%0A")
        logger.lifecycle("::error title=Gradle build failure::$chain")
    }
}
