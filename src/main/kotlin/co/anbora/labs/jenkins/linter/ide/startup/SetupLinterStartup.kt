package co.anbora.labs.jenkins.linter.ide.startup

import co.anbora.labs.jenkins.linter.ide.notifications.LinterNotifications
import co.anbora.labs.jenkins.linter.ide.toolchain.LinterLocalToolchain
import co.anbora.labs.jenkins.linter.license.CheckLicense
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class SetupLinterStartup: ProjectActivity {
    override suspend fun execute(project: Project) {
        val licensed = CheckLicense.isLicensed() ?: false

        if (!licensed) {
            LinterNotifications.supportNotification(project)
        }

        if (!LinterLocalToolchain.isValid()) {
            LinterLocalToolchain.setup(project)
        }

        if (LinterLocalToolchain.isValid() && !LinterLocalToolchain.hasWarDownloaded()) {
            LinterNotifications.downloadLinterNotification(project)
        }

        if (LinterLocalToolchain.hasWarDownloaded() && !LinterLocalToolchain.isLinterPluginInstalled()) {
            LinterNotifications.installPipelinePluginNotification(project)
        }
    }
}