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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddHome
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rentalviewingassistant.domain.model.AiAnalysisResult
import com.rentalviewingassistant.domain.model.ChecklistResult
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
    onOpenCreateProperty: () -> Unit,
    onOpenPropertyDetail: (String) -> Unit,
    onOpenViewing: (String) -> Unit,
    onToggleCandidate: (String) -> Unit,
    onBuildExport: (String) -> Unit,
) {
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
                        Text("新增房源", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("进入独立表单填写租金、押付、户型、联系人和来源链接。", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Button(modifier = Modifier.fillMaxWidth(), onClick = onOpenCreateProperty) {
                    Text("添加一套房源")
                }
            }
        }
        if (filtered.isEmpty()) {
            item {
                PrototypeCard {
                    Text("没有匹配房源", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("调整搜索或筛选条件，或者添加一套真实房源。")
                    OutlinedButton(onClick = onOpenCreateProperty) {
                        Text("新增房源")
                    }
                }
            }
        }
        items(filtered, key = { it.id }) { property ->
            PropertyCard(
                property = property,
                viewingCount = viewings.count { it.propertyId == property.id },
                score = scoreCards.latestFor(property.id),
                selected = comparisonEntries.any { it.propertyId == property.id && it.selected },
                onOpenPropertyDetail = onOpenPropertyDetail,
                onOpenViewing = onOpenViewing,
                onToggleCandidate = onToggleCandidate,
                onBuildExport = onBuildExport,
            )
        }
    }
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
    val latestAi = aiResults.filter { it.propertyId == property.id }.maxByOrNull { it.updatedAt }
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
                    OutlinedButton(onClick = { onBuildAiShare(property.id) }, modifier = Modifier.weight(1f)) { Text("AI 分析") }
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
    onOpenPropertyDetail: (String) -> Unit,
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
            MetricTile("月租", formatRent(property), Modifier.weight(1f))
            MetricTile("看房", "${viewingCount}次", Modifier.weight(1f))
            MetricTile("总分", score?.totalScore?.toInt()?.toString() ?: "-", Modifier.weight(1f))
        }
        ScoreRow("风险", score?.riskScore)
        Text(riskText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onOpenPropertyDetail(property.id) }, modifier = Modifier.weight(1f)) {
                Text("查看详情")
            }
            OutlinedButton(onClick = { onOpenViewing(property.id) }, modifier = Modifier.weight(1f)) {
                Text(if (viewingCount == 0) "记录看房" else "补 checklist")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onToggleCandidate(property.id) }, modifier = Modifier.weight(1f)) {
                Text(if (selected) "隐藏候选" else "加入候选")
            }
            OutlinedButton(onClick = { onBuildExport(property.id) }, modifier = Modifier.weight(1f)) {
                Text("导出")
            }
        }
    }
}

private fun List<ScoreCard>.latestFor(propertyId: String): ScoreCard? =
    filter { it.propertyId == propertyId }.maxByOrNull { it.updatedAt }

private fun formatRent(property: Property): String =
    property.rentAmount?.let { "¥$it" } ?: "-"
