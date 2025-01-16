package co.anbora.labs.jenkins.linter.lint.issue

import java.util.function.BiFunction
import java.util.regex.Matcher
import java.util.regex.Pattern

object IssueMapper: BiFunction<String, List<String>, List<Issue>> {
    var pattern: String = "WorkflowScript: \\d+: (.*?)\\. (.*?) @ line (\\d+), column (\\d+)"

    override fun apply(path: String, t: List<String>): List<Issue> {
        val r: Pattern = Pattern.compile(pattern)

        return t.map {
            val m: Matcher = r.matcher(it)
            if (m.find()) {
                val message = m.group(1).trim()
                val hint = m.group(2).trim()
                val line = m.group(3).toInt()
                val column = m.group(4).toInt()

                Issue(
                    path,
                    message,
                    hint,
                    line,
                    column,
                )
            } else {
                null
            }
        }.mapNotNull { it }

    }
}