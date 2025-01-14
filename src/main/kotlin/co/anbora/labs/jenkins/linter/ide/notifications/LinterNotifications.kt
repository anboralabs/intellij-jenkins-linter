package co.anbora.labs.jenkins.linter.ide.notifications

import co.anbora.labs.jenkins.linter.icons.LinterIcons
import co.anbora.labs.jenkins.linter.ide.actions.BuyLicense
import co.anbora.labs.jenkins.linter.ide.actions.DownloadLinter
import co.anbora.labs.jenkinsFile.ide.notifications.JenkinsFileNotifications.createNotification
import co.anbora.labs.jenkinsFile.ide.notifications.JenkinsFileNotifications.showNotification
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.Project

object LinterNotifications {

    private const val PLUGIN_NAME = "Jenkins Linter Plugin"

    @JvmStatic
    private fun createLinterNotification(
        title: String,
        content: String,
        type: NotificationType,
        vararg actions: AnAction
    ): Notification {
        return createNotification(
            title,
            content,
            type,
            "JenkinsOfflineLinter_Notification",
            LinterIcons.NOTIFICATION,
            *actions
        )
    }

    @JvmStatic
    fun supportNotification(project: Project?) {
        val notification = createLinterNotification(
            "Support $PLUGIN_NAME",
            "Buy the license; 10 USD per year",
            NotificationType.WARNING,
            BuyLicense()
        )

        showNotification(notification, project)
    }

    @JvmStatic
    fun errorDownloadToolsNotification(ex: Throwable, project: Project?) {
        val notification = createLinterNotification(
            PLUGIN_NAME,
            "There was an issue downloading the linter. ${ex.message}",
            NotificationType.ERROR
        )

        showNotification(notification, project)
    }

    @JvmStatic
    fun errorToolsNotification(project: Project?) {
        val notification = createLinterNotification(
            PLUGIN_NAME,
            "There was an issue extracting tools, try reinstalling the plugin.",
            NotificationType.ERROR
        )

        showNotification(notification, project)
    }

    @JvmStatic
    fun downloadLinterNotification(project: Project?) {
        val notification = createLinterNotification(
            PLUGIN_NAME,
            "Download Jenkinsfile Linter.",
            NotificationType.WARNING,
            DownloadLinter()
        )

        showNotification(notification, project)
    }

    @JvmStatic
    fun installPipelinePluginNotification(project: Project?) {
        val notification = createLinterNotification(
            PLUGIN_NAME,
            "Extract and Install Jenkinsfile Linter.",
            NotificationType.WARNING,
            DownloadLinter()
        )

        showNotification(notification, project)
    }
}