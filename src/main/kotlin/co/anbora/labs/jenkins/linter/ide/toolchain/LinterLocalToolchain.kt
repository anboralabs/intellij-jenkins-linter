package co.anbora.labs.jenkins.linter.ide.toolchain

import co.anbora.labs.jenkins.linter.ide.notifications.LinterNotifications
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.pathString
import co.anbora.labs.jenkins.linter.zip.ZipExtractor

object LinterLocalToolchain: JenkinsLinterToolchain {

    private val root = PathManager.getConfigDir().resolve("jenkins-tools")

    private const val ZIP = "tools.zip"

    private val runner = root.resolve("tools")
    private val repoDir = runner.resolve("repo")
    private val libDir = runner.resolve("lib")
    private val setupDir = libDir.resolve("setup")
    private val setupJar = setupDir.resolve("setup.jar")
    private val payloadDir = libDir.resolve("payload")
    private val payloadJar = payloadDir.resolve("payload.jar")
    private val libBinDir = runner.resolve("bin")

    private val warDir = root.resolve("war")
    private val explodeDir = root.resolve("exploded-war")
    private val pluginsDir = root.resolve("plugins")

    override fun name(): String = "jenkins-runner"

    override fun version(): String = "2.426.3"

    override fun stdRepoDir(): Path = repoDir

    override fun stdWarDir(): Path = warDir

    override fun stdPluginsDir(): Path = pluginsDir

    override fun stdExplodeDir(): Path = explodeDir

    override fun stdlibDir(): Path = libDir

    override fun stdBinDir(): Path = libBinDir

    override fun rootDir(): Path = root

    override fun runnerDir(): Path = runner

    override fun homePath(): String = root.pathString

    override fun jenkinsWar(): Path = stdWarDir().resolve(version()).resolve("jenkins-war-${version()}.war")

    override fun defaultPluginName(): String = "pipeline-model-definition:2.2218.v56d0cda_37c72"

    override fun isValid(): Boolean {
        return isValidDir(rootDir())
                && isValidRunner()
                && isValidDir(stdWarDir())
                && isValidDir(stdExplodeDir())
                && isValidDir(stdPluginsDir())
    }

    override fun isLinterPluginInstalled(): Boolean {
        return isValidFile(stdPluginsDir().resolve("pipeline-model-definition.jpi"))
    }

    override fun hasWarDownloaded(): Boolean {
        return isValidFile(jenkinsWar())
    }

    override fun setup(project: Project) {
        Files.createDirectories(rootDir())
        Files.createDirectories(stdWarDir())
        Files.createDirectories(stdExplodeDir())
        Files.createDirectories(stdPluginsDir())

        try {
            ZipExtractor.extractZip(ZIP, rootDir())
        } catch (e: Exception) {
            LinterNotifications.errorToolsNotification(project)
        }
    }

    private fun isValidRunner(): Boolean {
        return isValidDir(runnerDir())
                && isValidDir(stdBinDir())
                && isValidDir(stdlibDir())
                && isValidDir(stdRepoDir())
                && isValidDir(setupDir)
                && isValidFile(setupJar)
                && isValidDir(payloadDir)
                && isValidFile(payloadJar)
    }

    private fun isValidDir(dir: Path?): Boolean {
        return dir != null && Files.isDirectory(dir)
    }

    private fun isValidFile(config: Path?): Boolean {
        return config != null && Files.isRegularFile(config)
    }
}