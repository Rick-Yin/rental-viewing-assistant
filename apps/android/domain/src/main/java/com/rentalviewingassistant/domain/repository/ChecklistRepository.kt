package com.rentalviewingassistant.domain.repository

import com.rentalviewingassistant.domain.model.ChecklistDefinition
import com.rentalviewingassistant.domain.model.ChecklistStage

interface ChecklistRepository {
    suspend fun getChecklist(stage: ChecklistStage): ChecklistDefinition
    suspend fun getSchemaAssetText(assetName: String): String
}
