package com.rentalviewingassistant.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        RentalProfileEntity::class,
        ChecklistTemplateSelectionEntity::class,
        PropertyEntity::class,
        ViewingEntity::class,
        ChecklistResultEntity::class,
        MediaAssetEntity::class,
        ScoreCardEntity::class,
        ComparisonEntryEntity::class,
        SigningSessionEntity::class,
        SigningMaterialAssetEntity::class,
        AiAnalysisResultEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class RentalDatabase : RoomDatabase() {
    abstract fun rentalDao(): RentalDao
}
