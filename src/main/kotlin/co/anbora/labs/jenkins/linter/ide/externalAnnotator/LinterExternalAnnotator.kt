package co.anbora.labs.jenkins.linter.ide.externalAnnotator

import co.anbora.labs.jenkins.linter.ide.inspection.LinterInspection
import co.anbora.labs.jenkins.linter.ide.psiFinder.PsiFinderFlavor
import co.anbora.labs.jenkins.linter.ide.toolchain.LinterLocalToolchain
import co.anbora.labs.jenkins.linter.lint.Linter
import co.anbora.labs.jenkins.linter.lint.checker.Problem
import co.anbora.labs.jenkinsFile.lang.JenkinsFileType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiFile

class LinterExternalAnnotator: ExternalAnnotator<LinterExternalAnnotator.State, LinterExternalAnnotator.Results>() {

    data class State(
        val psiWithDocument: Pair<PsiFile, Document>,
    )

    data class Results(val issues: List<Problem>)

    private val log = Logger.getInstance(
        LinterExternalAnnotator::class.java
    )

    companion object {
        val NO_PROBLEMS_FOUND: Results = Results(emptyList())
    }

    override fun getPairedBatchInspectionShortName(): String = LinterInspection.INSPECTION_SHORT_NAME

    override fun collectInformation(file: PsiFile): State? {
        val vfile = file.virtualFile

        if (vfile == null) {
            log.info("Missing vfile for $file")
            return null
        }

        if (file.fileType !is JenkinsFileType) {
            return null
        }

        // collect the document here because doAnnotate has no read access to the file document manager
        val document = FileDocumentManager.getInstance().getDocument(vfile)

        if (document == null) {
            log.info("Missing document")
            return null
        }

        return State(Pair(file, document))
    }

    override fun doAnnotate(collectedInfo: State?): Results {
        val psiWithDocument = collectedInfo?.psiWithDocument ?: return NO_PROBLEMS_FOUND

        if (LinterLocalToolchain.isValid()) {
            return Linter.lint(collectedInfo, LinterLocalToolchain, PsiFinderFlavor.getApplicableFlavor())
        }

        return NO_PROBLEMS_FOUND
    }

    override fun apply(file: PsiFile, annotationResult: Results?, holder: AnnotationHolder) {
        if (annotationResult == null || !file.isValid) {
            return
        }

        for (problem in annotationResult.issues) {
            log.debug(problem.getMessage())
            problem.createAnnotation(holder)
        }
    }
}