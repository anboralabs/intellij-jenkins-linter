package co.anbora.labs.jenkins.linter.lint.api

import co.anbora.labs.jenkins.linter.ide.toolchain.JenkinsLinterToolchain
import co.anbora.labs.jenkins.linter.ide.toolchain.LinterToolchainService.Companion.toolchainSettings
import co.anbora.labs.jenkins.linter.lint.console.ListOutputStream
import co.anbora.labs.jenkins.linter.lint.exception.LinterException
import co.anbora.labs.jenkins.linter.lint.issue.Issue
import co.anbora.labs.jenkins.linter.lint.issue.IssueMapper
import com.intellij.ide.plugins.PluginManagerCore.getPlugin
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import io.jenkins.jenkinsfile.runner.bootstrap.commands.JenkinsLauncherOptions
import io.jenkins.jenkinsfile.runner.bootstrap.commands.LintJenkinsfileCommand
import io.jenkins.jenkinsfile.runner.bootstrap.commands.PipelineLintOptions
import java.io.PrintStream
import java.nio.file.Paths

object LinterRunner {

    fun lint(
        project: Project,
        toolchain: JenkinsLinterToolchain,
        filesToScan: Set<String>
    ): List<Issue> {
        if (filesToScan.isEmpty()) {
            throw LinterException("Illegal state: filesToScan is empty")
        }

        if (!toolchain.isValid()) {
            throw LinterException("Please install jenkinsfile linter")
        }

        val oldOut = System.out
        val oldErr = System.err

        val listOutput = ListOutputStream()
        val newOut = PrintStream(listOutput)

        System.setOut(newOut)

        val listErrorOutput = ListOutputStream()
        val newErrorOut = PrintStream(listErrorOutput)

        System.setErr(newErrorOut)

        val fileName = filesToScan.first()

        val command = LintJenkinsfileCommand()
        val pipelineLintOptions = PipelineLintOptions()
        pipelineLintOptions.jenkinsfile = Paths.get(fileName).toFile()

        val pluginId = PluginId.getId("co.anbora.labs.jenkinsfile.linter")
        val pluginPath = getPlugin(pluginId)?.pluginPath ?: throw LinterException("Reinstall the plugin, invalid path")

        val launcherOptions = JenkinsLauncherOptions(
            toolchainSettings.toolchain().rootDir(),
            pluginPath,
            toolchainSettings.toolchain().libPayloadDir(),
            toolchainSettings.toolchain().libSetupDir()
        )

        launcherOptions.warDir = toolchain.stdExplodeDir().toFile()
        launcherOptions.pluginsDir = toolchain.stdPluginsDir().toFile()

        command.launcherOptions = launcherOptions
        command.pipelineLintOptions = pipelineLintOptions
        command.call()

        newOut.flush()
        newErrorOut.flush()

        val messages = listOutput.getMessages()
        val messagesError = listErrorOutput.getMessages()

        System.setOut(oldOut)
        System.setErr(oldErr)

        return IssueMapper.apply(fileName, messages + messagesError)
    }
}