package com.github.pythondocgen.services

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.PyFunction

object DocstringInserter {

    fun insertDocstring(
        project: Project,
        editor: Editor,
        functionInfo: PythonFunctionExtractor.FunctionInfo,
        generatedDocstring: String
    ) {
        val document = editor.document
        val function = functionInfo.function
        val indentation = PythonFunctionExtractor.getBodyIndentation(function)
        val formattedDocstring = formatDocstring(generatedDocstring, indentation)

        WriteCommandAction.runWriteCommandAction(project, Runnable {
            if (functionInfo.hasExistingDocstring) {
                replaceExistingDocstring(document, function, formattedDocstring)
            } else {
                insertNewDocstring(document, functionInfo, formattedDocstring)
            }
        })
    }

    private fun formatDocstring(raw: String, indentation: String): String {
        val lines = raw.lines()
        if (lines.first().trim().startsWith("\"\"\"")) {
            return lines.joinToString("\n") { line ->
                if (line.isBlank()) "" else "$indentation$line".trimEnd()
            }
        }
        val inner = lines.joinToString("\n") { line ->
            if (line.isBlank()) "" else "$indentation$line".trimEnd()
        }
        return "$indentation\"\"\"\n$inner\n$indentation\"\"\""
    }

    private fun insertNewDocstring(
        document: com.intellij.openapi.editor.Document,
        functionInfo: PythonFunctionExtractor.FunctionInfo,
        formattedDocstring: String
    ) {
        val bodyStart = functionInfo.bodyStartOffset
        val lineNumber = document.getLineNumber(bodyStart)
        val insertOffset = document.getLineStartOffset(lineNumber)
        document.insertString(insertOffset, "$formattedDocstring\n")
    }

    private fun replaceExistingDocstring(
        document: com.intellij.openapi.editor.Document,
        function: PyFunction,
        formattedDocstring: String
    ) {
        val docStringExpression = function.docStringExpression ?: return
        val range = docStringExpression.textRange
        document.replaceString(range.startOffset, range.endOffset, formattedDocstring.trim())
    }
}