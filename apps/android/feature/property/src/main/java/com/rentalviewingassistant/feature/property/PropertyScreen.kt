package com.rentalviewingassistant.feature.property

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rentalviewingassistant.domain.model.AiAnalysisResult
import com.rentalviewingassistant.domain.model.ChecklistResult
import com.rentalviewingassistant.domain.model.ChecklistStage
import com.rentalviewingassistant.domain.model.ComparisonEntry
import com.rentalviewingassistant.domain.model.Property
import com.rentalviewingassistant.domain.model.PropertyStatus
import com.rentalviewingassistant.domain.model.ResultValue
import com.rentalviewingassistant.domain.model.ScoreCard
import com.rentalviewingassistant.domain.model.SigningSession
import com.rentalviewingassistant.domain.model.Viewing
import com.rentalviewingassistant.feature.common.MetricTile
import com.rentalviewingassistant.feature.common.PrototypeCard
import com.rentalviewingassistant.feature.common.PrototypeChip
import com.rentalviewingassistant.feature.common.PrototypeError
import com.rentalviewingassistant.feature.common.PrototypeErrorContainer
import com.rentalviewingassistant.feature.common.PrototypeInfoContainer
import com.rentalviewingassistant.feature.common.PrototypeInfoText
import com.rentalviewingassistant.feature.common.PrototypeWarning
import com.rentalviewingassistant.feature.common.PrototypeWarningContainer
import com.rentalviewingassistant.feature.common.ScoreRow
import com.rentalviewingassistant.feature.common.StatusChip

@Composable
fun PropertyScreen(
    properties: List<Property>,
    viewings: List<Viewing>,
    scoreCards: List<ScoreCard>,
    comparisonEntries: List<ComparisonEntry>,
    checklistResults: List<ChecklistResult>,
    activeSigningSession: SigningSession?,
    onOpenCreateProperty: () -> Unit,
    onOpenEditProperty: (String) -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenPropertyDetail: (String) -> Unit,
    onOpenViewing: (String) -> Unit,
    onOpenSigning: () -> Unit,
    onOpenCompare: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleCandidate: (String) -> Unit,
    onBuildExport: (String) -> Unit,
    onSeedDemoData: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf<PropertyStatus?>(null) }
    var sortState by remember { mutableStateOf<ListSortState?>(null) }

    val filtered = properties.filter { property ->
        val matchesQuery = query.isBlank() ||
            listOf(
                property.title,
                property.communityName,
                property.address,
                property.district,
                property.layoutText,
                property.floorInfo,
                property.orientation,
            ).any { it.contains(query, ignoreCase = true) }
        val matchesFilter = filter == null || property.status == filter
        matchesQuery && matchesFilter
    }
    val visibleProperties = sortState?.let { state -> filtered.sortedWith(state.comparator(scoreCards)) } ?: filtered
    val activeSigningProperty = activeSigningSession?.let { session ->
        properties.firstOrNull { it.id == session.propertyId }
    }
    fun clearListControls() {
        query = ""
        filter = null
        sortState = null
    }

    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 22.dp, end = 16.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                PropertyListTopBar(
                    onOpenCalendar = onOpenCalendar,
                    onOpenStats = onOpenStats,
                )
            }
            item {
                PrototypeSearchField(
                    value = query,
                    onValueChange = { query = it },
                    onClear = { query = "" },
                )
            }
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PrototypeFilterChip("全部 (${properties.size})", filter == null) { filter = null }
                    val statusFilters = buildList {
                        add(PropertyStatus.SHORTLISTED to "候选")
                        add(PropertyStatus.SIGNING to "签约中")
                        add(PropertyStatus.SIGNED to "已签约")
                        add(PropertyStatus.REJECTED to "已排除")
                        if (properties.any { it.status == PropertyStatus.SIGNING_ABANDONED }) {
                            add(PropertyStatus.SIGNING_ABANDONED to "已放弃")
                        }
                        if (properties.any { it.status == PropertyStatus.ARCHIVED }) {
                            add(PropertyStatus.ARCHIVED to "已归档")
                        }
                    }
                    statusFilters.forEach { (status, label) ->
                        val count = properties.count { it.status == status }
                        PrototypeFilterChip("$label ($count)", filter == status) { filter = status }
                    }
                }
            }
            if (properties.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "排序:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        ListSortKey.entries.forEach { sort ->
                            PrototypeFilterChip(sortState.labelFor(sort), sortState?.key == sort) {
                                sortState = sortState.cycle(sort)
                            }
                        }
                        if (sortState != null) {
                            Text(
                                "清除",
                                modifier = Modifier.clickable { sortState = null }.padding(horizontal = 6.dp, vertical = 7.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
            if (activeSigningProperty != null) {
                item {
                    SigningBanner(
                        property = activeSigningProperty,
                        results = checklistResults.filter { it.ownerId == activeSigningSession.id },
                        onOpenSigning = onOpenSigning,
                    )
                }
            }
            if (properties.isEmpty()) {
                item {
                    EmptyPropertyList(
                        onOpenCreateProperty = onOpenCreateProperty,
                        onOpenSettings = onOpenSettings,
                        onSeedDemoData = onSeedDemoData,
                    )
                }
            } else if (visibleProperties.isEmpty()) {
                item {
                    EmptySearchResult(onClear = ::clearListControls)
                }
            }
            items(visibleProperties, key = { it.id }) { property ->
                val propertyViewings = viewings.filter { it.propertyId == property.id }
                PropertyCard(
                    property = property,
                    viewings = propertyViewings,
                    score = scoreCards.latestFor(property.id),
                    selected = comparisonEntries.any { it.propertyId == property.id && it.selected },
                    checklistResults = checklistResults.filter { it.propertyId == property.id && it.stage == ChecklistStage.VIEWING },
                    onOpenPropertyDetail = onOpenPropertyDetail,
                    onOpenEditProperty = onOpenEditProperty,
                    onOpenViewing = onOpenViewing,
                    onOpenSigning = onOpenSigning,
                    onOpenCompare = onOpenCompare,
                    onToggleCandidate = onToggleCandidate,
                    onBuildExport = onBuildExport,
                )
            }
            item {
                Spacer(Modifier.height(8.dp))
            }
        }
        if (properties.isNotEmpty()) {
            FloatingActionButton(
                onClick = onOpenCreateProperty,
                modifier = Modifier.align(Alignment.BottomEnd).padding(18.dp),
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "新增房源")
            }
        }
    }
}

