package co.anbora.labs.jenkins.linter.ide.toolchain

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Attribute

@State(
    name = "Jenkinsfile Linter",
    storages = [Storage("JenkinsLinters.xml")]
)
class LinterToolchainService: PersistentStateComponent<LinterToolchainService.ToolchainState?> {

    class ToolchainState {
        @Attribute("location")
        var linterLocation: String = ""
    }

    companion object {
        val toolchainSettings
            get() = service<LinterToolchainService>()
    }

    private var state = ToolchainState()

    @Volatile
    private var toolchain: JenkinsLinterToolchain = JenkinsLinterToolchain.DEFAULT

    fun setToolchain(newToolchain: JenkinsLinterToolchain) {
        toolchain = newToolchain
        state.linterLocation = newToolchain.homePath()
    }

    fun toolchain(): JenkinsLinterToolchain = toolchain

    override fun getState(): ToolchainState = state

    override fun loadState(state: ToolchainState) {
        XmlSerializerUtil.copyBean(state, this.state)
    }

    override fun initializeComponent() {
        val app = ApplicationManager.getApplication()
        app.executeOnPooledThread {
            val currentLocation = state.linterLocation
            if (toolchain == JenkinsLinterToolchain.DEFAULT && currentLocation.isNotEmpty()) {
                setToolchain(JenkinsLinterToolchain.fromPath(currentLocation))
            }
        }.get()
    }
}