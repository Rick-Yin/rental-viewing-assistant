package com.rentalviewingassistant.domain.service

import com.rentalviewingassistant.domain.model.ChecklistDefinition
import com.rentalviewingassistant.domain.model.ChecklistPriority
import com.rentalviewingassistant.domain.model.ChecklistResult
import com.rentalviewingassistant.domain.model.ResultValue
import com.rentalviewingassistant.domain.model.ScoreCard
import com.rentalviewingassistant.domain.model.Severity
import kotlin.math.roundToInt

object ScoreCalculator {
    private const val VERSION = "1.0.0"

    private val dimensionWeights = mapOf(
        "transportScore" to 0.15,
        "environmentScore" to 0.20,
        "layoutScore" to 0.15,
        "facilityScore" to 0.20,
        "riskScore" to 0.20,
        "pricePerformanceScore" to 0.10,
    )

    private val itemDimensions = mapOf(
        "late_night_safety" to "transportScore",
        "external_noise_sources" to "environmentScore",
        "daily_amenities" to "environmentScore",
        "neighbor_public_area" to "environmentScore",
        "illegal_space_or_partition" to "riskScore",
        "old_building_fire_safety" to "riskScore",
        "renovation_formaldehyde_risk" to "riskScore",
        "natural_light_orientation" to "layoutScore",
        "window_door_sound_seal" to "layoutScore",
        "furniture_mattress_condition" to "layoutScore",
        "kitchen_drain_smoke_flue" to "facilityScore",
        "gas_safety" to "riskScore",
        "bathroom_water_drainage" to "facilityScore",
        "bathroom_mold_odor" to "facilityScore",
        "meter_network_baseline" to "facilityScore",
        "appliance_condition" to "facilityScore",
        "visible_damage" to "facilityScore",
        "agency_reliability" to "pricePerformanceScore",
        "negotiation_leverage" to "pricePerformanceScore",
    )

    fun calculate(
        id: String,
        propertyId: String,
        viewingId: String?,
        checklist: ChecklistDefinition,
        results: List<ChecklistResult>,
        nowIso: String,
    ): ScoreCard {
        val priorityByCode = checklist.categories
            .flatMap { it.items }
            .associate { it.code to it.priority }
        val checked = results.filter { it.resultValue != ResultValue.UNCHECKED }
        val scoresByDimension = checked
            .mapNotNull { result ->
                val dimension = itemDimensions[result.itemCode] ?: return@mapNotNull null
                val priority = priorityByCode[result.itemCode] ?: ChecklistPriority.MEDIUM
                dimension to itemScore(result, priority)
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, scores) -> scores.average().round1() }

        val weighted = scoresByDimension.entries.sumOf { (dimension, score) ->
            score * (dimensionWeights[dimension] ?: 0.0)
        }
        val weightTotal = scoresByDimension.keys.sumOf { dimensionWeights[it] ?: 0.0 }
        val rawTotal = if (weightTotal == 0.0) null else (weighted / weightTotal).round1()
        val hasHighRisk = checked.any { result ->
            itemDimensions[result.itemCode] == "riskScore" &&
                result.resultValue == ResultValue.RISK &&
                result.severity == Severity.HIGH
        }
        val total = rawTotal?.let { if (hasHighRisk) minOf(it, 60.0) else it }

        return ScoreCard(
            id = id,
            propertyId = propertyId,
            viewingId = viewingId,
            transportScore = scoresByDimension["transportScore"],
            environmentScore = scoresByDimension["environmentScore"],
            layoutScore = scoresByDimension["layoutScore"],
            facilityScore = scoresByDimension["facilityScore"],
            riskScore = scoresByDimension["riskScore"],
            pricePerformanceScore = scoresByDimension["pricePerformanceScore"],
            totalScore = total,
            scoringVersion = VERSION,
            createdAt = nowIso,
            updatedAt = nowIso,
        )
    }

    private fun itemScore(result: ChecklistResult, priority: ChecklistPriority): Double {
        val base = when (result.resultValue) {
            ResultValue.OK -> 100.0
            ResultValue.UNCERTAIN -> 50.0
            ResultValue.RISK -> 0.0
            ResultValue.UNCHECKED -> return 0.0
        }
        val severity = when (result.severity) {
            Severity.NONE -> 1.0
            Severity.LOW -> 0.85
            Severity.MEDIUM -> 0.65
            Severity.HIGH -> 0.40
        }
        val weight = when (priority) {
            ChecklistPriority.HIGH -> 1.2
            ChecklistPriority.MEDIUM -> 1.0
            ChecklistPriority.LOW -> 0.8
        }
        return (base * severity * weight).coerceIn(0.0, 100.0)
    }

    private fun Double.round1(): Double = (this * 10).roundToInt() / 10.0
}
