package co.anbora.labs.jenkins.linter.lint

import co.anbora.labs.jenkins.linter.ide.externalAnnotator.LinterExternalAnnotator
import co.anbora.labs.jenkins.linter.ide.psi.finder.PsiFinderFlavor
import co.anbora.labs.jenkins.linter.ide.toolchain.JenkinsLinterToolchain
import co.anbora.labs.jenkins.linter.lint.checker.ScanFiles
import com.intellij.openapi.diagnostic.Logger

object Linter {

    private val log: Logger = Logger.getInstance(
        Linter::class.java
    )

    fun lint(
        state: LinterExternalAnnotator.State,
        toolchain: JenkinsLinterToolchain,
        psiFinder: List<PsiFinderFlavor>,
    ): LinterExternalAnnotator.Results {
        log.info("jenkins linter executing")

        val psiFile = state.psiWithDocument.first
        val project = psiFile.project

        val scanFiles = ScanFiles(
            project, toolchain, psiFinder, listOf(state.psiWithDocument)
        )

        val map = scanFiles.call()

        if (map.isEmpty()) {
            return LinterExternalAnnotator.NO_PROBLEMS_FOUND
        }

        return LinterExternalAnnotator.Results(map[psiFile] ?: emptyList())
    }
}