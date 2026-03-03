package com.github.pythondocgen.actions

import com.github.pythondocgen.services.ClaudeApiService
import com.github.pythondocgen.services.DocGenSettings
import com.github.pythondocgen.services.DocstringInserter
import com.github.pythondocgen.services.PythonFunctionExtractor
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import com.jetbrains.python.psi.PyFile

class GenerateAllDocstringsAction : AnAction() {

    private val apiService = ClaudeApiService()

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return

        if (psiFile !is PyFile) return

        val settings = DocGenSettings.instance
        if (settings.apiKey.isBlank()) {
            Messages.showWarningDialog(
                project,
                "Please configure the API key in Settings → Tools → Python DocGen AI",
                "Python DocGen AI"
            )
            return
        }

        val functions = PythonFunctionExtractor.findAllFunctions(psiFile)
        if (functions.isEmpty()) {
            showNotification(e, "No functions found in the current file", NotificationType.INFORMATION)
            return
        }

        val confirm = Messages.showOkCancelDialog(
            project,
            "Docstrings will be generated for ${functions.size} functions.\nContinue?",
            "Python DocGen AI",
            "Generate",
            "Cancel",
            Messages.getQuestionIcon()
        )
        if (confirm != Messages.OK) return

        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            "Generating docstrings for the entire file...",
            true
        ) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = false
                var successCount = 0
                var errorCount = 0

                val functionsReversed = functions.sortedByDescending { it.startOffset }

                functionsReversed.forEachIndexed { index, funcInfo ->
                    if (indicator.isCanceled) return@forEachIndexed

                    val funcName = funcInfo.function.name ?: "function"
                    indicator.text = "Processing '$funcName'..."
                    indicator.fraction = index.toDouble() / functions.size

                    val result = apiService.generateDocstring(funcInfo.code, settings)

                    com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                        if (result.success) {
                            DocstringInserter.insertDocstring(project, editor, funcInfo, result.docstring)
                            successCount++
                        } else {
                            errorCount++
                        }
                    }

                    Thread.sleep(500)
                }

                com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                    val msg = "✅ $successCount docstrings generated" +
                            if (errorCount > 0) ", ❌ $errorCount errors" else ""
                    showNotification(e, msg, if (errorCount > 0) NotificationType.WARNING else NotificationType.INFORMATION)
                }
            }
        })
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        e.presentation.isEnabledAndVisible = editor != null && psiFile != null
    }

    private fun showNotification(e: AnActionEvent, message: String, type: NotificationType) {
        val project = e.project ?: return
        NotificationGroupManager.getInstance()
            .getNotificationGroup("PythonDocGen")
            .createNotification(message, type)
            .notify(project)
    }
}