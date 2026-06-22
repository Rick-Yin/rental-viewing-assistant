package com.rentalviewingassistant.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChecklistDefinition(
    val schemaVersion: String,
    val stage: ChecklistStage,
    val contentVersion: String,
    val updatedAt: String,
    val categories: List<ChecklistCategory>,
    val templateModules: List<TemplateModule>,
)

@Serializable
data class ChecklistCategory(
    val code: String,
    val label: String,
    val description: String = "",
    val items: List<ChecklistItem>,
)

@Serializable
data class ChecklistItem(
    val code: String,
    val label: String,
    val oneLine: String,
    val whyCheck: String,
    val howToCheck: List<String>,
    val riskSignals: List<String>,
    val captureAdvice: List<String>,
    val recordAdvice: String,
    val referenceLinks: List<ReferenceLink>,
    val priority: ChecklistPriority,
    val analysisMapping: AnalysisMapping? = null,
)

@Serializable
data class ReferenceLink(
    val title: String,
    val url: String,
    val note: String = "",
)

@Serializable
enum class ChecklistPriority {
    @SerialName("low")
    LOW,
    @SerialName("medium")
    MEDIUM,
    @SerialName("high")
    HIGH,
}

@Serializable
data class AnalysisMapping(
    val dimensions: List<String> = emptyList(),
)

@Serializable
data class TemplateModule(
    val code: String,
    val label: String,
    val reason: String,
    val categoryCodes: List<String>,
    val alwaysEnabled: Boolean = false,
)

fun ChecklistDefinition.enabledCategories(moduleCodes: List<String>): List<ChecklistCategory> {
    val enabledCodes = templateModules
        .filter { it.alwaysEnabled || it.code in moduleCodes }
        .flatMap { it.categoryCodes }
        .toSet()
    return categories.filter { it.code in enabledCodes }
}
