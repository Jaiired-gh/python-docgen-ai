package com.github.pythondocgen.services

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.psi.PyFunction

object PythonFunctionExtractor {

    data class FunctionInfo(
        val function: PyFunction,
        val code: String,
        val startOffset: Int,
        val bodyStartOffset: Int,
        val hasExistingDocstring: Boolean
    )

    fun findFunctionAtCursor(editor: Editor, psiFile: PsiFile): FunctionInfo? {
        val offset = editor.caretModel.offset
        val element = psiFile.findElementAt(offset) ?: return null
        val function = PsiTreeUtil.getParentOfType(element, PyFunction::class.java) ?: return null
        return extractInfo(function)
    }

    fun findAllFunctions(psiFile: PsiFile): List<FunctionInfo> {
        return PsiTreeUtil.findChildrenOfType(psiFile, PyFunction::class.java)
            .mapNotNull { extractInfo(it) }
    }

    private fun extractInfo(function: PyFunction): FunctionInfo? {
        val code = function.text ?: return null
        val startOffset = function.textRange.startOffset
        val statementList = function.statementList
        val bodyStartOffset = statementList.textRange.startOffset
        val hasDocstring = function.docStringValue != null

        return FunctionInfo(
            function = function,
            code = code,
            startOffset = startOffset,
            bodyStartOffset = bodyStartOffset,
            hasExistingDocstring = hasDocstring
        )
    }

    fun getBodyIndentation(function: PyFunction): String {
        val funcText = function.text ?: return "    "
        val bodyLine = funcText.lines()
            .drop(1)
            .firstOrNull { it.isNotBlank() } ?: return "    "
        val spaces = bodyLine.length - bodyLine.trimStart().length
        return " ".repeat(spaces)
    }
}