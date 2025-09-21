import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.9.0"
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

// Set the JVM language level used to build the project.
kotlin {
    jvmToolchain(properties("javaVersion").get().toInt())
}

// Configure project's dependencies
repositories {
    mavenCentral()
    maven("https://repo.jenkins-ci.org/public/")
    maven { url = uri("https://jitpack.io") }
    // IntelliJ Platform Gradle Plugin Repositories Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-repositories-extension.html
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        create(properties("platformType"), properties("platformVersion"))

        // Plugin Dependencies. Uses `platformBundledPlugins` property from the gradle.properties file for bundled IntelliJ Platform plugins.
        bundledPlugins(properties("platformBundledPlugins").map { it.split(',') })

        // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file for plugin from JetBrains Marketplace.
        plugins(properties("platformPlugins").map { it.split(',') })

        pluginVerifier()
        // testFramework(TestFrameworkType.Platform.JUnit4)
    }
    implementation(project(":jenkinsfile-runner"))
    implementation(project(":plugin-installation-manager-tool"))
    implementation("commons-io:commons-io:2.18.0")
    implementation("org.jenkins-ci:version-number:1.12")
    implementation("com.github.spotbugs:spotbugs-annotations:3.1.3")
    implementation("args4j:args4j:2.37")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("org.json:json:20241224")
}

// Configure IntelliJ Platform Gradle Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html
intellijPlatform {
    pluginConfiguration {
        version = properties("pluginVersion")
        description = file("src/main/html/description.html").inputStream().readBytes().toString(Charsets.UTF_8)
        changeNotes = file("src/main/html/change-notes.html").inputStream().readBytes().toString(Charsets.UTF_8)

        ideaVersion {
            sinceBuild = properties("pluginSinceBuild")
            untilBuild = properties("pluginUntilBuild")
        }
    }

    signing {
        certificateChain = environment("CERTIFICATE_CHAIN")
        privateKey = environment("PRIVATE_KEY")
        password = environment("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = environment("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = properties("pluginVersion").map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
    }

    pluginVerification {
        ides {
            create(IntelliJPlatformType.IntellijIdeaUltimate, properties("platformVersion").get())
            recommended()
        }
    }

    buildSearchableOptions = false
}

// Ensure signing is not silently skipped when publishing
val hasSigningCreds = listOf("CERTIFICATE_CHAIN", "PRIVATE_KEY", "PRIVATE_KEY_PASSWORD")
    .all { environment(it).isPresent }

tasks {

    // Fail early if trying to publish without signing credentials
    named("publishPlugin") {
        dependsOn("signPlugin")
        doFirst {
            if (!hasSigningCreds) {
                throw GradleException(
                    "Missing signing credentials. Set CERTIFICATE_CHAIN, PRIVATE_KEY, and PRIVATE_KEY_PASSWORD in the environment to sign before publishing."
                )
            }
        }
    }

    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }
}
