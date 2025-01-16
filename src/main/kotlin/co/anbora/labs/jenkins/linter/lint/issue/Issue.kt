package co.anbora.labs.jenkins.linter.lint.issue

data class Issue(
    var filepath: String,
    val message: String,
    val hint: String,
    val line: Int,
    val column: Int
)
