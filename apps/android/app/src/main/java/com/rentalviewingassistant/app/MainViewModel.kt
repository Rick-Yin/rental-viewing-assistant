package com.rentalviewingassistant.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rentalviewingassistant.core.Ids
import com.rentalviewingassistant.core.Time
import com.rentalviewingassistant.domain.model.AiAnalysisResult
import com.rentalviewingassistant.domain.model.ChecklistDefinition
import com.rentalviewingassistant.domain.model.ChecklistResult
import com.rentalviewingassistant.domain.model.ChecklistStage
import com.rentalviewingassistant.domain.model.ChecklistTemplateSelection
import com.rentalviewingassistant.domain.model.ComparisonEntry
import com.rentalviewingassistant.domain.model.MaterialType
import com.rentalviewingassistant.domain.model.MediaAsset
import com.rentalviewingassistant.domain.model.Property
import com.rentalviewingassistant.domain.model.PropertyStatus
import com.rentalviewingassistant.domain.model.RentalProfile
import com.rentalviewingassistant.domain.model.RentalState
import com.rentalviewingassistant.domain.model.ResultValue
import com.rentalviewingassistant.domain.model.Severity
import com.rentalviewingassistant.domain.model.SigningMaterialAsset
import com.rentalviewingassistant.domain.model.SigningSession
import com.rentalviewingassistant.domain.model.Viewing
import com.rentalviewingassistant.domain.model.WorkflowStage
import com.rentalviewingassistant.domain.repository.ChecklistRepository
import com.rentalviewingassistant.domain.repository.RentalRepository
import com.rentalviewingassistant.domain.service.ScoreCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class AppUiState(
    val rentalState: RentalState = RentalState(),
    val viewingChecklist: ChecklistDefinition? = null,
    val signingChecklist: ChecklistDefinition? = null,
    val selectedPropertyId: String? = null,
    val message: String? = null,
    val shareText: String? = null,
) {
    val selectedProperty: Property? = rentalState.properties.firstOrNull { it.id == selectedPropertyId }
    val activeSigningSession: SigningSession? =
        rentalState.signingSessions.firstOrNull { it.status == PropertyStatus.SIGNING }
    val activeSigningProperty: Property? =
        activeSigningSession?.let { session -> rentalState.properties.firstOrNull { it.id == session.propertyId } }
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
    private val checklistRepository: ChecklistRepository,
    private val json: Json,
) : ViewModel() {
    private val selectedPropertyId = MutableStateFlow<String?>(null)
    private val message = MutableStateFlow<String?>(null)
    private val shareText = MutableStateFlow<String?>(null)
    private val viewingChecklist = MutableStateFlow<ChecklistDefinition?>(null)
    private val signingChecklist = MutableStateFlow<ChecklistDefinition?>(null)

    val uiState: StateFlow<AppUiState> = combine(
        rentalRepository.state,
        viewingChecklist,
        signingChecklist,
        selectedPropertyId,
        message,
        shareText,
    ) { values ->
        val state = values[0] as RentalState
        val viewing = values[1] as ChecklistDefinition?
        val signing = values[2] as ChecklistDefinition?
        val selectedId = values[3] as String?
        val currentMessage = values[4] as String?
        val currentShare = values[5] as String?
        val stableSelected = selectedId ?: state.properties.firstOrNull()?.id
        AppUiState(
            rentalState = state,
            viewingChecklist = viewing,
            signingChecklist = signing,
            selectedPropertyId = stableSelected,
            message = currentMessage,
            shareText = currentShare,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState())

    init {
        viewModelScope.launch {
            runCatching {
                viewingChecklist.value = checklistRepository.getChecklist(ChecklistStage.VIEWING)
                signingChecklist.value = checklistRepository.getChecklist(ChecklistStage.SIGNING)
            }.onFailure { message.value = "Checklist 加载失败：${it.message}" }
        }
    }

    fun clearMessage() {
        message.value = null
    }

    fun clearShareText() {
        shareText.value = null
    }

    fun selectProperty(propertyId: String) {
        selectedPropertyId.value = propertyId
    }

    fun saveProfile(
        tenantType: String,
        gender: String,
        coLivingType: String,
        hasPet: String,
        hasChildren: String,
        rentalRegion: String,
    ) = viewModelScope.launch {
        val now = Time.nowIso()
        val profile = RentalProfile(
            id = uiState.value.rentalState.profile?.id ?: Ids.newId("profile"),
            tenantType = tenantType,
            gender = gender,
            specialPurposes = listOfNotNull(
                rentalRegion.takeIf { it.isNotBlank() }?.let { "local_compliance" },
                hasPet.takeIf { it.contains("宠物") }?.let { "pet_friendly" },
                hasChildren.takeIf { it.contains("孩子") || it.contains("陪读") }?.let { "child_friendly" },
            ),
            coLivingType = coLivingType,
            hasPet = hasPet,
            hasChildren = hasChildren,
            rentalRegion = rentalRegion,
            createdAt = uiState.value.rentalState.profile?.createdAt ?: now,
            updatedAt = now,
        )
        val recommendation = recommendTemplates(profile, now)
        rentalRepository.upsertProfile(profile)
        rentalRepository.upsertTemplateSelection(recommendation)
        message.value = "画像和模板推荐已保存"
    }

    fun createProperty(
        title: String,
        communityName: String,
        address: String,
        rentAmount: String,
        layoutText: String,
    ) = saveProperty(
        propertyId = null,
        title = title,
        communityName = communityName,
        address = address,
        district = "",
        rentAmount = rentAmount,
        depositRule = "押一付一",
        agencyFee = "",
        layoutText = layoutText,
        areaSquareMeter = "",
        floorInfo = "",
        orientation = "",
        contactName = "",
        contactPhone = "",
        sourcePlatform = "",
        listingUrl = "",
    )

    fun saveProperty(
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
    ) = viewModelScope.launch {
        val now = Time.nowIso()
        val existing = propertyId?.let { id ->
            uiState.value.rentalState.properties.firstOrNull { it.id == id }
        }
        val property = Property(
            id = existing?.id ?: Ids.newId("property"),
            title = title.ifBlank { communityName.ifBlank { "未命名房源" } },
            communityName = communityName,
            address = address,
            district = district,
            rentAmount = rentAmount.toIntOrNull(),
            depositRule = depositRule.ifBlank { "押一付一" },
            agencyFee = agencyFee,
            layoutText = layoutText,
            areaSquareMeter = areaSquareMeter.toDoubleOrNull(),
            floorInfo = floorInfo,
            orientation = orientation,
            contactName = contactName,
            contactPhone = contactPhone,
            sourcePlatform = sourcePlatform,
            listingUrl = listingUrl,
            status = existing?.status ?: PropertyStatus.TO_VIEW,
            workflowStage = existing?.workflowStage ?: WorkflowStage.VIEWING,
            selectedForSigningAt = existing?.selectedForSigningAt,
            rejectionReason = existing?.rejectionReason.orEmpty(),
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
        )
        rentalRepository.upsertProperty(property)
        selectedPropertyId.value = property.id
        message.value = if (existing == null) "房源已创建" else "房源已保存"
    }

    fun createViewing(propertyId: String, impression: String) = viewModelScope.launch {
        val now = Time.nowIso()
        val viewing = Viewing(
            id = Ids.newId("viewing"),
            propertyId = propertyId,
            visitedAt = now,
            firstImpression = impression,
            createdAt = now,
            updatedAt = now,
        )
        rentalRepository.upsertViewing(viewing)
        uiState.value.rentalState.properties.firstOrNull { it.id == propertyId }?.let {
            rentalRepository.upsertProperty(it.copy(status = PropertyStatus.VIEWED, updatedAt = now))
        }
        message.value = "看房记录已创建"
    }

    fun updateChecklistResult(
        stage: ChecklistStage,
        propertyId: String,
        ownerId: String,
        categoryCode: String,
        itemCode: String,
        resultValue: ResultValue,
        severity: Severity,
        comment: String,
    ) = viewModelScope.launch {
        val now = Time.nowIso()
        val existing = uiState.value.rentalState.checklistResults.firstOrNull {
            it.stage == stage && it.ownerId == ownerId && it.itemCode == itemCode
        }
        val result = ChecklistResult(
            id = existing?.id ?: Ids.newId("check"),
            stage = stage,
            propertyId = propertyId,
            ownerId = ownerId,
            categoryCode = categoryCode,
            itemCode = itemCode,
            resultValue = resultValue,
            severity = severity,
            comment = comment,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
        )
        rentalRepository.upsertChecklistResult(result)
        if (stage == ChecklistStage.VIEWING) {
            recalculateScore(propertyId, ownerId)
        }
    }

    fun addMediaNote(propertyId: String, viewingId: String, note: String, uri: String = "") = viewModelScope.launch {
        val now = Time.nowIso()
        rentalRepository.upsertMediaAsset(
            MediaAsset(
                id = Ids.newId("media"),
                propertyId = propertyId,
                viewingId = viewingId,
                fileName = uri.substringAfterLast('/').ifBlank { "现场照片说明" },
                filePath = uri,
                mimeType = if (uri.isBlank()) "text/plain" else "image/*",
                capturedAt = now,
                note = note,
                createdAt = now,
            ),
        )
        message.value = "照片/说明已记录"
    }

    fun toggleCandidate(propertyId: String) = viewModelScope.launch {
        val now = Time.nowIso()
        val state = uiState.value.rentalState
        val property = state.properties.firstOrNull { it.id == propertyId } ?: return@launch
        val entry = state.comparisonEntries.firstOrNull { it.propertyId == propertyId }
        if (entry == null) {
            rentalRepository.upsertComparisonEntry(
                ComparisonEntry(
                    id = Ids.newId("compare"),
                    propertyId = propertyId,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            rentalRepository.upsertProperty(property.copy(status = PropertyStatus.SHORTLISTED, updatedAt = now))
            message.value = "已加入候选对比"
        } else {
            rentalRepository.upsertComparisonEntry(entry.copy(selected = !entry.selected, updatedAt = now))
            message.value = if (entry.selected) "已从候选中隐藏" else "已恢复候选"
        }
    }

    fun changePropertyStatus(propertyId: String, status: PropertyStatus) = viewModelScope.launch {
        if (status == PropertyStatus.SIGNING) {
            startSigning(propertyId)
            return@launch
        }
        val state = uiState.value.rentalState
        val property = state.properties.firstOrNull { it.id == propertyId } ?: return@launch
        val now = Time.nowIso()
        val workflowStage = when (status) {
            PropertyStatus.SIGNED -> WorkflowStage.SIGNED
            PropertyStatus.SIGNING_ABANDONED -> WorkflowStage.SIGNING_ABANDONED
            else -> WorkflowStage.VIEWING
        }
        val updated = property.copy(
            status = status,
            workflowStage = workflowStage,
            rejectionReason = if (status == PropertyStatus.REJECTED && property.rejectionReason.isBlank()) {
                "从详情页排除"
            } else {
                property.rejectionReason
            },
            updatedAt = now,
        )
        rentalRepository.upsertProperty(updated)
        if (status == PropertyStatus.SHORTLISTED) {
            val entry = state.comparisonEntries.firstOrNull { it.propertyId == propertyId }
            rentalRepository.upsertComparisonEntry(
                entry?.copy(selected = true, updatedAt = now) ?: ComparisonEntry(
                    id = Ids.newId("compare"),
                    propertyId = propertyId,
                    selected = true,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        }
        if (property.status == PropertyStatus.SIGNING && status != PropertyStatus.SIGNING) {
            state.signingSessions.firstOrNull {
                it.propertyId == propertyId && it.status == PropertyStatus.SIGNING
            }?.let { session ->
                rentalRepository.upsertSigningSession(session.copy(status = status, decidedAt = now, updatedAt = now))
            }
        }
        message.value = when (status) {
            PropertyStatus.SHORTLISTED -> "已进入候选"
            PropertyStatus.REJECTED -> "已排除房源"
            PropertyStatus.ARCHIVED -> "已归档房源"
            PropertyStatus.VIEWED -> "已退回已看房"
            else -> "房源状态已更新"
        }
    }

    fun startSigning(propertyId: String) = viewModelScope.launch {
        val state = uiState.value.rentalState
        val property = state.properties.firstOrNull { it.id == propertyId } ?: return@launch
        if (state.viewings.none { it.propertyId == propertyId }) {
            message.value = "先记录至少一次看房，再进入签约核查"
            return@launch
        }
        val active = state.signingSessions.firstOrNull { it.status == PropertyStatus.SIGNING }
        if (active != null && active.propertyId != propertyId) {
            val activeTitle = state.properties.firstOrNull { it.id == active.propertyId }?.title ?: "另一套房"
            message.value = "当前已有「$activeTitle」签约中，请先完成或放弃"
            return@launch
        }
        val now = Time.nowIso()
        val session = active ?: SigningSession(
            id = Ids.newId("signing"),
            propertyId = propertyId,
            startedAt = now,
            createdAt = now,
            updatedAt = now,
        )
        rentalRepository.upsertSigningSession(session.copy(status = PropertyStatus.SIGNING, updatedAt = now))
        rentalRepository.upsertProperty(
            property.copy(
                status = PropertyStatus.SIGNING,
                workflowStage = WorkflowStage.SIGNING,
                selectedForSigningAt = property.selectedForSigningAt ?: now,
                updatedAt = now,
            ),
        )
        selectedPropertyId.value = propertyId
        message.value = "已进入签约阶段"
    }

    fun addSigningMaterial(session: SigningSession, title: String, content: String) = viewModelScope.launch {
        val now = Time.nowIso()
        rentalRepository.upsertSigningMaterial(
            SigningMaterialAsset(
                id = Ids.newId("material"),
                propertyId = session.propertyId,
                signingSessionId = session.id,
                materialType = MaterialType.TEXT_NOTE,
                title = title.ifBlank { "签约材料" },
                textContent = content,
                createdAt = now,
            ),
        )
        message.value = "签约材料已保存"
    }

    fun finishSigning(session: SigningSession, signed: Boolean, notes: String) = viewModelScope.launch {
        val now = Time.nowIso()
        val finalStatus = if (signed) PropertyStatus.SIGNED else PropertyStatus.SIGNING_ABANDONED
        val finalStage = if (signed) WorkflowStage.SIGNED else WorkflowStage.SIGNING_ABANDONED
        rentalRepository.upsertSigningSession(
            session.copy(status = finalStatus, decidedAt = now, decisionNotes = notes, updatedAt = now),
        )
        uiState.value.rentalState.properties.firstOrNull { it.id == session.propertyId }?.let {
            rentalRepository.upsertProperty(it.copy(status = finalStatus, workflowStage = finalStage, updatedAt = now))
        }
        message.value = if (signed) "已标记为签约完成" else "已记录签约放弃"
    }

    fun importAiResult(stage: ChecklistStage, propertyId: String, signingSessionId: String?, rawJson: String) =
        viewModelScope.launch {
            val parsed = runCatching { json.parseToJsonElement(rawJson).jsonObject }
                .getOrElse {
                    message.value = "无法解析，请检查是否为完整 JSON"
                    return@launch
                }
            val expectedVersion = when (stage) {
                ChecklistStage.VIEWING -> "viewing-analysis-result.v1"
                ChecklistStage.SIGNING -> "signing-analysis-result.v1"
            }
            val schemaVersion = parsed.string("schemaVersion")
            val parsedStage = parsed.string("stage")
            if (schemaVersion != expectedVersion) {
                message.value = "AI 输出版本不兼容，期望 $expectedVersion，实际 $schemaVersion"
                return@launch
            }
            if (parsedStage != stage.name.lowercase()) {
                message.value = "AI 结果阶段不匹配"
                return@launch
            }
            if (parsed.string("propertyId") != propertyId) {
                message.value = "AI 结果对应的房源不是当前房源"
                return@launch
            }
            if (stage == ChecklistStage.SIGNING && parsed.string("signingSessionId") != signingSessionId) {
                message.value = "AI 结果对应的签约会话不是当前会话"
                return@launch
            }
            val now = Time.nowIso()
            rentalRepository.upsertAiAnalysisResult(
                AiAnalysisResult(
                    id = Ids.newId("ai"),
                    propertyId = propertyId,
                    signingSessionId = signingSessionId,
                    stage = stage,
                    schemaVersion = schemaVersion.orEmpty(),
                    generatedAt = parsed.string("generatedAt").orEmpty(),
                    rawJson = rawJson,
                    summary = parsed.string("summary").orEmpty(),
                    recommendation = parsed.string("overallRecommendation").orEmpty(),
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            message.value = "AI 结果已导入"
        }

    fun buildAiShare(stage: ChecklistStage, propertyId: String, signingSessionId: String? = null) = viewModelScope.launch {
        val schemaName = when (stage) {
            ChecklistStage.VIEWING -> "viewing-analysis-result.v1.json"
            ChecklistStage.SIGNING -> "signing-analysis-result.v1.json"
        }
        val schema = checklistRepository.getSchemaAssetText(schemaName)
        val state = uiState.value.rentalState
        val property = state.properties.firstOrNull { it.id == propertyId } ?: return@launch
        val results = state.checklistResults.filter { it.propertyId == propertyId && it.stage == stage }
        val media = state.mediaAssets.filter { it.propertyId == propertyId }
        val materials = signingSessionId?.let { id -> state.signingMaterialAssets.filter { it.signingSessionId == id } }.orEmpty()
        val header = if (stage == ChecklistStage.VIEWING) "# 看房分析请求" else "# 签约审查请求"
        shareText.value = buildString {
            appendLine(header)
            appendLine()
            appendLine("## 房源基础信息")
            appendLine("- propertyId：${property.id}")
            appendLine("- 标题：${property.title}")
            appendLine("- 小区：${property.communityName}")
            appendLine("- 地址：${property.address}")
            appendLine("- 月租：${property.rentAmount ?: ""}")
            appendLine("- 押付：${property.depositRule}")
            appendLine("- 户型：${property.layoutText}")
            appendLine()
            appendLine("## 检查项结果")
            results.forEach { appendLine("- ${it.categoryCode}/${it.itemCode}: ${it.resultValue} ${it.severity} ${it.comment}") }
            appendLine()
            appendLine("## 照片说明")
            media.forEach { appendLine("- ${it.note}") }
            if (materials.isNotEmpty()) {
                appendLine()
                appendLine("## 签约材料")
                materials.forEach { appendLine("- ${it.title}: ${it.textContent.ifBlank { it.note }}") }
            }
            appendLine()
            appendLine("## 请求")
            appendLine("请只输出严格符合以下 JSON schema 的 JSON：")
            appendLine(schema)
        }
    }

    fun buildExport(propertyId: String? = null) {
        val state = uiState.value.rentalState
        shareText.value = if (propertyId == null) {
            buildCompareSummary(state)
        } else {
            val signingIds = state.signingSessions
                .filter { it.propertyId == propertyId }
                .map { it.id }
                .toSet()
            json.encodeToString(
                state.copy(
                    properties = state.properties.filter { it.id == propertyId },
                    viewings = state.viewings.filter { it.propertyId == propertyId },
                    checklistResults = state.checklistResults.filter { it.propertyId == propertyId },
                    mediaAssets = state.mediaAssets.filter { it.propertyId == propertyId },
                    scoreCards = state.scoreCards.filter { it.propertyId == propertyId },
                    comparisonEntries = state.comparisonEntries.filter { it.propertyId == propertyId },
                    signingSessions = state.signingSessions.filter { it.propertyId == propertyId },
                    signingMaterialAssets = state.signingMaterialAssets.filter { it.signingSessionId in signingIds },
                    aiAnalysisResults = state.aiAnalysisResults.filter { it.propertyId == propertyId },
                ),
            )
        }
    }

    fun seedDemoData() = viewModelScope.launch {
        rentalRepository.deleteAllDemoData()
        val now = Time.nowIso()
        saveProfile("在职人员", "不填写", "独居", "无宠物", "无孩子", "上海")
        listOf(
            Triple("近地铁一居室", "梧桐里", "2号线附近"),
            Triple("预算友好合租房", "青禾公寓", "大学城旁"),
            Triple("签约候选整租", "云间花园", "商圈步行十分钟"),
        ).forEachIndexed { index, item ->
            val property = Property(
                id = Ids.newId("property"),
                title = item.first,
                communityName = item.second,
                address = item.third,
                rentAmount = 4200 + index * 600,
                depositRule = "押一付一",
                layoutText = if (index == 1) "合租单间" else "一室一厅",
                status = if (index == 2) PropertyStatus.SHORTLISTED else PropertyStatus.VIEWED,
                createdAt = now,
                updatedAt = now,
            )
            rentalRepository.upsertProperty(property)
            rentalRepository.upsertViewing(
                Viewing(
                    id = Ids.newId("viewing"),
                    propertyId = property.id,
                    visitedAt = now,
                    firstImpression = "演示看房记录：交通和采光需要复核",
                    notes = "用于验证 MVP 流程",
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            if (index == 2) {
                rentalRepository.upsertComparisonEntry(
                    ComparisonEntry(
                        id = Ids.newId("compare"),
                        propertyId = property.id,
                        createdAt = now,
                        updatedAt = now,
                    ),
                )
            }
        }
        message.value = "演示数据已生成"
    }

    private suspend fun recalculateScore(propertyId: String, viewingId: String) {
        val checklist = viewingChecklist.value ?: return
        val state = uiState.value.rentalState
        val results = state.checklistResults.filter {
            it.stage == ChecklistStage.VIEWING && it.ownerId == viewingId
        }
        val existing = state.scoreCards.firstOrNull { it.viewingId == viewingId }
        val now = Time.nowIso()
        val score = ScoreCalculator.calculate(
            id = existing?.id ?: Ids.newId("score"),
            propertyId = propertyId,
            viewingId = viewingId,
            checklist = checklist,
            results = results,
            nowIso = now,
        ).copy(createdAt = existing?.createdAt ?: now)
        rentalRepository.upsertScoreCard(score)
    }

    private fun recommendTemplates(profile: RentalProfile, now: String): ChecklistTemplateSelection {
        val viewing = mutableSetOf(
            "viewing.agency_basics",
            "viewing.neighborhood",
            "viewing.property_safety",
            "viewing.living_facilities",
        )
        val signing = mutableSetOf(
            "signing.identity_authorization",
            "signing.payment_deposit",
            "signing.lease_term_exit",
            "signing.delivery_maintenance",
        )
        val reasons = mutableListOf("默认启用基础看房与签约核查模块。")
        if (profile.tenantType.contains("在职") || profile.coLivingType.contains("独居")) {
            viewing += listOf("viewing.commute_stability", "viewing.personal_safety", "viewing.privacy_risk")
            signing += listOf("signing.entry_rules", "signing.rent_stability")
            reasons += "在职或独居场景优先关注通勤、网络、安全和进入权限。"
        }
        if (profile.hasPet.contains("宠物")) {
            viewing += "viewing.pet_friendly"
            signing += "signing.pet_clause"
            reasons += "宠物场景需要提前确认宠物条款、押金和清洁责任。"
        }
        if (profile.hasChildren.contains("孩子") || profile.hasChildren.contains("陪读")) {
            viewing += listOf("viewing.child_friendly", "viewing.neighborhood_for_children")
            signing += "signing.school_and_stability"
            reasons += "陪读或儿童场景需要关注稳定性、安全和材料配合。"
        }
        if (profile.rentalRegion.isNotBlank()) {
            signing += "signing.filing_local"
            reasons += "已填写租住区域，增加地方合规和备案核查。"
        }
        return ChecklistTemplateSelection(
            id = uiState.value.rentalState.templateSelection?.id ?: Ids.newId("template"),
            rentalProfileId = profile.id,
            viewingTemplateCodes = viewing.toList(),
            signingTemplateCodes = signing.toList(),
            recommendationReasons = reasons,
            manuallyEdited = false,
            createdAt = uiState.value.rentalState.templateSelection?.createdAt ?: now,
            updatedAt = now,
        )
    }

    private fun buildCompareSummary(state: RentalState): String = buildString {
        appendLine("# 多房源对比摘要")
        state.properties.forEach { property ->
            val score = state.scoreCards.firstOrNull { it.propertyId == property.id }?.totalScore
            appendLine("- ${property.title}：${property.rentAmount ?: "-"} 元，状态 ${property.status}，总分 ${score ?: "待评分"}")
        }
    }

    private fun JsonObject.string(name: String): String? =
        this[name]?.jsonPrimitive?.contentOrNull
}
