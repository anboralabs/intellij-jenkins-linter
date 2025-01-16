package co.anbora.labs.jenkins.linter.lint.api

import co.anbora.labs.jenkins.linter.ide.toolchain.JenkinsLinterToolchain
import co.anbora.labs.jenkins.linter.lint.console.ListOutputStream
import co.anbora.labs.jenkins.linter.lint.exception.LinterException
import co.anbora.labs.jenkins.linter.lint.issue.Issue
import co.anbora.labs.jenkins.linter.lint.issue.IssueMapper
import com.intellij.openapi.project.Project
import io.jenkins.jenkinsfile.runner.bootstrap.commands.*
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

        val listOutput = ListOutputStream()
        val newOut = PrintStream(listOutput)

        System.setOut(newOut)

        val fileName = filesToScan.first()

        val command = LintJenkinsfileCommand()
        val pipelineLintOptions = PipelineLintOptions()
        pipelineLintOptions.jenkinsfile = Paths.get(fileName).toFile()

        val launcherOptions = JenkinsLauncherOptions()
        launcherOptions.warDir = toolchain.stdExplodeDir()?.toFile()
        launcherOptions.pluginsDir = toolchain.stdPluginsDir()?.toFile()

        command.launcherOptions = launcherOptions
        command.pipelineLintOptions = pipelineLintOptions
        command.call()

        newOut.flush()
        val messages = listOutput.getMessages()

        System.setOut(oldOut)

        return IssueMapper.apply(fileName, messages)
    }
}