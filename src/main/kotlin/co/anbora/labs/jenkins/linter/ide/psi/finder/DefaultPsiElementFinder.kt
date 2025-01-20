package co.anbora.labs.jenkins.linter.ide.psi.finder

import co.anbora.labs.jenkins.linter.lint.issue.Issue
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilCore

class DefaultPsiElementFinder: PsiFinderFlavor() {
    override fun findElement(psiFile: PsiFile, document: Document, issue: Issue): PsiElement {
        var lineNumber = issue.line
        val lineCount = document.lineCount
        if (0 == lineCount) {
            return psiFile
        }

        lineNumber = if (lineNumber > 0) lineNumber - 1 else lineNumber
        val position = issue.column

        val lineStartOffset = document.getLineStartOffset(lineNumber)

        val initialPosition = if (position > 0) position - 1 else 0

        return PsiUtilCore.getElementAtOffset(psiFile, lineStartOffset + initialPosition)
    }

    override fun isApplicable(): Boolean = true
}