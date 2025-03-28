package co.anbora.labs.jenkins.linter.lint.checker

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.psi.PsiFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.stream.Collectors

class ScannableFile(
    val psiFile: Pair<PsiFile, Document>
) {

    private val realFile: File

    init {
        val document = psiFile.second
        val tempFile = Files.createTempFile("Jenkinsfile", "")
        realFile = tempFile.toFile().also {
            it.deleteOnExit()
        }
        Files.write(tempFile, document.text.toByteArray())
    }

    private fun deleteFileIfRequired() {
        if (!realFile.exists()) {
            return
        }

        realFile.delete()
    }

    fun getAbsolutePath(): String {
        return realFile.absolutePath
    }

    companion object {
        private val log = Logger.getInstance(
            ScanFiles::class.java
        )

        fun createAndValidate(
            psiFiles: Collection<Pair<PsiFile, Document>>
        ): List<ScannableFile> {
            val action = ThrowableComputable<List<ScannableFile>, RuntimeException> {
                    psiFiles.stream()
                        .map(ScannableFile::create)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection { CopyOnWriteArrayList() })
                }
            return ReadAction.compute(action)
        }

        fun create(psiFile: Pair<PsiFile, Document>): ScannableFile? {
            try {
                val fileAction = CreateScannableFileAction(psiFile)
                ReadAction.run(fileAction)

                val failure = fileAction.getFailure()

                if (failure != null) {
                    throw failure
                }

                return fileAction.getFile()
            } catch (e: IOException) {
                log.warn("Failure when creating temporary file", e)
                return null
            }
        }

        fun deleteIfRequired(scannableFile: ScannableFile?) {
            scannableFile?.deleteFileIfRequired()
        }
    }

}
