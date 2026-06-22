package com.rentalviewingassistant.data.repository

import com.rentalviewingassistant.data.local.AiAnalysisResultEntity
import com.rentalviewingassistant.data.local.ChecklistResultEntity
import com.rentalviewingassistant.data.local.ChecklistTemplateSelectionEntity
import com.rentalviewingassistant.data.local.ComparisonEntryEntity
import com.rentalviewingassistant.data.local.MediaAssetEntity
import com.rentalviewingassistant.data.local.PropertyEntity
import com.rentalviewingassistant.data.local.RentalProfileEntity
import com.rentalviewingassistant.data.local.ScoreCardEntity
import com.rentalviewingassistant.data.local.SigningMaterialAssetEntity
import com.rentalviewingassistant.data.local.SigningSessionEntity
import com.rentalviewingassistant.data.local.ViewingEntity
import com.rentalviewingassistant.domain.model.AiAnalysisResult
import com.rentalviewingassistant.domain.model.ChecklistResult
import com.rentalviewingassistant.domain.model.ChecklistStage
import com.rentalviewingassistant.domain.model.ChecklistTemplateSelection
import com.rentalviewingassistant.domain.model.ComparisonEntry
import com.rentalviewingassistant.domain.model.MediaAsset
import com.rentalviewingassistant.domain.model.MaterialType
import com.rentalviewingassistant.domain.model.Property
import com.rentalviewingassistant.domain.model.PropertyStatus
import com.rentalviewingassistant.domain.model.RentalProfile
import com.rentalviewingassistant.domain.model.ResultValue
import com.rentalviewingassistant.domain.model.ScoreCard
import com.rentalviewingassistant.domain.model.Severity
import com.rentalviewingassistant.domain.model.SigningMaterialAsset
import com.rentalviewingassistant.domain.model.SigningSession
import com.rentalviewingassistant.domain.model.Viewing
import com.rentalviewingassistant.domain.model.WorkflowStage
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val stringListSerializer = ListSerializer(String.serializer())

internal fun Json.encodeList(values: List<String>): String = encodeToString(stringListSerializer, values)

internal fun Json.decodeList(value: String): List<String> = if (value.isBlank()) {
    emptyList()
} else {
    decodeFromString(stringListSerializer, value)
}

