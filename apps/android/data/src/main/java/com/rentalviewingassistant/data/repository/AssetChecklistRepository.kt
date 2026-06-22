package com.rentalviewingassistant.data.repository

import android.content.Context
import com.rentalviewingassistant.domain.model.ChecklistDefinition
import com.rentalviewingassistant.domain.model.ChecklistStage
import com.rentalviewingassistant.domain.repository.ChecklistRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@Singleton
class AssetChecklistRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) : ChecklistRepository {
    override suspend fun getChecklist(stage: ChecklistStage): ChecklistDefinition {
        val assetName = when (stage) {
            ChecklistStage.VIEWING -> "viewing-checklist.json"
            ChecklistStage.SIGNING -> "signing-checklist.json"
        }
        return json.decodeFromString(readAsset(assetName))
    }

    override suspend fun getSchemaAssetText(assetName: String): String = readAsset(assetName)

    private suspend fun readAsset(assetName: String): String = withContext(Dispatchers.IO) {
        context.assets.open(assetName).bufferedReader().use { it.readText() }
    }
}
