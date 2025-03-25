package co.anbora.labs.jenkins.linter.ide.settings

import co.anbora.labs.jenkins.linter.ide.toolchain.LinterKnownToolchainService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel

class ProjectSettingsForm(private val project: Project, private val model: Model) {

    data class Model(
        var homeLocation: String,
    )

    private val mainPanel: DialogPanel
    private val toolchainChooser = ToolchainChooserComponent(project) {
        LinterKnownToolchainService.getInstance().add(it)
        this.model.homeLocation = it.homePath()
    }

    init {
        mainPanel = panel {
            row {
                cell(toolchainChooser)
                    .align(AlignX.FILL)
            }
        }

        // setup initial location
        model.homeLocation = toolchainChooser.selectedToolchain()?.homePath() ?: ""
    }

    fun createComponent() = mainPanel

    fun reset() {
        toolchainChooser.select(model.homeLocation)
    }
}