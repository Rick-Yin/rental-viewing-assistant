package com.rentalviewingassistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RentalDao {
    @Query("SELECT * FROM rental_profiles ORDER BY updatedAt DESC")
    fun profiles(): Flow<List<RentalProfileEntity>>

    @Query("SELECT * FROM template_selections ORDER BY updatedAt DESC")
    fun templateSelections(): Flow<List<ChecklistTemplateSelectionEntity>>

    @Query("SELECT * FROM properties ORDER BY updatedAt DESC")
    fun properties(): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM viewings ORDER BY visitedAt DESC, updatedAt DESC")
    fun viewings(): Flow<List<ViewingEntity>>

    @Query("SELECT * FROM checklist_results ORDER BY updatedAt DESC")
    fun checklistResults(): Flow<List<ChecklistResultEntity>>

    @Query("SELECT * FROM media_assets ORDER BY sortOrder ASC, createdAt DESC")
    fun mediaAssets(): Flow<List<MediaAssetEntity>>

    @Query("SELECT * FROM score_cards ORDER BY updatedAt DESC")
    fun scoreCards(): Flow<List<ScoreCardEntity>>

    @Query("SELECT * FROM comparison_entries ORDER BY updatedAt DESC")
    fun comparisonEntries(): Flow<List<ComparisonEntryEntity>>

    @Query("SELECT * FROM signing_sessions ORDER BY updatedAt DESC")
    fun signingSessions(): Flow<List<SigningSessionEntity>>

    @Query("SELECT * FROM signing_material_assets ORDER BY sortOrder ASC, createdAt DESC")
    fun signingMaterialAssets(): Flow<List<SigningMaterialAssetEntity>>

    @Query("SELECT * FROM ai_analysis_results ORDER BY updatedAt DESC")
    fun aiAnalysisResults(): Flow<List<AiAnalysisResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(entity: RentalProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTemplateSelection(entity: ChecklistTemplateSelectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProperty(entity: PropertyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertViewing(entity: ViewingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChecklistResult(entity: ChecklistResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMediaAsset(entity: MediaAssetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertScoreCard(entity: ScoreCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertComparisonEntry(entity: ComparisonEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSigningSession(entity: SigningSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSigningMaterialAsset(entity: SigningMaterialAssetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAiAnalysisResult(entity: AiAnalysisResultEntity)

    @Query("DELETE FROM rental_profiles")
    suspend fun clearProfiles()

    @Query("DELETE FROM template_selections")
    suspend fun clearTemplateSelections()

    @Query("DELETE FROM properties")
    suspend fun clearProperties()

    @Query("DELETE FROM viewings")
    suspend fun clearViewings()

    @Query("DELETE FROM checklist_results")
    suspend fun clearChecklistResults()

    @Query("DELETE FROM media_assets")
    suspend fun clearMediaAssets()

    @Query("DELETE FROM score_cards")
    suspend fun clearScoreCards()

    @Query("DELETE FROM comparison_entries")
    suspend fun clearComparisonEntries()

    @Query("DELETE FROM signing_sessions")
    suspend fun clearSigningSessions()

    @Query("DELETE FROM signing_material_assets")
    suspend fun clearSigningMaterialAssets()

    @Query("DELETE FROM ai_analysis_results")
    suspend fun clearAiAnalysisResults()
}
