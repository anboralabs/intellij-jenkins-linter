package co.anbora.labs.jenkins.linter.ide.settings.ui

import co.anbora.labs.jenkins.linter.icons.LinterIcons
import co.anbora.labs.jenkins.linter.ide.toolchain.JenkinsLinterToolchain
import com.intellij.openapi.ui.getPresentablePath
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import javax.swing.JList

class LinterListCellRenderer: ColoredListCellRenderer<JenkinsLinterToolchain>() {
    override fun customizeCellRenderer(
        list: JList<out JenkinsLinterToolchain>,
        value: JenkinsLinterToolchain?,
        index: Int,
        selected: Boolean,
        hasFocus: Boolean,
    ) {
        if (value == null) {
            return
        }
        icon = LinterIcons.FILE
        append(value.version())
        append("  ")
        append(getPresentablePath(value.homePath()), SimpleTextAttributes.GRAYED_ATTRIBUTES)
    }
}