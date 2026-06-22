package com.rentalviewingassistant.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class WorkflowStage {
    @SerialName("viewing")
    VIEWING,
    @SerialName("signing")
    SIGNING,
    @SerialName("signed")
    SIGNED,
    @SerialName("signing_abandoned")
    SIGNING_ABANDONED,
}

@Serializable
enum class PropertyStatus {
    @SerialName("draft")
    DRAFT,
    @SerialName("to_view")
    TO_VIEW,
    @SerialName("viewed")
    VIEWED,
    @SerialName("shortlisted")
    SHORTLISTED,
    @SerialName("signing")
    SIGNING,
    @SerialName("signed")
    SIGNED,
    @SerialName("signing_abandoned")
    SIGNING_ABANDONED,
    @SerialName("rejected")
    REJECTED,
    @SerialName("archived")
    ARCHIVED,
}

@Serializable
enum class ResultValue {
    @SerialName("unchecked")
    UNCHECKED,
    @SerialName("ok")
    OK,
    @SerialName("risk")
    RISK,
    @SerialName("uncertain")
    UNCERTAIN,
}

@Serializable
enum class Severity {
    @SerialName("none")
    NONE,
    @SerialName("low")
    LOW,
    @SerialName("medium")
    MEDIUM,
    @SerialName("high")
    HIGH,
}

@Serializable
enum class ChecklistStage {
    @SerialName("viewing")
    VIEWING,
    @SerialName("signing")
    SIGNING,
}

@Serializable
enum class MaterialType {
    @SerialName("text_note")
    TEXT_NOTE,
    @SerialName("image")
    IMAGE,
}

@Serializable
enum class ExportProfile {
    @SerialName("local_backup")
    LOCAL_BACKUP,
    @SerialName("ai_quick_share")
    AI_QUICK_SHARE,
    @SerialName("manual_export")
    MANUAL_EXPORT,
}

@Serializable
data class RentalProfile(
    val id: String,
    val tenantType: String = "",
    val gender: String = "",
    val specialPurposes: List<String> = emptyList(),
    val coLivingType: String = "",
    val hasPet: String = "",
    val hasChildren: String = "",
    val rentalRegion: String = "",
    val notes: String = "",
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class ChecklistTemplateSelection(
    val id: String,
    val rentalProfileId: String?,
    val viewingTemplateCodes: List<String>,
    val signingTemplateCodes: List<String>,
    val recommendationReasons: List<String>,
    val manuallyEdited: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class Property(
    val id: String,
    val title: String,
    val communityName: String = "",
    val address: String = "",
    val district: String = "",
    val rentAmount: Int? = null,
    val depositRule: String = "",
    val agencyFee: String = "",
    val layoutText: String = "",
    val areaSquareMeter: Double? = null,
    val floorInfo: String = "",
    val orientation: String = "",
    val contactName: String = "",
    val contactPhone: String = "",
    val sourcePlatform: String = "",
    val listingUrl: String = "",
    val status: PropertyStatus = PropertyStatus.DRAFT,
    val workflowStage: WorkflowStage = WorkflowStage.VIEWING,
    val selectedForSigningAt: String? = null,
    val rejectionReason: String = "",
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class Viewing(
    val id: String,
    val propertyId: String,
    val scheduledAt: String = "",
    val visitedAt: String = "",
    val transportSummary: String = "",
    val firstImpression: String = "",
    val noiseLevel: String = "",
    val odorLevel: String = "",
    val cleanlinessLevel: String = "",
    val lightingLevel: String = "",
    val ventilationLevel: String = "",
    val agentAttitude: String = "",
    val riskSummary: String = "",
    val notes: String = "",
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class ChecklistResult(
    val id: String,
    val stage: ChecklistStage,
    val propertyId: String,
    val ownerId: String,
    val categoryCode: String,
    val itemCode: String,
    val resultValue: ResultValue,
    val severity: Severity,
    val comment: String = "",
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class MediaAsset(
    val id: String,
    val propertyId: String,
    val viewingId: String? = null,
    val signingSessionId: String? = null,
    val fileName: String,
    val filePath: String,
    val mimeType: String,
    val capturedAt: String,
    val note: String = "",
    val sortOrder: Int = 0,
    val createdAt: String,
)

@Serializable
data class ScoreCard(
    val id: String,
    val propertyId: String,
    val viewingId: String?,
    val transportScore: Double? = null,
    val environmentScore: Double? = null,
    val layoutScore: Double? = null,
    val facilityScore: Double? = null,
    val riskScore: Double? = null,
    val pricePerformanceScore: Double? = null,
    val totalScore: Double? = null,
    val scoringVersion: String = "1.0.0",
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class ComparisonEntry(
    val id: String,
    val propertyId: String,
    val selected: Boolean = true,
    val pinned: Boolean = false,
    val userConclusion: String = "",
    val decisionTag: String = "",
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class SigningSession(
    val id: String,
    val propertyId: String,
    val status: PropertyStatus = PropertyStatus.SIGNING,
    val startedAt: String,
    val decidedAt: String? = null,
    val counterpartyRole: String = "",
    val counterpartyName: String = "",
    val counterpartyContact: String = "",
    val decisionNotes: String = "",
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class SigningMaterialAsset(
    val id: String,
    val propertyId: String,
    val signingSessionId: String,
    val materialType: MaterialType,
    val title: String,
    val textContent: String = "",
    val fileName: String = "",
    val filePath: String = "",
    val mimeType: String = "",
    val capturedAt: String = "",
    val note: String = "",
    val sortOrder: Int = 0,
    val createdAt: String,
)

@Serializable
data class AiAnalysisResult(
    val id: String,
    val propertyId: String,
    val signingSessionId: String? = null,
    val stage: ChecklistStage,
    val schemaVersion: String,
    val generatedAt: String,
    val rawJson: String,
    val summary: String,
    val recommendation: String,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class RentalState(
    val profile: RentalProfile? = null,
    val templateSelection: ChecklistTemplateSelection? = null,
    val properties: List<Property> = emptyList(),
    val viewings: List<Viewing> = emptyList(),
    val checklistResults: List<ChecklistResult> = emptyList(),
    val mediaAssets: List<MediaAsset> = emptyList(),
    val scoreCards: List<ScoreCard> = emptyList(),
    val comparisonEntries: List<ComparisonEntry> = emptyList(),
    val signingSessions: List<SigningSession> = emptyList(),
    val signingMaterialAssets: List<SigningMaterialAsset> = emptyList(),
    val aiAnalysisResults: List<AiAnalysisResult> = emptyList(),
)
