package co.anbora.labs.jenkins.linter.ide.plugins

import co.anbora.labs.jenkins.linter.ide.actions.DownloadLinter
import com.intellij.openapi.application.PluginPathManager
import java.nio.file.Path

object LinterPluginManager {
    fun getPluginPath(): Path? {
        val resource = PluginPathManager.getPluginResource(DownloadLinter::class.java, "messages.properties")
        return resource?.toPath()?.parent
    }
}