package co.anbora.labs.jenkins.linter.ide.settings

import co.anbora.labs.jenkins.linter.ide.notifications.LinterNotifications
import co.anbora.labs.jenkins.linter.ide.settings.ui.LinterListCellRenderer
import co.anbora.labs.jenkins.linter.ide.toolchain.JenkinsLinterToolchain
import co.anbora.labs.jenkins.linter.ide.toolchain.LinterKnownToolchainService
import co.anbora.labs.jenkins.linter.ide.toolchain.LinterToolchainService.Companion.toolchainSettings
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.ComponentWithBrowseButton
import com.intellij.util.ui.SwingHelper
import com.jgoodies.common.base.Objects
import java.awt.event.ItemEvent
import javax.swing.DefaultComboBoxModel

class ToolchainChooserComponent(
    private val project: Project,
    private val onSelectAction: (JenkinsLinterToolchain) -> Unit
): ComponentWithBrowseButton<ComboBox<JenkinsLinterToolchain>>(ComboBox<JenkinsLinterToolchain>(), null) {

    private val comboBox = childComponent
    private val knownToolchains get() = LinterKnownToolchainService.getInstance().knownToolchains()
    private var knownToolchainInfos = LinterKnownToolchainService.getInstance().knownToolchains()

    private var myLastSelectedItem: JenkinsLinterToolchain = toolchainSettings.toolchain()
    private val myModel: ToolchainComboBoxModel = ToolchainComboBoxModel()

    init {
        this.addActionListener {
            val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
            FileChooser.chooseFile(descriptor, null, null) { file ->
                val newToolchain = JenkinsLinterToolchain.fromPath(file.path)
                this.onSelectAction(newToolchain)
                this.updateDropDownList()
                this.comboBox.selectedItem = newToolchain
                if (!newToolchain.isValid()) {
                    if (!newToolchain.isValidSetup()) {
                        newToolchain.setup(project)
                    }

                    if (!newToolchain.isValid()) {
                        LinterNotifications.downloadLinterNotification(project)
                    }
                }
            }
        }

        this.comboBox.setModel(this.myModel)
        this.comboBox.renderer = LinterListCellRenderer()
        this.comboBox.setMinimumAndPreferredWidth(0)
        this.myModel.addElement(toolchainSettings.toolchain())
        this.myModel.selectedItem = toolchainSettings.toolchain()
        this.updateDropDownList()
        this.comboBox.addItemListener { e: ItemEvent ->
            if (e.stateChange == 1) {
                this.handleSelectedItemChange()
            }
        }
    }

    private fun updateDropDownList() {
        val toolchains: LinkedHashSet<JenkinsLinterToolchain> = LinkedHashSet(knownToolchainInfos)
        toolchains.add(toolchainSettings.toolchain())
        SwingHelper.updateItems(this.comboBox, toolchains.toList(), null)

        val selected = toolchainSettings.toolchain()

        if (!Objects.equals(this.comboBox.selectedItem, selected)) {
            this.comboBox.selectedItem = selected
            this.handleSelectedItemChange()
        }
    }

    private fun handleSelectedItemChange() {
        val selected = this.getToolchainRef()
        if (this.myLastSelectedItem != selected && selected.isValid()) {
            this.myLastSelectedItem = selected
            this@ToolchainChooserComponent.onSelectAction(selected)
        }
    }

    private fun getToolchainRef(): JenkinsLinterToolchain {
        var ref = this.comboBox.selectedItem as? JenkinsLinterToolchain
        if (ref == null) {
            ref = JenkinsLinterToolchain.DEFAULT
        }
        return ref
    }

    fun selectedToolchain(): JenkinsLinterToolchain? {
        return comboBox.selectedItem as? JenkinsLinterToolchain
    }

    fun refresh() {
        comboBox.removeAllItems()
        knownToolchainInfos = knownToolchains

        updateDropDownList()
    }

    fun select(location: String) {
        if (location.isEmpty()) {
            comboBox.selectedItem = JenkinsLinterToolchain.DEFAULT
            return
        }

        val infoToSelect = knownToolchainInfos.find { it.homePath() == location } ?: return
        comboBox.selectedItem = infoToSelect
    }

    private inner class ToolchainComboBoxModel: DefaultComboBoxModel<JenkinsLinterToolchain>()
}
