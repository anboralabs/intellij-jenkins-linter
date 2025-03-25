package co.anbora.labs.jenkins.linter.ide.externalAnnotator

import co.anbora.labs.jenkins.linter.ide.externalAnnotator.LinterExternalAnnotator.Results
import co.anbora.labs.jenkins.linter.ide.externalAnnotator.LinterExternalAnnotator.State
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.psi.PsiFile

class ExternalAnnotator: ExternalAnnotator<State, Results>() {
    override fun collectInformation(file: PsiFile): State? = LinterExternalAnnotator.collectInformation(file)

    override fun doAnnotate(collectedInfo: State?): Results = LinterExternalAnnotator.doAnnotate(collectedInfo)

    override fun apply(file: PsiFile, annotationResult: Results?, holder: AnnotationHolder) = LinterExternalAnnotator.apply(file, annotationResult, holder)
}