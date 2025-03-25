package co.anbora.labs.jenkins.linter.lint.checker

import co.anbora.labs.jenkins.linter.ide.notifications.LinterNotifications
import co.anbora.labs.jenkins.linter.ide.psi.finder.PsiFinderFlavor
import co.anbora.labs.jenkins.linter.ide.toolchain.JenkinsLinterToolchain
import co.anbora.labs.jenkins.linter.lint.exception.LinterException
import co.anbora.labs.jenkins.linter.lint.issue.Issue
import co.anbora.labs.jenkins.linter.lint.api.LinterRunner
import co.anbora.labs.jenkins.linter.lint.api.ProcessResultsThread
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import java.io.InterruptedIOException
import java.util.concurrent.Callable

class ScanFiles(
    val project: Project,
    val toolchain: JenkinsLinterToolchain,
    val psiFinder: List<PsiFinderFlavor>,
    val files: List<Pair<PsiFile, Document>>
): Callable<Map<PsiFile, List<Problem>>> {

    private val log = Logger.getInstance(
        ScanFiles::class.java
    )

    override fun call(): Map<PsiFile, List<Problem>> {
        try {
            return scanCompletedSuccessfully(checkFiles(files.toSet()))
        } catch (ex: InterruptedIOException) {
            log.debug("Scan cancelled by IDE", ex)
            return scanCompletedSuccessfully(emptyMap())
        } catch (ex: InterruptedException) {
            log.debug("Scan cancelled by IDE", ex)
            return scanCompletedSuccessfully(emptyMap())
        } catch (ex: LinterException) {
            log.warn("An error occurred while scanning a file.", ex)
            return scanFailedWithError(ex)
        } catch (ex: Throwable) {
            log.warn("An error occurred while scanning a file.", ex)
            return scanFailedWithError(LinterException("An error occurred while scanning a file.", ex))
        }
    }

    @Throws(InterruptedIOException::class, InterruptedException::class)
    private fun checkFiles(filesToScan: Set<Pair<PsiFile, Document>>): Map<PsiFile, List<Problem>> {
        val scannableFiles = mutableListOf<ScannableFile>()
        try {
            scannableFiles.addAll(ScannableFile.createAndValidate(filesToScan))
            return scan(scannableFiles)
        } finally {
            scannableFiles.forEach(ScannableFile::deleteIfRequired)
        }
    }

    @Throws(InterruptedIOException::class, InterruptedException::class)
    private fun scan(filesToScan: List<ScannableFile>): Map<PsiFile, List<Problem>> {
        val fileNamesToPsiFiles: Map<String, Pair<PsiFile, Document>> = mapFilesToElements(filesToScan)
        val errors: List<Issue> = LinterRunner.lint(project, toolchain, fileNamesToPsiFiles.keys)
        val findThread = ProcessResultsThread(
            psiFinder,
            errors,
            fileNamesToPsiFiles
        )

        ReadAction.run(findThread)
        return findThread.getProblems()
    }

    private fun mapFilesToElements(filesToScan: List<ScannableFile>): Map<String, Pair<PsiFile, Document>> {
        val filePathsToElements: MutableMap<String, Pair<PsiFile, Document>> = HashMap()
        for (scannableFile in filesToScan) {
            filePathsToElements[scannableFile.getAbsolutePath()] = scannableFile.psiFile
        }
        return filePathsToElements
    }

    private fun scanFailedWithError(e: LinterException): Map<PsiFile, List<Problem>> {
        LinterNotifications.errorNotification(e, project)

        return emptyMap()
    }

    private fun scanCompletedSuccessfully(filesToProblems: Map<PsiFile, List<Problem>>): Map<PsiFile, List<Problem>> {
        return filesToProblems
    }
}