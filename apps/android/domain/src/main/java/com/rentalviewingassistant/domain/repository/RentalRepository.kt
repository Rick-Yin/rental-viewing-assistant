package com.rentalviewingassistant.domain.repository

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
import kotlinx.coroutines.flow.Flow

interface RentalRepository {
    val state: Flow<RentalState>

    suspend fun upsertProfile(profile: RentalProfile)
    suspend fun upsertTemplateSelection(selection: ChecklistTemplateSelection)
    suspend fun upsertProperty(property: Property)
    suspend fun upsertViewing(viewing: Viewing)
    suspend fun upsertChecklistResult(result: ChecklistResult)
    suspend fun upsertMediaAsset(asset: MediaAsset)
    suspend fun upsertScoreCard(scoreCard: ScoreCard)
    suspend fun upsertComparisonEntry(entry: ComparisonEntry)
    suspend fun upsertSigningSession(session: SigningSession)
    suspend fun upsertSigningMaterial(asset: SigningMaterialAsset)
    suspend fun upsertAiAnalysisResult(result: AiAnalysisResult)
    suspend fun deleteAllDemoData()
}
