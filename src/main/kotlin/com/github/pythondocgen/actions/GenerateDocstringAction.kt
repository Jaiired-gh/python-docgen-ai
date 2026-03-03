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

class GenerateDocstringAction : AnAction() {

    private val apiService = ClaudeApiService()

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return

        if (psiFile !is PyFile) {
            showNotification(e, "This plugin only works with Python files (.py)", NotificationType.WARNING)
            return
        }

        val settings = DocGenSettings.instance
        if (settings.apiKey.isBlank()) {
            val result = Messages.showOkCancelDialog(
                project,
                "Groq API key not configured.\nWould you like to open the settings?",
                "Python DocGen AI",
                "Open Settings",
                "Cancel",
                Messages.getWarningIcon()
            )
            if (result == Messages.OK) {
                com.intellij.openapi.options.ShowSettingsUtil.getInstance()
                    .showSettingsDialog(project, "Python DocGen AI")
            }
            return
        }

        val functionInfo = PythonFunctionExtractor.findFunctionAtCursor(editor, psiFile)
        if (functionInfo == null) {
            showNotification(e, "Place the cursor inside a Python function", NotificationType.INFORMATION)
            return
        }

        val funcName = functionInfo.function.name ?: "function"

        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            "Generating docstring for '$funcName'...",
            false
        ) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                indicator.text = "Calling Groq AI..."

                val result = apiService.generateDocstring(functionInfo.code, settings)

                com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                    if (result.success) {
                        DocstringInserter.insertDocstring(project, editor, functionInfo, result.docstring)
                        showNotification(e, "✅ Docstring generated for '$funcName'", NotificationType.INFORMATION)
                    } else {
                        showNotification(e, "❌ ${result.error}", NotificationType.ERROR)
                    }
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