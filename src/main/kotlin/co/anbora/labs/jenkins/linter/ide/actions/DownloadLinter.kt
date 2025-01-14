package co.anbora.labs.jenkins.linter.ide.actions

import co.anbora.labs.jenkins.linter.ide.tasks.DownloadLinterTask
import co.anbora.labs.jenkins.linter.ide.toolchain.LinterLocalToolchain
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.ProjectManager

class DownloadLinter: NotificationAction("Download") {
    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
        val project = e.project ?: ProjectManager.getInstance().defaultProject
        DownloadLinterTask(
            project,
            LinterLocalToolchain.version(),
            LinterLocalToolchain.jenkinsWar(),
            LinterLocalToolchain.stdExplodeDir()
        ).queue()
        notification.expire()
    }
}