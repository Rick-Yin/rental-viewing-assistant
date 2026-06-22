package com.rentalviewingassistant.feature.property

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddHome
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rentalviewingassistant.domain.model.ComparisonEntry
import com.rentalviewingassistant.domain.model.Property
import com.rentalviewingassistant.domain.model.PropertyStatus
import com.rentalviewingassistant.domain.model.ScoreCard
import com.rentalviewingassistant.domain.model.SigningSession
import com.rentalviewingassistant.domain.model.Viewing
import com.rentalviewingassistant.feature.common.MetricTile
import com.rentalviewingassistant.feature.common.PrototypeCard
import com.rentalviewingassistant.feature.common.PrototypeChip
import com.rentalviewingassistant.feature.common.PrototypeInfoContainer
import com.rentalviewingassistant.feature.common.PrototypeInfoText
import com.rentalviewingassistant.feature.common.ScreenHeader
import com.rentalviewingassistant.feature.common.ScoreRow
import com.rentalviewingassistant.feature.common.StatusChip

@Composable
fun PropertyScreen(
    properties: List<Property>,
    viewings: List<Viewing>,
    scoreCards: List<ScoreCard>,
    comparisonEntries: List<ComparisonEntry>,
    activeSigningSession: SigningSession?,
    onCreateProperty: (String, String, String, String, String) -> Unit,
    onOpenViewing: (String) -> Unit,
    onToggleCandidate: (String) -> Unit,
    onBuildExport: (String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var community by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var rent by remember { mutableStateOf("") }
    var layout by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf<PropertyStatus?>(null) }

    val filtered = properties.filter { property ->
        val matchesQuery = query.isBlank() ||
            listOf(property.title, property.communityName, property.address).any { it.contains(query, ignoreCase = true) }
        val matchesFilter = filter == null || property.status == filter
        matchesQuery && matchesFilter
    }
    val activeSigningProperty = activeSigningSession?.let { session ->
        properties.firstOrNull { it.id == session.propertyId }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ScreenHeader(
                title = "我的房源",
                subtitle = "看房助手 · 本地优先",
                trailing = {
                    PrototypeChip("${properties.size} 套", PrototypeInfoContainer, PrototypeInfoText)
                },
            )
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("搜索房源名称、小区、地址...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChipText("全部 (${properties.size})", filter == null) { filter = null }
                listOf(
                    PropertyStatus.SHORTLISTED to "候选",
                    PropertyStatus.SIGNING to "签约中",
                    PropertyStatus.REJECTED to "已排除",
                    PropertyStatus.VIEWED to "已看房",
                ).forEach { (status, label) ->
                    val count = properties.count { it.status == status }
                    FilterChipText("$label ($count)", filter == status) { filter = status }
                }
            }
        }
        if (activeSigningProperty != null) {
            item {
                PrototypeCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Text("当前签约中", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(activeSigningProperty.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("完成或放弃这套房的签约后，其他候选房源才能进入签约阶段。")
                }
            }
        }
        item {
            PrototypeCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Outlined.AddHome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text("添加第一套房源", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("只填标题、月租和小区也可以，现场细节之后再补。", style = MaterialTheme.typography.bodySmall)
                    }
                }
                OutlinedTextField(title, { title = it }, label = { Text("标题") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(community, { community = it }, label = { Text("小区") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(rent, { rent = it }, label = { Text("月租") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(address, { address = it }, label = { Text("地址") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(layout, { layout = it }, label = { Text("户型") }, modifier = Modifier.fillMaxWidth())
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onCreateProperty(title, community, address, rent, layout)
                        title = ""
                        community = ""
                        address = ""
                        rent = ""
                        layout = ""
                    },
                ) {
                    Text("保存房源")
                }
            }
        }
        if (filtered.isEmpty()) {
            item {
                PrototypeCard {
                    Text("没有匹配房源", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("调整搜索或筛选条件，或者添加一套真实房源。")
                }
            }
        }
        items(filtered, key = { it.id }) { property ->
            PropertyCard(
                property = property,
                viewingCount = viewings.count { it.propertyId == property.id },
                score = scoreCards.firstOrNull { it.propertyId == property.id },
                selected = comparisonEntries.any { it.propertyId == property.id && it.selected },
                onOpenViewing = onOpenViewing,
                onToggleCandidate = onToggleCandidate,
                onBuildExport = onBuildExport,
            )
        }
    }
}

@Composable
private fun FilterChipText(label: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick) { Text(label) }
    }
}

@Composable
private fun PropertyCard(
    property: Property,
    viewingCount: Int,
    score: ScoreCard?,
    selected: Boolean,
    onOpenViewing: (String) -> Unit,
    onToggleCandidate: (String) -> Unit,
    onBuildExport: (String) -> Unit,
) {
    val riskText = when {
        score?.riskScore != null && score.riskScore!! < 60.0 -> "发现风险项，建议先复核"
        score?.totalScore != null -> "暂无明显风险"
        else -> "等待 checklist 评分"
    }
    PrototypeCard {
        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(property.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    listOf(property.communityName, property.address).filter { it.isNotBlank() }.joinToString(" · ")
                        .ifBlank { "房源位置待补充" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatusChip(property.status)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricTile("月租", property.rentAmount?.let { "¥$it" } ?: "-", Modifier.weight(1f))
            MetricTile("看房", "${viewingCount}次", Modifier.weight(1f))
            MetricTile("总分", score?.totalScore?.toInt()?.toString() ?: "-", Modifier.weight(1f))
        }
        ScoreRow("风险", score?.riskScore)
        Text(riskText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onOpenViewing(property.id) }) { Text(if (viewingCount == 0) "记录看房" else "补 checklist") }
            OutlinedButton(onClick = { onToggleCandidate(property.id) }) { Text(if (selected) "隐藏候选" else "加入候选") }
            OutlinedButton(onClick = { onBuildExport(property.id) }) { Text("导出") }
        }
    }
}
