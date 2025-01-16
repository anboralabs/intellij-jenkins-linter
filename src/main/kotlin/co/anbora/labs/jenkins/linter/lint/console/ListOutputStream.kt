package co.anbora.labs.jenkins.linter.lint.console

import java.io.OutputStream


class ListOutputStream: OutputStream() {
    private val messages: MutableList<String> = ArrayList()
    private var currentMessage = StringBuilder()

    override fun write(b: Int) {
        // Convertimos el byte a carácter
        val c = b.toChar()
        if (c == '\n') {
            // Si es un salto de línea, agregamos el mensaje completo a la lista
            messages.add(currentMessage.toString())
            currentMessage = StringBuilder()
        } else {
            // Si no, acumulamos el carácter
            currentMessage.append(c)
        }
    }

    override fun flush() {
        if (currentMessage.length > 0) {
            messages.add(currentMessage.toString())
            currentMessage = StringBuilder()
        }
    }

    fun getMessages(): List<String> {
        return ArrayList(messages)
    }
}