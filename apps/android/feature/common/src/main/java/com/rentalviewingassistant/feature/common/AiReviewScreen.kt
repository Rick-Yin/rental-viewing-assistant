package com.rentalviewingassistant.feature.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rentalviewingassistant.domain.model.AiAnalysisResult
import com.rentalviewingassistant.domain.model.ChecklistStage
import com.rentalviewingassistant.domain.model.Property
import com.rentalviewingassistant.domain.model.ScoreCard
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun AiReviewScreen(
    stage: ChecklistStage,
    property: Property?,
    result: AiAnalysisResult?,
    scoreCard: ScoreCard?,
    onBack: () -> Unit,
    onBuildAiShare: () -> Unit,
    onOpenImportEntry: () -> Unit,
) {
    val title = if (stage == ChecklistStage.SIGNING) "AI 签约审查" else "AI 看房分析"

    if (property == null) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AiHeader(title = title, onBack = onBack)
            PrototypeCard {
                Text("房源不存在", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("可能已被删除或当前本地状态尚未加载。")
            }
        }
        return
    }

    val payload = remember(result?.rawJson) {
        result?.rawJson?.let(::parseAiPayload) ?: AiPayload()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            AiHeader(title = "AI 分析结果", subtitle = property.title, onBack = onBack)
        }
        if (result == null) {
            item {
                PrototypeCard {
                    Text("还没有 AI 分析结果", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("先生成 Quick Share 内容发给外部 AI，再把严格 JSON 回贴导入。", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = onBuildAiShare, modifier = Modifier.fillMaxWidth()) {
                        Text(if (stage == ChecklistStage.SIGNING) "生成 AI 审查内容" else "生成 AI 分析内容")
                    }
                    OutlinedButton(onClick = onOpenImportEntry, modifier = Modifier.fillMaxWidth()) {
                        Text("去导入入口")
                    }
                }
            }
            return@LazyColumn
        }
        payload.riskScore?.let { score ->
            item {
                RiskScoreCard(score)
            }
        }
        item {
            RecommendationCard(
                recommendation = payload.overallRecommendation.ifBlank { result.recommendation },
                summary = result.summary,
                generatedAt = result.generatedAt,
                schemaVersion = result.schemaVersion,
            )
        }
        if (scoreCard != null) {
            item {
                PrototypeCard {
                    Text("房源评分参考", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    ScoreRow("交通", scoreCard.transportScore)
                    ScoreRow("环境", scoreCard.environmentScore)
                    ScoreRow("户型", scoreCard.layoutScore)
                    ScoreRow("设施", scoreCard.facilityScore)
                    ScoreRow("风险", scoreCard.riskScore)
                    ScoreRow("性价比", scoreCard.pricePerformanceScore)
                }
            }
        }
        if (payload.criticalRisks.isNotEmpty()) {
            item {
                AiItemSection("关键风险 (${payload.criticalRisks.size})", payload.criticalRisks)
            }
        }
        if (payload.missingClauses.isNotEmpty()) {
            item {
                AiItemSection("待确认条款 (${payload.missingClauses.size})", payload.missingClauses)
            }
        }
        if (payload.negotiationPoints.isNotEmpty()) {
            item {
                TextListSection("谈判建议", payload.negotiationPoints)
            }
        }
        if (payload.categoryAssessments.isNotEmpty()) {
            item {
                AiItemSection("分类评估", payload.categoryAssessments)
            }
        }
        if (payload.itemAssessments.isNotEmpty()) {
            item {
                AiItemSection("逐项评估 (${payload.itemAssessments.size} 项)", payload.itemAssessments.take(8))
            }
        }
        if (payload.nextSteps.isNotEmpty()) {
            item {
                OrderedTextSection("下一步", payload.nextSteps)
            }
        }
        item {
            PrototypeCard {
                Text("重新分析", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = onOpenImportEntry, modifier = Modifier.fillMaxWidth()) {
                    Text("重新导入 AI 结果")
                }
                Button(onClick = onBuildAiShare, modifier = Modifier.fillMaxWidth()) {
                    Text("重新生成 Quick Share")
                }
            }
        }
    }
}

@Composable
private fun AiHeader(title: String, subtitle: String? = null, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun RiskScoreCard(score: Double) {
    val safeScore = score.coerceIn(0.0, 100.0)
    val (bg, fg) = when {
        safeScore >= 70.0 -> PrototypeSuccessContainer to PrototypeSuccess
        safeScore >= 40.0 -> PrototypeWarningContainer to PrototypeWarning
        else -> PrototypeErrorContainer to PrototypeError
    }
    PrototypeCard(containerColor = bg) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("风险评分", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = fg)
            Text(safeScore.toInt().toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = fg)
        }
        LinearProgressIndicator(
            progress = { (safeScore / 100.0).toFloat() },
            modifier = Modifier.fillMaxWidth(),
            color = fg,
            trackColor = Color.White.copy(alpha = 0.48f),
        )
        Text("100 = 无风险，0 = 极高风险", style = MaterialTheme.typography.labelSmall, color = fg)
    }
}

