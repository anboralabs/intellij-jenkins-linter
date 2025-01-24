package co.anbora.labs.jenkins.linter.ide.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import javax.swing.JComponent

class JenkinsLinterSettingsConfigurable(private val project: Project) : Configurable {



    override fun createComponent(): JComponent? {
        TODO("Not yet implemented")
    }

    override fun isModified(): Boolean {
        TODO("Not yet implemented")
    }

    override fun apply() {
        TODO("Not yet implemented")
    }

    override fun getDisplayName(): String = "Jenkins Linter Offline"
}