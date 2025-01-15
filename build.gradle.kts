fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.24"
    id("org.jetbrains.intellij.platform") version "2.2.1"
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
    implementation("info.picocli:picocli:4.7.5")
    implementation("commons-io:commons-io:2.18.0")
    implementation("org.jenkins-ci:version-number:1.12")
    implementation("io.jenkins.lib:support-log-formatter:1.2")
    implementation("com.github.spotbugs:spotbugs-annotations:3.1.3")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("args4j:args4j:2.37")
    implementation("org.assertj:assertj-core:3.27.2")
    implementation("net.bytebuddy:byte-buddy:1.15.11")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("commons-validator:commons-validator:1.9.0")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("commons-codec:commons-codec:1.17.1")
    implementation("org.json:json:20241224")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.2")
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

    publishing {
        token = environment("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = properties("pluginVersion").map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }

    buildSearchableOptions = false
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }
}
