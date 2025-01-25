package co.anbora.labs.jenkins.linter.ide.toolchain

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "JenkinsLinter Home",
    storages = [Storage("NewJenkinsLinterToolchains.xml")]
)
class LinterKnownToolchainService: PersistentStateComponent<LinterKnownToolchainService?> {

    companion object {
        fun getInstance() = service<LinterKnownToolchainService>()
    }

    private var knownToolchains: Set<String> = emptySet()

    @Volatile
    private var linterToolchains: MutableSet<JenkinsLinterToolchain> = mutableSetOf()

    fun knownToolchains(): Set<JenkinsLinterToolchain> = linterToolchains

    fun isKnown(homePath: String): Boolean {
        return knownToolchains.contains(homePath)
    }

    fun add(toolchain: JenkinsLinterToolchain) {
        knownToolchains = knownToolchains + toolchain.homePath()
        linterToolchains.add(toolchain)
    }

    fun remove(toolchain: JenkinsLinterToolchain) {
        knownToolchains = knownToolchains - toolchain.homePath()
        linterToolchains.remove(toolchain)
    }

    override fun getState() = this

    override fun loadState(state: LinterKnownToolchainService) {
        XmlSerializerUtil.copyBean(state, this)
    }

    override fun initializeComponent() {
        val app = ApplicationManager.getApplication()
        app.executeOnPooledThread {
            linterToolchains = knownToolchains.map { JenkinsLinterToolchain.fromPath(it) }.toMutableSet()
        }.get()
    }
}