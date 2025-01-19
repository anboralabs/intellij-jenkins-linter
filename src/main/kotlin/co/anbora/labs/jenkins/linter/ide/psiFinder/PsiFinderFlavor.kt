package co.anbora.labs.jenkins.linter.ide.psiFinder

import co.anbora.labs.jenkins.linter.lint.issue.Issue
import com.intellij.openapi.editor.Document
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

abstract class PsiFinderFlavor {

    abstract fun findElement(psiFile: PsiFile, document: Document, issue: Issue): PsiElement?

    protected open fun isApplicable(): Boolean = false

    companion object {
        private val EP_NAME: ExtensionPointName<PsiFinderFlavor> =
            ExtensionPointName.create("co.anbora.labs.jenkinsfile.linter.elementFinder")

        fun getApplicableFlavor(): List<PsiFinderFlavor> =
            EP_NAME.extensionList.filter { it.isApplicable() }
    }

}
