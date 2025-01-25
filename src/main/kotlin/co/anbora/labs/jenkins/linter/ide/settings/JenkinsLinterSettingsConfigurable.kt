package co.anbora.labs.jenkins.linter.ide.settings

import co.anbora.labs.jenkins.linter.ide.toolchain.JenkinsLinterToolchain
import co.anbora.labs.jenkins.linter.ide.toolchain.LinterToolchainService.Companion.toolchainSettings
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import javax.swing.JComponent

class JenkinsLinterSettingsConfigurable(private val project: Project) : Configurable {

    private val mainPanel: DialogPanel
    private val model = ProjectSettingsForm.Model(
        homeLocation = "",
    )
    private val settingsForm = ProjectSettingsForm(project, model)

    init {
        mainPanel = settingsForm.createComponent()
    }

    override fun createComponent(): JComponent = mainPanel

    override fun getPreferredFocusedComponent(): JComponent = mainPanel

    override fun isModified(): Boolean {
        mainPanel.apply()

        return model.homeLocation != toolchainSettings.toolchain().homePath()
    }

    override fun apply() {
        mainPanel.apply()

        validateSettings()

        toolchainSettings.setToolchain(JenkinsLinterToolchain.fromPath(model.homeLocation))
    }

    private fun validateSettings() {
        val issues = mainPanel.validateAll()
        if (issues.isNotEmpty()) {
            throw ConfigurationException(issues.first().message)
        }
    }

    override fun reset() {
        val settings = toolchainSettings

        with(model) {
            homeLocation = settings.toolchain().homePath()
        }

        settingsForm.reset()
        mainPanel.reset()
    }

    override fun getDisplayName(): String = "Jenkins Linter Offline"

    companion object {
        @JvmStatic
        fun show(project: Project) {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, JenkinsLinterSettingsConfigurable::class.java)
        }
    }
}