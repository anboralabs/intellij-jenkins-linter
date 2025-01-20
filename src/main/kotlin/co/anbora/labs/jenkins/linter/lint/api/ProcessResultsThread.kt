package co.anbora.labs.jenkins.linter.lint.api

import co.anbora.labs.jenkins.linter.ide.psi.finder.PsiFinderFlavor
import co.anbora.labs.jenkins.linter.lint.checker.Problem
import co.anbora.labs.jenkins.linter.lint.issue.Issue
import co.anbora.labs.jenkins.linter.lint.checker.PsiProblem
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiInvalidElementAccessException
import com.intellij.util.ThrowableRunnable
import java.util.*

class ProcessResultsThread(
    val psiFinder: List<PsiFinderFlavor>,
    val errors: List<Issue>,
    val fileNamesToPsiFiles: Map<String, Pair<PsiFile, Document>>
): ThrowableRunnable<RuntimeException> {

    private val log = Logger.getInstance(
        ProcessResultsThread::class.java
    )

    private val problems: MutableMap<PsiFile, MutableList<Problem>> = HashMap()

    override fun run() {
        val lineLengthCachesByFile: MutableMap<PsiFile, MutableList<Int>> = HashMap()

        for (event in errors) {
            val pair = fileNamesToPsiFiles[event.filepath]
            val psiFile = pair?.first
            val document = pair?.second
            if (pair == null || psiFile == null || document == null) {
                log.info(("Could not find mapping for file: " + event.filepath) + " in " + fileNamesToPsiFiles)
                return
            }

            var lineLengthCache = lineLengthCachesByFile[psiFile]
            if (lineLengthCache == null) {
                // we cache the offset of each line as it is created, so as to
                // avoid retreating ground we've already covered.
                lineLengthCache = ArrayList()
                lineLengthCache.add(0) // line 1 is offset 0

                lineLengthCachesByFile[psiFile] = lineLengthCache
            }

            processEvent(psiFile, document, lineLengthCache, event)
        }
    }

    private fun processEvent(psiFile: PsiFile, document: Document, lineLengthCache: List<Int>, event: Issue) {
        val psiElement = psiFinder.mapNotNull { it.findElement(psiFile, document, event) }

        psiElement.forEach {
            addProblemTo(it, psiFile, event)
        }
    }

    private fun addProblemTo(psiElement: PsiElement, psiFile: PsiFile, event: Issue) {
        try {
            addProblem(
                psiFile,
                PsiProblem(
                    "Error: ${event.message}, Hint: ${event.hint}",
                    psiElement,
                    HighlightSeverity.ERROR,
                    null
                )
            )
        } catch (ex: PsiInvalidElementAccessException) {
            log.warn("Element access failed", ex)
        }
    }

    fun getProblems(): Map<PsiFile, List<Problem>> {
        return Collections.unmodifiableMap(problems)
    }

    private fun addProblem(psiFile: PsiFile, problem: Problem) {
        var problemsForFile: MutableList<Problem>? = problems[psiFile]
        if (problemsForFile == null) {
            problemsForFile = ArrayList()
            problems[psiFile] = problemsForFile
        }

        problemsForFile.add(problem)
    }
}
