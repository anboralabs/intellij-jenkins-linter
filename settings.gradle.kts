pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "intellij-jenkinsfile-linter"
include("plugin-installation-manager-tool")
include("jenkinsfile-runner")
