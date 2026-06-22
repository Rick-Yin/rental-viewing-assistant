package com.rentalviewingassistant.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rental_profiles")
data class RentalProfileEntity(
    @PrimaryKey val id: String,
    val tenantType: String,
    val gender: String,
    val specialPurposesJson: String,
    val coLivingType: String,
    val hasPet: String,
    val hasChildren: String,
    val rentalRegion: String,
    val notes: String,
    val createdAt: String,
    val updatedAt: String,
)

@Entity(tableName = "template_selections")
data class ChecklistTemplateSelectionEntity(
    @PrimaryKey val id: String,
    val rentalProfileId: String?,
    val viewingTemplateCodesJson: String,
    val signingTemplateCodesJson: String,
    val recommendationReasonsJson: String,
    val manuallyEdited: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

@Entity(tableName = "properties")
data class PropertyEntity(
    @PrimaryKey val id: String,
    val title: String,
    val communityName: String,
    val address: String,
    val district: String,
    val rentAmount: Int?,
    val depositRule: String,
    val agencyFee: String,
    val layoutText: String,
    val areaSquareMeter: Double?,
    val floorInfo: String,
    val orientation: String,
    val contactName: String,
    val contactPhone: String,
    val sourcePlatform: String,
    val listingUrl: String,
    val status: String,
    val workflowStage: String,
    val selectedForSigningAt: String?,
    val rejectionReason: String,
    val createdAt: String,
    val updatedAt: String,
)

@Entity(tableName = "viewings")
data class ViewingEntity(
    @PrimaryKey val id: String,
    val propertyId: String,
    val scheduledAt: String,
    val visitedAt: String,
    val transportSummary: String,
    val firstImpression: String,
    val noiseLevel: String,
    val odorLevel: String,
    val cleanlinessLevel: String,
    val lightingLevel: String,
    val ventilationLevel: String,
    val agentAttitude: String,
    val riskSummary: String,
    val notes: String,
    val createdAt: String,
    val updatedAt: String,
)

@Entity(tableName = "checklist_results")
data class ChecklistResultEntity(
    @PrimaryKey val id: String,
    val stage: String,
    val propertyId: String,
    val ownerId: String,
    val categoryCode: String,
    val itemCode: String,
    val resultValue: String,
    val severity: String,
    val comment: String,
    val createdAt: String,
    val updatedAt: String,
)

@Entity(tableName = "media_assets")
data class MediaAssetEntity(
    @PrimaryKey val id: String,
    val propertyId: String,
    val viewingId: String?,
    val signingSessionId: String?,
    val fileName: String,
    val filePath: String,
    val mimeType: String,
    val capturedAt: String,
    val note: String,
    val sortOrder: Int,
    val createdAt: String,
)

@Entity(tableName = "score_cards")
data class ScoreCardEntity(
    @PrimaryKey val id: String,
    val propertyId: String,
    val viewingId: String?,
    val transportScore: Double?,
    val environmentScore: Double?,
    val layoutScore: Double?,
    val facilityScore: Double?,
    val riskScore: Double?,
    val pricePerformanceScore: Double?,
    val totalScore: Double?,
    val scoringVersion: String,
    val createdAt: String,
    val updatedAt: String,
)

@Entity(tableName = "comparison_entries")
data class ComparisonEntryEntity(
    @PrimaryKey val id: String,
    val propertyId: String,
    val selected: Boolean,
    val pinned: Boolean,
    val userConclusion: String,
    val decisionTag: String,
    val createdAt: String,
    val updatedAt: String,
)

@Entity(tableName = "signing_sessions")
data class SigningSessionEntity(
    @PrimaryKey val id: String,
    val propertyId: String,
    val status: String,
    val startedAt: String,
    val decidedAt: String?,
    val counterpartyRole: String,
    val counterpartyName: String,
    val counterpartyContact: String,
    val decisionNotes: String,
    val createdAt: String,
    val updatedAt: String,
)

@Entity(tableName = "signing_material_assets")
data class SigningMaterialAssetEntity(
    @PrimaryKey val id: String,
    val propertyId: String,
    val signingSessionId: String,
    val materialType: String,
    val title: String,
    val textContent: String,
    val fileName: String,
    val filePath: String,
    val mimeType: String,
    val capturedAt: String,
    val note: String,
    val sortOrder: Int,
    val createdAt: String,
)

@Entity(tableName = "ai_analysis_results")
data class AiAnalysisResultEntity(
    @PrimaryKey val id: String,
    val propertyId: String,
    val signingSessionId: String?,
    val stage: String,
    val schemaVersion: String,
    val generatedAt: String,
    val rawJson: String,
    val summary: String,
    val recommendation: String,
    val createdAt: String,
    val updatedAt: String,
)
