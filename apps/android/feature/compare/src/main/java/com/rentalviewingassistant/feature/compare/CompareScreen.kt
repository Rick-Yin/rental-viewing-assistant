package com.rentalviewingassistant.feature.compare

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rentalviewingassistant.domain.model.ComparisonEntry
import com.rentalviewingassistant.domain.model.Property
import com.rentalviewingassistant.domain.model.ScoreCard
import com.rentalviewingassistant.domain.model.SigningSession
import com.rentalviewingassistant.feature.common.MetricTile
import com.rentalviewingassistant.feature.common.PrototypeCard
import com.rentalviewingassistant.feature.common.PrototypeChip
import com.rentalviewingassistant.feature.common.PrototypeInfoContainer
import com.rentalviewingassistant.feature.common.PrototypeInfoText
import com.rentalviewingassistant.feature.common.ScreenHeader
import com.rentalviewingassistant.feature.common.ScoreRow
import com.rentalviewingassistant.feature.common.StatusChip

@Composable
fun CompareScreen(
    properties: List<Property>,
    comparisonEntries: List<ComparisonEntry>,
    scoreCards: List<ScoreCard>,
    activeSigningSession: SigningSession?,
    onToggleCandidate: (String) -> Unit,
    onStartSigning: (String) -> Unit,
    onOpenViewing: (String) -> Unit,
    onBuildExport: () -> Unit,
) {
    val candidates = properties.filter { property ->
        comparisonEntries.any { it.propertyId == property.id && it.selected }
    }
    val leader = candidates.maxByOrNull { property ->
        scoreCards.firstOrNull { it.propertyId == property.id }?.totalScore ?: -1.0
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ScreenHeader("多房源对比", "${candidates.size} 套候选 · 按总分和风险收敛选择")
        }
        item {
            PrototypeCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                Text("推荐推进", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(leader?.title ?: "等待至少 2 套候选和评分", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    leader?.let { "综合分更高且风险可控，进入签约前仍需复核不确定项。" }
                        ?: "先在房源页加入候选，并在看房 checklist 中完成评分。",
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrototypeChip("${candidates.size} 套候选", PrototypeInfoContainer, PrototypeInfoText)
                    leader?.let {
                        val score = scoreCards.firstOrNull { card -> card.propertyId == it.id }?.totalScore?.toInt()
                        PrototypeChip("${score ?: "-"} 分", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.primary)
                    }
                }
                if (activeSigningSession != null) {
                    Text("已有签约中房源，其他房源进入签约会被限制。", color = MaterialTheme.colorScheme.primary)
                }
                Button(onClick = onBuildExport) { Text("导出对比摘要") }
            }
        }
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("总分", "交通", "环境", "户型", "设施", "风险", "性价比").forEach {
                    PrototypeChip(it, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (candidates.size < 2) {
            item {
                PrototypeCard {
                    Text("候选不足", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("至少需要 2 套候选房源才能形成有效对比。")
                }
            }
        }
        items(properties, key = { it.id }) { property ->
            val selected = comparisonEntries.any { it.propertyId == property.id && it.selected }
            val score = scoreCards.firstOrNull { it.propertyId == property.id }
            PrototypeCard {
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(property.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("月租 ${property.rentAmount ?: "-"} · ${property.communityName.ifBlank { "小区待补充" }}", style = MaterialTheme.typography.bodySmall)
                    }
                    StatusChip(property.status)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricTile("总分", score?.totalScore?.toInt()?.toString() ?: "-", Modifier.weight(1f))
                    MetricTile("风险", score?.riskScore?.toInt()?.toString() ?: "-", Modifier.weight(1f))
                    MetricTile("候选", if (selected) "是" else "否", Modifier.weight(1f))
                }
                ScoreRow("交通", score?.transportScore)
                ScoreRow("环境", score?.environmentScore)
                ScoreRow("户型", score?.layoutScore)
                ScoreRow("设施", score?.facilityScore)
                ScoreRow("风险", score?.riskScore)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onToggleCandidate(property.id) }) {
                        Text(if (selected) "移出候选" else "加入候选")
                    }
                    OutlinedButton(onClick = { onOpenViewing(property.id) }) { Text("看记录") }
                    Button(onClick = { onStartSigning(property.id) }) { Text("进入签约") }
                }
            }
        }
    }
}
