plugins {
    id("java")
}

group = "co.anbora.labs"
version = "1.0.1"

repositories {
    maven("https://repo.jenkins-ci.org/public/")
    mavenCentral()
}

dependencies {
    implementation("info.picocli:picocli:4.7.5")
    implementation("commons-io:commons-io:2.18.0")
    implementation("org.jenkins-ci:version-number:1.12")
    implementation("io.jenkins.lib:support-log-formatter:1.2")
    implementation("com.github.spotbugs:spotbugs-annotations:3.1.3")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("args4j:args4j:2.37")
}

tasks.test {
    useJUnitPlatform()
}