internal fun RentalProfile.toEntity(json: Json) = RentalProfileEntity(
    id = id,
    tenantType = tenantType,
    gender = gender,
    specialPurposesJson = json.encodeList(specialPurposes),
    coLivingType = coLivingType,
    hasPet = hasPet,
    hasChildren = hasChildren,
    rentalRegion = rentalRegion,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun RentalProfileEntity.toDomain(json: Json) = RentalProfile(
    id = id,
    tenantType = tenantType,
    gender = gender,
    specialPurposes = json.decodeList(specialPurposesJson),
    coLivingType = coLivingType,
    hasPet = hasPet,
    hasChildren = hasChildren,
    rentalRegion = rentalRegion,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun ChecklistTemplateSelection.toEntity(json: Json) = ChecklistTemplateSelectionEntity(
    id = id,
    rentalProfileId = rentalProfileId,
    viewingTemplateCodesJson = json.encodeList(viewingTemplateCodes),
    signingTemplateCodesJson = json.encodeList(signingTemplateCodes),
    recommendationReasonsJson = json.encodeList(recommendationReasons),
    manuallyEdited = manuallyEdited,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun ChecklistTemplateSelectionEntity.toDomain(json: Json) = ChecklistTemplateSelection(
    id = id,
    rentalProfileId = rentalProfileId,
    viewingTemplateCodes = json.decodeList(viewingTemplateCodesJson),
    signingTemplateCodes = json.decodeList(signingTemplateCodesJson),
    recommendationReasons = json.decodeList(recommendationReasonsJson),
    manuallyEdited = manuallyEdited,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun Property.toEntity() = PropertyEntity(
    id = id,
    title = title,
    communityName = communityName,
    address = address,
    district = district,
    rentAmount = rentAmount,
    depositRule = depositRule,
    agencyFee = agencyFee,
    layoutText = layoutText,
    areaSquareMeter = areaSquareMeter,
    floorInfo = floorInfo,
    orientation = orientation,
    contactName = contactName,
    contactPhone = contactPhone,
    sourcePlatform = sourcePlatform,
    listingUrl = listingUrl,
    status = status.name,
    workflowStage = workflowStage.name,
    selectedForSigningAt = selectedForSigningAt,
    rejectionReason = rejectionReason,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun PropertyEntity.toDomain() = Property(
    id = id,
    title = title,
    communityName = communityName,
    address = address,
    district = district,
    rentAmount = rentAmount,
    depositRule = depositRule,
    agencyFee = agencyFee,
    layoutText = layoutText,
    areaSquareMeter = areaSquareMeter,
    floorInfo = floorInfo,
    orientation = orientation,
    contactName = contactName,
    contactPhone = contactPhone,
    sourcePlatform = sourcePlatform,
    listingUrl = listingUrl,
    status = enumValueOf(status),
    workflowStage = enumValueOf(workflowStage),
    selectedForSigningAt = selectedForSigningAt,
    rejectionReason = rejectionReason,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun Viewing.toEntity() = ViewingEntity(
    id = id,
    propertyId = propertyId,
    scheduledAt = scheduledAt,
    visitedAt = visitedAt,
    transportSummary = transportSummary,
    firstImpression = firstImpression,
    noiseLevel = noiseLevel,
    odorLevel = odorLevel,
    cleanlinessLevel = cleanlinessLevel,
    lightingLevel = lightingLevel,
    ventilationLevel = ventilationLevel,
    agentAttitude = agentAttitude,
    riskSummary = riskSummary,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun ViewingEntity.toDomain() = Viewing(
    id = id,
    propertyId = propertyId,
    scheduledAt = scheduledAt,
    visitedAt = visitedAt,
    transportSummary = transportSummary,
    firstImpression = firstImpression,
    noiseLevel = noiseLevel,
    odorLevel = odorLevel,
    cleanlinessLevel = cleanlinessLevel,
    lightingLevel = lightingLevel,
    ventilationLevel = ventilationLevel,
    agentAttitude = agentAttitude,
    riskSummary = riskSummary,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun ChecklistResult.toEntity() = ChecklistResultEntity(
    id = id,
    stage = stage.name,
    propertyId = propertyId,
    ownerId = ownerId,
    categoryCode = categoryCode,
    itemCode = itemCode,
    resultValue = resultValue.name,
    severity = severity.name,
    comment = comment,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun ChecklistResultEntity.toDomain() = ChecklistResult(
    id = id,
    stage = enumValueOf(stage),
    propertyId = propertyId,
    ownerId = ownerId,
    categoryCode = categoryCode,
    itemCode = itemCode,
    resultValue = enumValueOf(resultValue),
    severity = enumValueOf(severity),
    comment = comment,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun MediaAsset.toEntity() = MediaAssetEntity(
    id = id,
    propertyId = propertyId,
    viewingId = viewingId,
    signingSessionId = signingSessionId,
    fileName = fileName,
    filePath = filePath,
    mimeType = mimeType,
    capturedAt = capturedAt,
    note = note,
    sortOrder = sortOrder,
    createdAt = createdAt,
)

internal fun MediaAssetEntity.toDomain() = MediaAsset(
    id = id,
    propertyId = propertyId,
    viewingId = viewingId,
    signingSessionId = signingSessionId,
    fileName = fileName,
    filePath = filePath,
    mimeType = mimeType,
    capturedAt = capturedAt,
    note = note,
    sortOrder = sortOrder,
    createdAt = createdAt,
)

internal fun ScoreCard.toEntity() = ScoreCardEntity(
    id = id,
    propertyId = propertyId,
    viewingId = viewingId,
    transportScore = transportScore,
    environmentScore = environmentScore,
    layoutScore = layoutScore,
    facilityScore = facilityScore,
    riskScore = riskScore,
    pricePerformanceScore = pricePerformanceScore,
    totalScore = totalScore,
    scoringVersion = scoringVersion,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun ScoreCardEntity.toDomain() = ScoreCard(
    id = id,
    propertyId = propertyId,
    viewingId = viewingId,
    transportScore = transportScore,
    environmentScore = environmentScore,
    layoutScore = layoutScore,
    facilityScore = facilityScore,
    riskScore = riskScore,
    pricePerformanceScore = pricePerformanceScore,
    totalScore = totalScore,
    scoringVersion = scoringVersion,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun ComparisonEntry.toEntity() = ComparisonEntryEntity(
    id = id,
    propertyId = propertyId,
    selected = selected,
    pinned = pinned,
    userConclusion = userConclusion,
    decisionTag = decisionTag,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun ComparisonEntryEntity.toDomain() = ComparisonEntry(
    id = id,
    propertyId = propertyId,
    selected = selected,
    pinned = pinned,
    userConclusion = userConclusion,
    decisionTag = decisionTag,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun SigningSession.toEntity() = SigningSessionEntity(
    id = id,
    propertyId = propertyId,
    status = status.name,
    startedAt = startedAt,
    decidedAt = decidedAt,
    counterpartyRole = counterpartyRole,
    counterpartyName = counterpartyName,
    counterpartyContact = counterpartyContact,
    decisionNotes = decisionNotes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun SigningSessionEntity.toDomain() = SigningSession(
    id = id,
    propertyId = propertyId,
    status = enumValueOf<PropertyStatus>(status),
    startedAt = startedAt,
    decidedAt = decidedAt,
    counterpartyRole = counterpartyRole,
    counterpartyName = counterpartyName,
    counterpartyContact = counterpartyContact,
    decisionNotes = decisionNotes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun SigningMaterialAsset.toEntity() = SigningMaterialAssetEntity(
    id = id,
    propertyId = propertyId,
    signingSessionId = signingSessionId,
    materialType = materialType.name,
    title = title,
    textContent = textContent,
    fileName = fileName,
    filePath = filePath,
    mimeType = mimeType,
    capturedAt = capturedAt,
    note = note,
    sortOrder = sortOrder,
    createdAt = createdAt,
)

internal fun SigningMaterialAssetEntity.toDomain() = SigningMaterialAsset(
    id = id,
    propertyId = propertyId,
    signingSessionId = signingSessionId,
    materialType = enumValueOf<MaterialType>(materialType),
    title = title,
    textContent = textContent,
    fileName = fileName,
    filePath = filePath,
    mimeType = mimeType,
    capturedAt = capturedAt,
    note = note,
    sortOrder = sortOrder,
    createdAt = createdAt,
)

internal fun AiAnalysisResult.toEntity() = AiAnalysisResultEntity(
    id = id,
    propertyId = propertyId,
    signingSessionId = signingSessionId,
    stage = stage.name,
    schemaVersion = schemaVersion,
    generatedAt = generatedAt,
    rawJson = rawJson,
    summary = summary,
    recommendation = recommendation,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun AiAnalysisResultEntity.toDomain() = AiAnalysisResult(
    id = id,
    propertyId = propertyId,
    signingSessionId = signingSessionId,
    stage = enumValueOf<ChecklistStage>(stage),
    schemaVersion = schemaVersion,
    generatedAt = generatedAt,
    rawJson = rawJson,
    summary = summary,
    recommendation = recommendation,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
