package co.anbora.labs.jenkins.linter.ide.startup

import co.anbora.labs.jenkins.linter.ide.notifications.LinterNotifications
import co.anbora.labs.jenkins.linter.ide.toolchain.LinterToolchainService.Companion.toolchainSettings
import co.anbora.labs.jenkins.linter.license.CheckLicense
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class SetupLinterStartup: ProjectActivity {
    override suspend fun execute(project: Project) {
        val licensed = CheckLicense.isLicensed() ?: false

        if (!licensed) {
            CheckLicense.requestLicense("Support Plugin")
            LinterNotifications.supportNotification(project)
        }

        val toolchain = toolchainSettings.toolchain()

        if (!toolchain.isValidSetup()) {
            toolchain.setup(project)
        }

        if (!toolchain.isValid()) {
            LinterNotifications.downloadLinterNotification(project)
        }
    }
}