@Composable
private fun RecommendationCard(recommendation: String, summary: String, generatedAt: String, schemaVersion: String) {
    val lower = recommendation.lowercase()
    val (bg, fg, label) = when {
        lower.contains("not_recommended") || recommendation.contains("不建议") || recommendation.contains("不推荐") ->
            Triple(PrototypeErrorContainer, Color(0xFF410E0B), "不建议推进")
        lower.contains("caution") || recommendation.contains("谨慎") || recommendation.contains("需确认") ->
            Triple(PrototypeWarningContainer, Color(0xFF3E2E00), "谨慎推进")
        lower.contains("proceed") || recommendation.contains("建议") || recommendation.contains("可签约") ->
            Triple(PrototypeSuccessContainer, Color(0xFF1B3A10), "可以推进")
        else -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, "AI 建议")
    }
    PrototypeCard(containerColor = bg) {
        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = fg)
        Text(recommendation.ifBlank { "AI 未提供总体建议字段。" }, style = MaterialTheme.typography.bodySmall, color = fg)
        if (summary.isNotBlank()) {
            Text(summary, style = MaterialTheme.typography.bodySmall, color = fg)
        }
        Text(
            listOf(schemaVersion, generatedAt).filter { it.isNotBlank() }.joinToString(" · "),
            style = MaterialTheme.typography.labelSmall,
            color = fg,
        )
    }
}

@Composable
private fun AiItemSection(title: String, items: List<AiReviewItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        items.forEach { item ->
            PrototypeCard {
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(item.title.ifBlank { "未命名项目" }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        if (item.description.isNotBlank()) {
                            Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (item.suggestion.isNotBlank()) {
                            Text("建议：${item.suggestion}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    SeverityChip(item.severity)
                }
            }
        }
    }
}

@Composable
private fun SeverityChip(severity: String) {
    val normalized = severity.lowercase()
    val (label, bg, fg) = when {
        normalized.contains("high") || severity.contains("高") -> Triple("高", PrototypeErrorContainer, Color(0xFF410E0B))
        normalized.contains("medium") || severity.contains("中") -> Triple("中", PrototypeWarningContainer, Color(0xFF3E2E00))
        normalized.contains("low") || severity.contains("低") -> Triple("低", PrototypeInfoContainer, PrototypeInfoText)
        else -> Triple("项", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }
    PrototypeChip(label, bg, fg)
}

@Composable
private fun TextListSection(title: String, values: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        PrototypeCard {
            values.forEach { value ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("->", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(value, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun OrderedTextSection(title: String, values: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        PrototypeCard {
            values.forEachIndexed { index, value ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${index + 1}.", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(value, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private data class AiPayload(
    val riskScore: Double? = null,
    val overallRecommendation: String = "",
    val criticalRisks: List<AiReviewItem> = emptyList(),
    val missingClauses: List<AiReviewItem> = emptyList(),
    val negotiationPoints: List<String> = emptyList(),
    val categoryAssessments: List<AiReviewItem> = emptyList(),
    val itemAssessments: List<AiReviewItem> = emptyList(),
    val nextSteps: List<String> = emptyList(),
)

private data class AiReviewItem(
    val title: String = "",
    val description: String = "",
    val suggestion: String = "",
    val severity: String = "",
)

private fun parseAiPayload(rawJson: String): AiPayload {
    val root = runCatching { Json.parseToJsonElement(rawJson).jsonObject }.getOrNull() ?: return AiPayload()
    return AiPayload(
        riskScore = root.number("riskScore"),
        overallRecommendation = root.string("overallRecommendation") ?: root.string("recommendation").orEmpty(),
        criticalRisks = root.items("criticalRisks"),
        missingClauses = root.items("missingClauses"),
        negotiationPoints = root.strings("negotiationPoints"),
        categoryAssessments = root.items("categoryAssessments"),
        itemAssessments = root.items("itemAssessments"),
        nextSteps = root.strings("nextSteps"),
    )
}

private fun JsonObject.string(name: String): String? =
    this[name]?.jsonPrimitive?.contentOrNull

private fun JsonObject.number(name: String): Double? =
    this[name]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()

private fun JsonObject.items(name: String): List<AiReviewItem> =
    (this[name] as? JsonArray).orEmpty().map { element ->
        val obj = element as? JsonObject
        if (obj == null) {
            AiReviewItem(title = element.primitiveText())
        } else {
            AiReviewItem(
                title = obj.string("title")
                    ?: obj.string("label")
                    ?: obj.string("category")
                    ?: obj.string("item")
                    ?: obj.string("clause")
                    ?: obj.string("status").orEmpty(),
                description = obj.string("description")
                    ?: obj.string("summary")
                    ?: obj.string("analysis").orEmpty(),
                suggestion = obj.string("suggestion")
                    ?: obj.string("nextAction").orEmpty(),
                severity = obj.string("severity")
                    ?: obj.string("status").orEmpty(),
            )
        }
    }

private fun JsonObject.strings(name: String): List<String> =
    (this[name] as? JsonArray).orEmpty().mapNotNull { element ->
        val obj = element as? JsonObject
        obj?.string("title")
            ?: obj?.string("summary")
            ?: obj?.string("description")
            ?: element.primitiveText().takeIf { it.isNotBlank() }
    }

private fun JsonElement.primitiveText(): String =
    runCatching { jsonPrimitive.contentOrNull.orEmpty() }.getOrDefault(toString())
