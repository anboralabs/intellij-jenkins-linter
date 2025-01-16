package co.anbora.labs.jenkins.linter.ide.toolchain

import com.intellij.openapi.project.Project
import java.nio.file.Path

interface JenkinsLinterToolchain {
    fun name(): String
    fun version(): String
    fun stdRepoDir(): Path?
    fun stdWarDir(): Path?
    fun stdPluginsDir(): Path?
    fun stdExplodeDir(): Path?
    fun stdlibDir(): Path?
    fun stdBinDir(): Path?
    fun libSetupDir(): Path?
    fun libPayloadDir(): Path?
    fun rootDir(): Path?
    fun runnerDir(): Path?
    fun homePath(): String
    fun jenkinsWar(): Path?
    fun defaultPluginName(): String
    fun isValid(): Boolean
    fun isValidSetup(): Boolean
    fun isLinterPluginInstalled(): Boolean
    fun hasWarDownloaded(): Boolean
    fun setup(project: Project)
}