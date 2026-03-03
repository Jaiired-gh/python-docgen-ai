package com.github.pythondocgen.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "PythonDocGenSettings",
    storages = [Storage("PythonDocGenSettings.xml")]
)
class DocGenSettings : PersistentStateComponent<DocGenSettings.State> {

    data class State(
        var apiKey: String = "",
        var docstringFormat: String = "google",
        var language: String = "italian",
        var includeExamples: Boolean = false,
        var includeRaises: Boolean = true
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    var apiKey: String
        get() = myState.apiKey
        set(value) { myState.apiKey = value }

    var docstringFormat: String
        get() = myState.docstringFormat
        set(value) { myState.docstringFormat = value }

    var language: String
        get() = myState.language
        set(value) { myState.language = value }

    var includeExamples: Boolean
        get() = myState.includeExamples
        set(value) { myState.includeExamples = value }

    var includeRaises: Boolean
        get() = myState.includeRaises
        set(value) { myState.includeRaises = value }

    companion object {
        val instance: DocGenSettings
            get() = ApplicationManager.getApplication().getService(DocGenSettings::class.java)
    }
}