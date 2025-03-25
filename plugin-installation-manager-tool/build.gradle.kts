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
    implementation("commons-io:commons-io:2.18.0")
    implementation("org.jenkins-ci:version-number:1.12")
    implementation("com.github.spotbugs:spotbugs-annotations:3.1.3")
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

tasks.test {
    useJUnitPlatform()
}