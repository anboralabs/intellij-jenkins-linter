package co.anbora.labs.jenkins.linter.icons

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object LinterIcons {

    val NOTIFICATION = getIcon("notification.svg")

    private fun getIcon(path: String): Icon {
        return IconLoader.findIcon("/icons/$path", LinterIcons::class.java.classLoader) as Icon
    }
}