@Composable
private fun EmptyPropertyList(
    onOpenCreateProperty: () -> Unit,
    onOpenSettings: () -> Unit,
    onSeedDemoData: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PrototypeCard {
            Text("还没有房源", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "先添加第一套准备看的房子。只填租金、小区和户型也可以，现场信息之后再补。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onOpenCreateProperty, modifier = Modifier.fillMaxWidth()) {
                Text("新增第一套房源")
            }
            OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
                Text("完善租房偏好")
            }
            OutlinedButton(onClick = onSeedDemoData, modifier = Modifier.fillMaxWidth()) {
                Text("查看示例数据")
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("建议的第一条记录", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(
                    "房源名称、租金、押付、小区、来源链接。现场再补照片、风险和 checklist。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmptySearchResult(onClear: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("没有符合条件的房源", color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedButton(onClick = onClear) {
            Text("清除搜索和筛选")
        }
    }
}

@Composable
private fun PropertyListTopBar(
    onOpenCalendar: () -> Unit,
    onOpenStats: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("我的房源", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("看房助手 · 本地优先", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            IconButton(onClick = onOpenCalendar, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = "看房日历")
            }
            IconButton(onClick = onOpenStats, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Outlined.BarChart, contentDescription = "统计")
            }
        }
    }
}

@Composable
private fun PrototypeSearchField(value: String, onValueChange: (String) -> Unit, onClear: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (value.isBlank()) {
                        Text("搜索房源名称、小区、地址...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    innerTextField()
                },
            )
            if (value.isNotBlank()) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "清除搜索",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp).clickable(onClick = onClear),
                )
            }
        }
    }
}

@Composable
private fun PrototypeFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(999.dp),
        tonalElevation = if (selected) 0.dp else 1.dp,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@Composable
