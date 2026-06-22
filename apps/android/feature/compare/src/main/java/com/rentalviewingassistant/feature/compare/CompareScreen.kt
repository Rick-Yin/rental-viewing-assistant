package com.rentalviewingassistant.feature.compare

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("对比", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("决策摘要", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("候选房源：${candidates.size} 套")
                    val best = candidates.maxByOrNull { property ->
                        scoreCards.firstOrNull { it.propertyId == property.id }?.totalScore ?: -1.0
                    }
                    Text("推荐推进：${best?.title ?: "等待至少 2 套候选和评分"}")
                    if (activeSigningSession != null) {
                        Text("已有签约中房源，其他房源进入签约会被禁用。", color = MaterialTheme.colorScheme.primary)
                    }
                    Button(onClick = onBuildExport) { Text("导出对比摘要") }
                }
            }
        }
        if (candidates.size < 2) {
            item { Text("至少需要 2 套候选房源才能形成有效对比。") }
        }
        items(properties, key = { it.id }) { property ->
            val selected = comparisonEntries.any { it.propertyId == property.id && it.selected }
            val score = scoreCards.firstOrNull { it.propertyId == property.id }
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(property.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("月租 ${property.rentAmount ?: "-"} / 状态 ${property.status}")
                    Text("总分 ${score?.totalScore ?: "待评分"}  风险 ${score?.riskScore ?: "待检查"}")
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
}
