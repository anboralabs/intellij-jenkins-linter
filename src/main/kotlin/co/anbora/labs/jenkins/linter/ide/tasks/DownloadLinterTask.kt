package co.anbora.labs.jenkins.linter.ide.tasks

import co.anbora.labs.jenkins.linter.ide.notifications.LinterNotifications
import co.anbora.labs.jenkins.linter.ide.toolchain.LinterLocalToolchain
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.util.io.HttpRequests
import io.jenkins.tools.pluginmanager.config.Config
import io.jenkins.tools.pluginmanager.impl.PluginManager
import io.jenkins.tools.pluginmanager.util.PluginListParser
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.jar.JarFile
import kotlin.io.path.pathString

private const val TOTAL_WAR = 1265

class DownloadLinterTask(
    private val project: Project,
    private val jenkinsVersion: String,
    private val dlFilePath: Path,
    private val explodeWarPath: Path
): Task.Backgroundable(project, "Download jenkinsfile linter", false) {
    override fun run(progressIndicator: ProgressIndicator) {
        try {
            val url = String.format("https://updates.jenkins.io/download/war/%s/jenkins.war", jenkinsVersion)
            HttpRequests.request(url).connect { request ->
                downloadFile(
                    request.inputStream,
                    dlFilePath,
                    progressIndicator,
                    request.connection.getContentLength().toLong()
                )
                this@DownloadLinterTask.uncompress(dlFilePath, explodeWarPath, progressIndicator)
                this@DownloadLinterTask.installPlugins(dlFilePath, progressIndicator)
                LinterNotifications.installSuccessNotification(project)
            }
        } catch (ex: Throwable) {
            LinterNotifications.errorDownloadToolsNotification(ex, project)
        }
    }

    @Throws(IOException::class)
    private fun downloadFile(input: InputStream, dlFileName: Path, progressIndicator: ProgressIndicator, size: Long) {
        val buffer = ByteArray(4096)
        Files.createDirectories(dlFileName.parent)

        Files.newOutputStream(dlFileName).use { output ->
            var accumulated = 0L
            var lg: Int
            while ((input.read(buffer).also { lg = it }) > 0 && !progressIndicator.isCanceled) {
                output.write(buffer, 0, lg)
                accumulated += lg.toLong()
                progressIndicator.fraction = accumulated.toDouble() / size.toDouble()
            }
        }
    }

    @Throws(IOException::class)
    private fun uncompress(dlFilePath: Path, cmd: Path, progressIndicator: ProgressIndicator) {
        if (!Files.exists(cmd)) {
            Files.createDirectories(cmd)
        }
        JarFile(dlFilePath.toFile()).use { jarfile ->
            val enu = jarfile.entries()
            // Get current working directory path
            val destDir = cmd.toFile()

            var counter = 0;

            while (enu.hasMoreElements()) {
                val je = enu.nextElement()
                var file = File(destDir, je.name)
                if (!file.exists()) {
                    file.parentFile.mkdirs()
                    file = File(destDir, je.name)
                }

                counter++
                progressIndicator.fraction = counter.toDouble() / TOTAL_WAR
                progressIndicator.text = "Extracting files"
                progressIndicator.text2 = "Extracting files $counter / $TOTAL_WAR"

                if (je.isDirectory) {
                    continue
                }
                val inputStream = jarfile.getInputStream(je)

                FileOutputStream(file).use { fo ->
                    while (inputStream.available() > 0) {
                        fo.write(inputStream.read())
                    }
                    fo.close()
                    inputStream.close()
                }
            }
        }
    }

    @Throws(Throwable::class)
    private fun installPlugins(dlFilePath: Path, progressIndicator: ProgressIndicator) {
        val pluginParser = PluginListParser(false)

        val config = Config.builder()
            .withPluginDir(LinterLocalToolchain.stdPluginsDir().toFile())
            .withJenkinsWar(dlFilePath.pathString)
            .withPlugins(ArrayList(pluginParser.parsePluginsFromCliOption(arrayOf(LinterLocalToolchain.defaultPluginName()))))
            .withDoDownload(true)
            .withProgressIndicator(progressIndicator)
            .build()

        PluginManager(config).use { pluginManager ->
            pluginManager.start()
        }
    }

}