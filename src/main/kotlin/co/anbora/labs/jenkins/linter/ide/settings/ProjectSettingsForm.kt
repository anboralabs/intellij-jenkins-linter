package co.anbora.labs.jenkins.linter.ide.settings

import com.intellij.openapi.project.Project

class ProjectSettingsForm(private val project: Project, private val model: Model) {

    data class Model(
        var homeLocation: String,
    )
}