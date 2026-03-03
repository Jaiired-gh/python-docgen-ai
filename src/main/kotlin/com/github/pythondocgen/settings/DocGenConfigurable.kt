package com.github.pythondocgen.settings

import com.github.pythondocgen.services.DocGenSettings
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class DocGenConfigurable : Configurable {

    private val apiKeyField = JBPasswordField()
    private val formatCombo = ComboBox(arrayOf("Google Style", "NumPy", "reStructuredText"))
    private val languageCombo = ComboBox(arrayOf("Italian", "English"))
    private val includeExamplesCheck = JBCheckBox("Include Examples section")
    private val includeRaisesCheck = JBCheckBox("Include Raises/Exceptions section")

    private var mainPanel: JPanel? = null

    override fun getDisplayName(): String = "Python DocGen AI"

    override fun createComponent(): JComponent {
        val panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Groq API Key:"), apiKeyField, 1, false)
            .addSeparator()
            .addLabeledComponent(JBLabel("Docstring Format:"), formatCombo, 1, false)
            .addLabeledComponent(JBLabel("Comment Language:"), languageCombo, 1, false)
            .addSeparator()
            .addComponent(includeExamplesCheck)
            .addComponent(includeRaisesCheck)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        mainPanel = panel
        return panel
    }

    override fun isModified(): Boolean {
        val settings = DocGenSettings.instance
        return String(apiKeyField.password) != settings.apiKey ||
                getFormatValue() != settings.docstringFormat ||
                getLanguageValue() != settings.language ||
                includeExamplesCheck.isSelected != settings.includeExamples ||
                includeRaisesCheck.isSelected != settings.includeRaises
    }

    override fun apply() {
        val settings = DocGenSettings.instance
        settings.apiKey = String(apiKeyField.password)
        settings.docstringFormat = getFormatValue()
        settings.language = getLanguageValue()
        settings.includeExamples = includeExamplesCheck.isSelected
        settings.includeRaises = includeRaisesCheck.isSelected
    }

    override fun reset() {
        val settings = DocGenSettings.instance
        apiKeyField.text = settings.apiKey
        formatCombo.selectedIndex = when (settings.docstringFormat) {
            "numpy" -> 1
            "rst" -> 2
            else -> 0
        }
        languageCombo.selectedIndex = if (settings.language == "english") 1 else 0
        includeExamplesCheck.isSelected = settings.includeExamples
        includeRaisesCheck.isSelected = settings.includeRaises
    }

    private fun getFormatValue(): String = when (formatCombo.selectedIndex) {
        1 -> "numpy"
        2 -> "rst"
        else -> "google"
    }

    private fun getLanguageValue(): String = when (languageCombo.selectedIndex) {
        1 -> "english"
        else -> "italian"
    }
}