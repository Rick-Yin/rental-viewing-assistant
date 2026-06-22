package com.rentalviewingassistant.data.repository

import com.rentalviewingassistant.data.local.RentalDao
import com.rentalviewingassistant.domain.model.AiAnalysisResult
import com.rentalviewingassistant.domain.model.ChecklistResult
import com.rentalviewingassistant.domain.model.ChecklistTemplateSelection
import com.rentalviewingassistant.domain.model.ComparisonEntry
import com.rentalviewingassistant.domain.model.MediaAsset
import com.rentalviewingassistant.domain.model.Property
import com.rentalviewingassistant.domain.model.RentalProfile
import com.rentalviewingassistant.domain.model.RentalState
import com.rentalviewingassistant.domain.model.ScoreCard
import com.rentalviewingassistant.domain.model.SigningMaterialAsset
import com.rentalviewingassistant.domain.model.SigningSession
import com.rentalviewingassistant.domain.model.Viewing
import com.rentalviewingassistant.domain.repository.RentalRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.json.Json

@Singleton
class RoomRentalRepository @Inject constructor(
    private val dao: RentalDao,
    private val json: Json,
) : RentalRepository {
    @Suppress("UNCHECKED_CAST")
    override val state: Flow<RentalState> = combine(
        dao.profiles(),
        dao.templateSelections(),
        dao.properties(),
        dao.viewings(),
        dao.checklistResults(),
        dao.mediaAssets(),
        dao.scoreCards(),
        dao.comparisonEntries(),
        dao.signingSessions(),
        dao.signingMaterialAssets(),
        dao.aiAnalysisResults(),
    ) { values ->
        RentalState(
            profile = (values[0] as List<com.rentalviewingassistant.data.local.RentalProfileEntity>).firstOrNull()?.toDomain(json),
            templateSelection = (values[1] as List<com.rentalviewingassistant.data.local.ChecklistTemplateSelectionEntity>).firstOrNull()?.toDomain(json),
            properties = (values[2] as List<com.rentalviewingassistant.data.local.PropertyEntity>).map { it.toDomain() },
            viewings = (values[3] as List<com.rentalviewingassistant.data.local.ViewingEntity>).map { it.toDomain() },
            checklistResults = (values[4] as List<com.rentalviewingassistant.data.local.ChecklistResultEntity>).map { it.toDomain() },
            mediaAssets = (values[5] as List<com.rentalviewingassistant.data.local.MediaAssetEntity>).map { it.toDomain() },
            scoreCards = (values[6] as List<com.rentalviewingassistant.data.local.ScoreCardEntity>).map { it.toDomain() },
            comparisonEntries = (values[7] as List<com.rentalviewingassistant.data.local.ComparisonEntryEntity>).map { it.toDomain() },
            signingSessions = (values[8] as List<com.rentalviewingassistant.data.local.SigningSessionEntity>).map { it.toDomain() },
            signingMaterialAssets = (values[9] as List<com.rentalviewingassistant.data.local.SigningMaterialAssetEntity>).map { it.toDomain() },
            aiAnalysisResults = (values[10] as List<com.rentalviewingassistant.data.local.AiAnalysisResultEntity>).map { it.toDomain() },
        )
    }

    override suspend fun upsertProfile(profile: RentalProfile) = dao.upsertProfile(profile.toEntity(json))

    override suspend fun upsertTemplateSelection(selection: ChecklistTemplateSelection) =
        dao.upsertTemplateSelection(selection.toEntity(json))

    override suspend fun upsertProperty(property: Property) = dao.upsertProperty(property.toEntity())

    override suspend fun upsertViewing(viewing: Viewing) = dao.upsertViewing(viewing.toEntity())

    override suspend fun upsertChecklistResult(result: ChecklistResult) = dao.upsertChecklistResult(result.toEntity())

    override suspend fun upsertMediaAsset(asset: MediaAsset) = dao.upsertMediaAsset(asset.toEntity())

    override suspend fun upsertScoreCard(scoreCard: ScoreCard) = dao.upsertScoreCard(scoreCard.toEntity())

    override suspend fun upsertComparisonEntry(entry: ComparisonEntry) = dao.upsertComparisonEntry(entry.toEntity())

    override suspend fun upsertSigningSession(session: SigningSession) = dao.upsertSigningSession(session.toEntity())

    override suspend fun upsertSigningMaterial(asset: SigningMaterialAsset) =
        dao.upsertSigningMaterialAsset(asset.toEntity())

    override suspend fun upsertAiAnalysisResult(result: AiAnalysisResult) = dao.upsertAiAnalysisResult(result.toEntity())

    override suspend fun deleteAllDemoData() {
        dao.clearAiAnalysisResults()
        dao.clearSigningMaterialAssets()
        dao.clearSigningSessions()
        dao.clearComparisonEntries()
        dao.clearScoreCards()
        dao.clearMediaAssets()
        dao.clearChecklistResults()
        dao.clearViewings()
        dao.clearProperties()
        dao.clearTemplateSelections()
        dao.clearProfiles()
    }
}