private fun SigningBanner(
    property: Property,
    results: List<ChecklistResult>,
    onOpenSigning: () -> Unit,
) {
    val checkedCount = results.count { it.resultValue != ResultValue.UNCHECKED }
    val riskCount = results.count { it.resultValue == ResultValue.RISK }
    val progress = (checkedCount / 12f).coerceIn(0f, 1f)
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenSigning),
        color = PrototypeWarningContainer,
        contentColor = Color(0xFF3E2E00),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("签约中 (1)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            property.title.substringBefore(" "),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text("$checkedCount 项已核对", style = MaterialTheme.typography.labelSmall)
                        if (riskCount > 0) {
                            PrototypeChip("风险$riskCount", PrototypeErrorContainer, Color(0xFF410E0B))
                        }
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = Color(0xFF3E2E00),
                        trackColor = Color(0x1A000000),
                    )
                }
                Text("进入签约总览", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private enum class ListSortKey(val label: String) {
    SCORE("总分"),
    TIME("添加时间"),
    RENT("租金");
}

private data class ListSortState(
    val key: ListSortKey,
    val descending: Boolean,
) {
    fun comparator(scores: List<ScoreCard>): Comparator<Property> {
        val base = when (key) {
            ListSortKey.SCORE -> compareBy<Property> { scores.latestFor(it.id)?.totalScore ?: 0.0 }
            ListSortKey.TIME -> compareBy { it.createdAt }
            ListSortKey.RENT -> compareBy { it.rentAmount ?: 0 }
        }
        return if (descending) base.reversed() else base
    }
}

private fun ListSortState?.cycle(key: ListSortKey): ListSortState? =
    when {
        this == null || this.key != key -> ListSortState(key, descending = true)
        descending -> copy(descending = false)
        else -> null
    }

private fun ListSortState?.labelFor(key: ListSortKey): String {
    if (this?.key != key) return key.label
    return key.label + if (descending) "↓" else "↑"
}

private enum class CardActionStyle {
    PRIMARY,
    WARNING,
    MUTED,
}

private enum class CardActionTarget {
    DETAIL,
    VIEWING,
    COMPARE,
    SIGNING,
}

private data class CardAction(
    val label: String,
    val reason: String,
    val style: CardActionStyle,
    val target: CardActionTarget,
)

private data class RiskInsight(
    val label: String,
    val detail: String,
    val level: ResultValue,
)

private fun topRiskInsight(results: List<ChecklistResult>, score: ScoreCard?, property: Property): RiskInsight {
    val topRisk = results.firstOrNull { it.resultValue == ResultValue.RISK }
    if (topRisk != null) {
        return RiskInsight(
            label = "最高风险：${topRisk.itemCode}",
            detail = topRisk.comment.ifBlank { "建议先处理后再推进。" },
            level = ResultValue.RISK,
        )
    }
    val topUncertain = results.firstOrNull { it.resultValue == ResultValue.UNCERTAIN }
    if (topUncertain != null) {
        return RiskInsight(
            label = "待确认：${topUncertain.itemCode}",
            detail = topUncertain.comment.ifBlank { "建议补充确认后再判断。" },
            level = ResultValue.UNCERTAIN,
        )
    }
    if (property.status == PropertyStatus.REJECTED) {
        return RiskInsight(
            label = "已排除",
            detail = property.rejectionReason.ifBlank { "已排除，必要时可恢复候选。" },
            level = ResultValue.RISK,
        )
    }
    return RiskInsight(
        label = if (score == null) "等待 checklist 评分" else "暂无明显阻断项",
        detail = if (score == null) "继续补齐看房记录和现场证据。" else "继续补齐看房记录和材料即可。",
        level = ResultValue.OK,
    )
}

private fun Property.nextAction(
    viewings: List<Viewing>,
    riskInsight: RiskInsight,
    score: ScoreCard?,
): CardAction =
    when {
        status == PropertyStatus.SIGNING && riskInsight.level == ResultValue.RISK ->
            CardAction("先处理签前风险", riskInsight.label, CardActionStyle.WARNING, CardActionTarget.SIGNING)
        status == PropertyStatus.SIGNING ->
            CardAction("进入签约总览", "继续核对材料、条款和入住准备", CardActionStyle.PRIMARY, CardActionTarget.SIGNING)
        status == PropertyStatus.SHORTLISTED && riskInsight.level == ResultValue.RISK ->
            CardAction("先复核看房风险", riskInsight.label, CardActionStyle.WARNING, CardActionTarget.DETAIL)
        status == PropertyStatus.SHORTLISTED ->
            CardAction("进入对比决策", "候选房源，适合和其他房源一起比较", CardActionStyle.PRIMARY, CardActionTarget.COMPARE)
        status == PropertyStatus.REJECTED ->
            CardAction("查看排除原因", rejectionReason.ifBlank { "已排除，必要时可恢复候选" }, CardActionStyle.MUTED, CardActionTarget.DETAIL)
        viewings.isEmpty() ->
            CardAction("记录首次看房", "先补现场观察和 checklist", CardActionStyle.PRIMARY, CardActionTarget.VIEWING)
        riskInsight.level == ResultValue.RISK ->
            CardAction("复核风险证据", riskInsight.label, CardActionStyle.WARNING, CardActionTarget.DETAIL)
        else ->
            CardAction(
                "查看详情",
                if (score == null) "补充照片、备注或加入候选" else "已生成评分，可决定是否候选",
                CardActionStyle.PRIMARY,
                CardActionTarget.DETAIL,
            )
    }

@Composable
fun PropertyDetailScreen(
    property: Property?,
    viewings: List<Viewing>,
    scoreCards: List<ScoreCard>,
    comparisonEntries: List<ComparisonEntry>,
    checklistResults: List<ChecklistResult>,
    aiResults: List<AiAnalysisResult>,
    activeSigningSession: SigningSession?,
    onBack: () -> Unit,
    onEditProperty: (String) -> Unit,
    onOpenViewing: (String) -> Unit,
    onToggleCandidate: (String) -> Unit,
    onStartSigning: (String) -> Unit,
    onOpenCompare: () -> Unit,
    onOpenSigning: () -> Unit,
    onOpenAiReview: (String) -> Unit,
    onBuildAiShare: (String) -> Unit,
    onBuildExport: (String) -> Unit,
) {
    if (property == null) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailHeader(title = "房源详情", property = null, onBack = onBack)
            PrototypeCard {
                Text("房源不存在", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("可能已被删除或尚未同步到本地状态。")
            }
        }
        return
    }

    val propertyViewings = viewings.filter { it.propertyId == property.id }
    val score = scoreCards.latestFor(property.id)
    val selected = comparisonEntries.any { it.propertyId == property.id && it.selected }
    val riskCount = checklistResults.count { it.propertyId == property.id && it.resultValue == ResultValue.RISK }
    val uncertainCount = checklistResults.count { it.propertyId == property.id && it.resultValue == ResultValue.UNCERTAIN }
    val latestAi = aiResults
        .filter { it.propertyId == property.id && it.stage == ChecklistStage.VIEWING && it.signingSessionId == null }
        .maxByOrNull { it.updatedAt }
    val propertySigningIsActive = activeSigningSession?.propertyId == property.id
    val anotherSigningIsActive = activeSigningSession != null && !propertySigningIsActive

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            DetailHeader(title = property.title, property = property, onBack = onBack)
        }
        item {
            PrototypeCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                Text(formatRent(property), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(
                    listOf(property.depositRule, property.agencyFee.takeIf { it.isNotBlank() }?.let { "中介费 $it" })
                        .filterNotNull()
                        .joinToString(" · ")
                        .ifBlank { "押付规则待补充" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    listOf(property.layoutText, property.areaSquareMeter?.let { "${it.toInt()}m²" }, property.floorInfo, property.orientation)
                        .filter { it?.isNotBlank() == true }
                        .joinToString(" · ")
                        .ifBlank { "户型、面积、楼层和朝向待补充" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    listOf(property.communityName, property.district, property.address)
                        .filter { it.isNotBlank() }
                        .joinToString(" · ")
                        .ifBlank { "位置待补充" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (property.contactName.isNotBlank() || property.contactPhone.isNotBlank()) {
                    Text("${property.contactName} ${property.contactPhone}".trim(), style = MaterialTheme.typography.bodySmall)
                }
                if (property.sourcePlatform.isNotBlank() || property.listingUrl.isNotBlank()) {
                    Text(
                        listOf(property.sourcePlatform, property.listingUrl).filter { it.isNotBlank() }.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        if (propertySigningIsActive || anotherSigningIsActive) {
            item {
                PrototypeCard(containerColor = if (propertySigningIsActive) MaterialTheme.colorScheme.primaryContainer else PrototypeWarningContainer) {
                    Text(
                        if (propertySigningIsActive) "这套房正在签约中" else "已有其他房源签约中",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        if (propertySigningIsActive) "继续核对签约 checklist、材料和 AI 审查结果。"
                        else "MVP 约束同一时间只允许一个 signing，会阻止这套房直接进入签约。",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (propertySigningIsActive) {
                        Button(onClick = onOpenSigning) { Text("进入签约总览") }
                    }
                }
            }
        }
        item {
            PrototypeCard {
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("综合评分", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(score?.totalScore?.toInt()?.toString() ?: "-", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricTile("看房", "${propertyViewings.size}次", Modifier.weight(1f))
                    MetricTile("风险", riskCount.toString(), Modifier.weight(1f))
                    MetricTile("不确定", uncertainCount.toString(), Modifier.weight(1f))
                }
                ScoreRow("交通", score?.transportScore)
                ScoreRow("环境", score?.environmentScore)
                ScoreRow("户型", score?.layoutScore)
                ScoreRow("设施", score?.facilityScore)
                ScoreRow("风险", score?.riskScore)
                ScoreRow("性价比", score?.pricePerformanceScore)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("看房记录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                PrototypeChip("${propertyViewings.size} 次", PrototypeInfoContainer, PrototypeInfoText)
            }
        }
        if (propertyViewings.isEmpty()) {
            item {
                PrototypeCard {
                    Text("还没有看房记录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("先创建一次看房记录，再填写 checklist、照片说明和评分。")
                    Button(onClick = { onOpenViewing(property.id) }) { Text("开始看房") }
                }
            }
        } else {
            items(propertyViewings, key = { it.id }) { viewing ->
                val viewingResults = checklistResults.filter { it.ownerId == viewing.id }
                val viewingRisks = viewingResults.count { it.resultValue == ResultValue.RISK }
                val viewingUncertain = viewingResults.count { it.resultValue == ResultValue.UNCERTAIN }
                PrototypeCard {
                    Text(
                        listOf(viewing.visitedAt, viewing.scheduledAt).firstOrNull { it.isNotBlank() } ?: "未记录时间",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        viewing.firstImpression.ifBlank { viewing.notes.ifBlank { "现场印象待补充" } },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (viewingRisks > 0) {
                            PrototypeChip("风险 $viewingRisks", PrototypeErrorContainer, PrototypeError)
                        }
                        if (viewingUncertain > 0) {
                            PrototypeChip("不确定 $viewingUncertain", PrototypeWarningContainer, PrototypeWarning)
                        }
                        if (viewingRisks == 0 && viewingUncertain == 0) {
                            PrototypeChip("暂无阻断", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    OutlinedButton(onClick = { onOpenViewing(property.id) }, modifier = Modifier.fillMaxWidth()) {
                        Text("打开看房 checklist")
                    }
                }
            }
        }
        if (latestAi != null) {
            item {
                PrototypeCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Text("AI 结果", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(latestAi.summary.ifBlank { "AI 已导入，但没有摘要字段。" }, style = MaterialTheme.typography.bodySmall)
                    if (latestAi.recommendation.isNotBlank()) {
                        Text("建议：${latestAi.recommendation}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    OutlinedButton(onClick = { onOpenAiReview(property.id) }, modifier = Modifier.fillMaxWidth()) {
                        Text("查看完整 AI 结果")
                    }
                }
            }
        }
        item {
            PrototypeCard {
                Text("下一步动作", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (property.status == PropertyStatus.SIGNING) {
                    Button(onClick = onOpenSigning, modifier = Modifier.fillMaxWidth()) { Text("进入签约总览") }
                } else {
                    Button(onClick = { onStartSigning(property.id) }, modifier = Modifier.fillMaxWidth()) { Text("进入签约核查") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onToggleCandidate(property.id) }, modifier = Modifier.weight(1f)) {
                        Text(if (selected) "隐藏候选" else "加入候选")
                    }
                    OutlinedButton(onClick = { onOpenViewing(property.id) }, modifier = Modifier.weight(1f)) {
                        Text("看房 checklist")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onEditProperty(property.id) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.Edit, contentDescription = null)
                        Text("编辑")
                    }
                    OutlinedButton(onClick = onOpenCompare, modifier = Modifier.weight(1f)) { Text("进入对比") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            if (latestAi == null) onBuildAiShare(property.id) else onOpenAiReview(property.id)
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(if (latestAi == null) "AI 分析" else "AI 结果")
                    }
                    OutlinedButton(onClick = { onBuildExport(property.id) }, modifier = Modifier.weight(1f)) { Text("导出单房") }
                }
            }
        }
    }
}

@Composable
fun PropertyEditorScreen(
    property: Property?,
    onBack: () -> Unit,
    onSave: (
        propertyId: String?,
        title: String,
        communityName: String,
        address: String,
        district: String,
        rentAmount: String,
        depositRule: String,
        agencyFee: String,
        layoutText: String,
        areaSquareMeter: String,
        floorInfo: String,
        orientation: String,
        contactName: String,
        contactPhone: String,
        sourcePlatform: String,
        listingUrl: String,
    ) -> Unit,
) {
    var title by remember(property?.id) { mutableStateOf(property?.title.orEmpty()) }
    var community by remember(property?.id) { mutableStateOf(property?.communityName.orEmpty()) }
    var address by remember(property?.id) { mutableStateOf(property?.address.orEmpty()) }
    var district by remember(property?.id) { mutableStateOf(property?.district.orEmpty()) }
    var rent by remember(property?.id) { mutableStateOf(property?.rentAmount?.toString().orEmpty()) }
    var deposit by remember(property?.id) { mutableStateOf(property?.depositRule ?: "押一付一") }
    var agencyFee by remember(property?.id) { mutableStateOf(property?.agencyFee.orEmpty()) }
    var layout by remember(property?.id) { mutableStateOf(property?.layoutText.orEmpty()) }
    var area by remember(property?.id) { mutableStateOf(property?.areaSquareMeter?.toString().orEmpty()) }
    var floor by remember(property?.id) { mutableStateOf(property?.floorInfo.orEmpty()) }
    var orientation by remember(property?.id) { mutableStateOf(property?.orientation.orEmpty()) }
    var contactName by remember(property?.id) { mutableStateOf(property?.contactName.orEmpty()) }
    var contactPhone by remember(property?.id) { mutableStateOf(property?.contactPhone.orEmpty()) }
    var sourcePlatform by remember(property?.id) { mutableStateOf(property?.sourcePlatform.orEmpty()) }
    var listingUrl by remember(property?.id) { mutableStateOf(property?.listingUrl.orEmpty()) }

    fun save() {
        onSave(
            property?.id,
            title,
            community,
            address,
            district,
            rent,
            deposit,
            agencyFee,
            layout,
            area,
            floor,
            orientation,
            contactName,
            contactPhone,
            sourcePlatform,
            listingUrl,
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                }
                Text(
                    if (property == null) "新增房源" else "编辑房源",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                TextButton(onClick = ::save) { Text("保存") }
            }
        }
        item {
            PrototypeCard {
                Text("基础信息", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(title, { title = it }, label = { Text("房源标题 *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(community, { community = it }, label = { Text("小区/公寓") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(address, { address = it }, label = { Text("地址") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(district, { district = it }, label = { Text("区域") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        }
        item {
            PrototypeCard {
                Text("租金与户型", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(rent, { rent = it }, label = { Text("月租 (元)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(deposit, { deposit = it }, label = { Text("押付规则") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(agencyFee, { agencyFee = it }, label = { Text("中介费") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(layout, { layout = it }, label = { Text("户型") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(area, { area = it }, label = { Text("面积 (m²)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(floor, { floor = it }, label = { Text("楼层") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(orientation, { orientation = it }, label = { Text("朝向") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        }
        item {
            PrototypeCard {
                Text("联系人与来源", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(contactName, { contactName = it }, label = { Text("联系人") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(contactPhone, { contactPhone = it }, label = { Text("电话/微信") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(sourcePlatform, { sourcePlatform = it }, label = { Text("来源平台") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(listingUrl, { listingUrl = it }, label = { Text("房源链接") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = ::save, modifier = Modifier.fillMaxWidth()) {
                    Text("保存房源")
                }
            }
        }
    }
}

@Composable
private fun DetailHeader(title: String, property: Property?, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
        }
        Text(
            title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        property?.let { StatusChip(it.status) }
    }
}

@Composable
private fun PropertyCard(
    property: Property,
    viewings: List<Viewing>,
    score: ScoreCard?,
    selected: Boolean,
    checklistResults: List<ChecklistResult>,
    onOpenPropertyDetail: (String) -> Unit,
    onOpenEditProperty: (String) -> Unit,
    onOpenViewing: (String) -> Unit,
    onOpenSigning: () -> Unit,
    onOpenCompare: () -> Unit,
    onToggleCandidate: (String) -> Unit,
    onBuildExport: (String) -> Unit,
) {
    var menuExpanded by remember(property.id) { mutableStateOf(false) }
    val riskInsight = topRiskInsight(checklistResults, score, property)
    val nextAction = property.nextAction(viewings, riskInsight, score)
    val riskCount = checklistResults.count { it.resultValue == ResultValue.RISK }
    val uncertainCount = checklistResults.count { it.resultValue == ResultValue.UNCERTAIN }
    val latestViewing = viewings.maxByOrNull { it.visitedAt.ifBlank { it.updatedAt } }

    fun runPrimaryAction() {
        when (nextAction.target) {
            CardActionTarget.DETAIL -> onOpenPropertyDetail(property.id)
            CardActionTarget.VIEWING -> onOpenViewing(property.id)
            CardActionTarget.COMPARE -> onOpenCompare()
            CardActionTarget.SIGNING -> onOpenSigning()
        }
    }

    PrototypeCard(
        modifier = Modifier.clickable { onOpenPropertyDetail(property.id) },
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        property.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    StatusChip(property.status)
                }
                Text(
                    property.specLine(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    score?.totalScore?.toInt()?.toString() ?: "-",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text("总分", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
            Text(
                formatRent(property),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "/月 · ${property.depositRule.ifBlank { "押付待补充" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 3.dp),
            )
        }

        val (riskContainer, riskContent) = riskInsight.colors()
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = riskContainer,
            contentColor = riskContent,
            shape = RoundedCornerShape(8.dp),
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(riskInsight.label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(riskInsight.detail, style = MaterialTheme.typography.labelSmall)
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp),
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("下一步", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    val (actionContainer, actionContent) = nextAction.colors()
                    Surface(
                        modifier = Modifier.weight(1f).clickable { runPrimaryAction() },
                        color = actionContainer,
                        contentColor = actionContent,
                        shape = RoundedCornerShape(999.dp),
                    ) {
                        Text(
                            text = nextAction.label,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Outlined.MoreVert, contentDescription = "更多操作")
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("查看详情") },
                                onClick = {
                                    menuExpanded = false
                                    onOpenPropertyDetail(property.id)
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("编辑") },
                                onClick = {
                                    menuExpanded = false
                                    onOpenEditProperty(property.id)
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(if (selected) "隐藏候选" else "加入候选") },
                                onClick = {
                                    menuExpanded = false
                                    onToggleCandidate(property.id)
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("导出单房摘要") },
                                onClick = {
                                    menuExpanded = false
                                    onBuildExport(property.id)
                                },
                            )
                        }
                    }
                }
                Text(
                    "因：${nextAction.reason}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (property.status == PropertyStatus.REJECTED) {
                Text(
                    "排除原因 ${property.rejectionReason.ifBlank { "未填写" }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Text(
                    "最近看房 ${latestViewing?.visitedAt?.take(10)?.ifBlank { "-" } ?: "-"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text("风险 $riskCount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("不确定 $uncertainCount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (selected && property.status != PropertyStatus.SHORTLISTED) {
                    PrototypeChip("候选", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

private fun Property.specLine(): String =
    listOf(
        floorInfo,
        orientation,
        areaSquareMeter?.let { "${it.toInt()}m²" }.orEmpty(),
    ).filter { it.isNotBlank() }.joinToString(" · ")
        .ifBlank {
            listOf(communityName, address).filter { it.isNotBlank() }.joinToString(" · ")
                .ifBlank { "户型、楼层和朝向待补充" }
        }

@Composable
private fun RiskInsight.colors(): Pair<Color, Color> =
    when (level) {
        ResultValue.RISK -> PrototypeErrorContainer to PrototypeError
        ResultValue.UNCERTAIN -> PrototypeWarningContainer to PrototypeWarning
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

@Composable
private fun CardAction.colors(): Pair<Color, Color> =
    when (style) {
        CardActionStyle.WARNING -> PrototypeWarningContainer to PrototypeWarning
        CardActionStyle.MUTED -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurfaceVariant
        CardActionStyle.PRIMARY -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    }

private fun List<ScoreCard>.latestFor(propertyId: String): ScoreCard? =
    filter { it.propertyId == propertyId }.maxByOrNull { it.updatedAt }

private fun formatRent(property: Property): String =
    property.rentAmount?.let { "¥$it" } ?: "-"
