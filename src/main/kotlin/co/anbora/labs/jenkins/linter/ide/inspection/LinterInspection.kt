package co.anbora.labs.jenkins.linter.ide.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ex.ExternalAnnotatorBatchInspection

class LinterInspection: LocalInspectionTool(), ExternalAnnotatorBatchInspection {

    companion object {
        const val INSPECTION_SHORT_NAME = "JenkinsfileLinter"
    }

    override fun getShortName(): String = INSPECTION_SHORT_NAME
}