package com.rentalviewingassistant.domain.service

import com.rentalviewingassistant.domain.model.AnalysisMapping
import com.rentalviewingassistant.domain.model.ChecklistCategory
import com.rentalviewingassistant.domain.model.ChecklistDefinition
import com.rentalviewingassistant.domain.model.ChecklistItem
import com.rentalviewingassistant.domain.model.ChecklistPriority
import com.rentalviewingassistant.domain.model.ChecklistResult
import com.rentalviewingassistant.domain.model.ChecklistStage
import com.rentalviewingassistant.domain.model.ReferenceLink
import com.rentalviewingassistant.domain.model.ResultValue
import com.rentalviewingassistant.domain.model.Severity
import com.rentalviewingassistant.domain.model.TemplateModule
import com.rentalviewingassistant.domain.model.enabledCategories
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoreCalculatorTest {
    @Test
    fun okChecklistItemsProduceFullScore() {
        val score = ScoreCalculator.calculate(
            id = "score:1",
            propertyId = "property:1",
            viewingId = "viewing:1",
            checklist = checklist(),
            results = listOf(result("late_night_safety", ResultValue.OK, Severity.NONE)),
            nowIso = "2026-01-01T00:00:00Z",
        )

        assertEquals(100.0, score.transportScore)
        assertEquals(100.0, score.totalScore)
    }

    @Test
    fun highRiskSafetyItemCapsTotalScoreAtSixty() {
        val score = ScoreCalculator.calculate(
            id = "score:1",
            propertyId = "property:1",
            viewingId = "viewing:1",
            checklist = checklist(),
            results = listOf(
                result("late_night_safety", ResultValue.OK, Severity.NONE),
                result("gas_safety", ResultValue.RISK, Severity.HIGH),
            ),
            nowIso = "2026-01-01T00:00:00Z",
        )

        assertEquals(0.0, score.riskScore)
        assertTrue((score.totalScore ?: 100.0) <= 60.0)
    }

    @Test
    fun enabledCategoriesIncludesAlwaysEnabledAndSelectedModules() {
        val enabled = checklist().enabledCategories(listOf("viewing.optional")).map { it.code }

        assertEquals(listOf("neighborhood", "kitchen"), enabled)
    }

    private fun checklist() = ChecklistDefinition(
        schemaVersion = "viewing-checklist",
        stage = ChecklistStage.VIEWING,
        contentVersion = "1.0.0",
        updatedAt = "2026-01-01T00:00:00Z",
        categories = listOf(
            ChecklistCategory(
                code = "neighborhood",
                label = "周边",
                items = listOf(item("late_night_safety", ChecklistPriority.HIGH)),
            ),
            ChecklistCategory(
                code = "kitchen",
                label = "厨房",
                items = listOf(item("gas_safety", ChecklistPriority.HIGH)),
            ),
        ),
        templateModules = listOf(
            TemplateModule(
                code = "viewing.base",
                label = "基础",
                reason = "默认",
                categoryCodes = listOf("neighborhood"),
                alwaysEnabled = true,
            ),
            TemplateModule(
                code = "viewing.optional",
                label = "可选",
                reason = "测试",
                categoryCodes = listOf("kitchen"),
            ),
        ),
    )

    private fun item(code: String, priority: ChecklistPriority) = ChecklistItem(
        code = code,
        label = code,
        oneLine = code,
        whyCheck = code,
        howToCheck = listOf(code),
        riskSignals = listOf(code),
        captureAdvice = listOf(code),
        recordAdvice = code,
        referenceLinks = listOf(ReferenceLink("ref", "https://example.com")),
        priority = priority,
        analysisMapping = AnalysisMapping(),
    )

    private fun result(itemCode: String, resultValue: ResultValue, severity: Severity) = ChecklistResult(
        id = "check:$itemCode",
        stage = ChecklistStage.VIEWING,
        propertyId = "property:1",
        ownerId = "viewing:1",
        categoryCode = "test",
        itemCode = itemCode,
        resultValue = resultValue,
        severity = severity,
        createdAt = "2026-01-01T00:00:00Z",
        updatedAt = "2026-01-01T00:00:00Z",
    )
}
