package com.rentalviewingassistant.feature.signing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rentalviewingassistant.domain.model.AiAnalysisResult
import com.rentalviewingassistant.domain.model.ChecklistDefinition
import com.rentalviewingassistant.domain.model.ChecklistResult
import com.rentalviewingassistant.domain.model.ChecklistStage
import com.rentalviewingassistant.domain.model.ChecklistTemplateSelection
import com.rentalviewingassistant.domain.model.Property
import com.rentalviewingassistant.domain.model.ResultValue
import com.rentalviewingassistant.domain.model.Severity
import com.rentalviewingassistant.domain.model.SigningMaterialAsset
import com.rentalviewingassistant.domain.model.SigningSession
import com.rentalviewingassistant.domain.model.enabledCategories
import com.rentalviewingassistant.feature.common.MetricTile
import com.rentalviewingassistant.feature.common.PrototypeCard
import com.rentalviewingassistant.feature.common.ResultChip
import com.rentalviewingassistant.feature.common.ScreenHeader
import com.rentalviewingassistant.feature.common.StatusChip

@Composable
fun SigningScreen(
    session: SigningSession?,
    property: Property?,
    checklist: ChecklistDefinition?,
    selection: ChecklistTemplateSelection?,
    results: List<ChecklistResult>,
    materials: List<SigningMaterialAsset>,
    aiResults: List<AiAnalysisResult>,
    onUpdateChecklist: (ChecklistStage, String, String, String, String, ResultValue, Severity, String) -> Unit,
    onAddMaterial: (SigningSession, String, String) -> Unit,
    onFinishSigning: (SigningSession, Boolean, String) -> Unit,
    onBuildAiShare: (String, String) -> Unit,
    onImportAi: (String, String, String) -> Unit,
) {
    if (session == null || property == null) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ScreenHeader("签约", "暂无签约中房源，请从对比页选择候选房源")
        }
        return
    }

    var materialTitle by remember(session.id) { mutableStateOf("") }
    var materialContent by remember(session.id) { mutableStateOf("") }
    var decisionNotes by remember(session.id) { mutableStateOf("") }
    var aiJson by remember(session.id) { mutableStateOf("") }
    val sessionResults = results.filter { it.stage == ChecklistStage.SIGNING && it.ownerId == session.id }
    val riskCount = sessionResults.count { it.resultValue == ResultValue.RISK }
    val uncertainCount = sessionResults.count { it.resultValue == ResultValue.UNCERTAIN }
    val checkedCount = sessionResults.count { it.resultValue != ResultValue.UNCHECKED }
    val moduleCodes = selection?.signingTemplateCodes.orEmpty()
    val categories = checklist?.enabledCategories(moduleCodes).orEmpty()
    val totalCount = categories.sumOf { it.items.size }
    val latestAi = aiResults.filter { it.signingSessionId == session.id }.maxByOrNull { it.updatedAt }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ScreenHeader(
                title = "签约总览",
                subtitle = property.title,
                trailing = { StatusChip(property.status) },
            )
        }
        item {
            PrototypeCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                Text("签前阻断项", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricTile("进度", "$checkedCount/$totalCount", Modifier.weight(1f))
                    MetricTile("风险", riskCount.toString(), Modifier.weight(1f))
                    MetricTile("不确定", uncertainCount.toString(), Modifier.weight(1f))
                }
                Text("优先处理身份授权、付款押金、维修边界和口头承诺是否写入合同。", style = MaterialTheme.typography.bodySmall)
                if (latestAi != null) {
                    Text("AI：${latestAi.recommendation} · ${latestAi.summary}", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item {
            PrototypeCard {
                Text("签约材料", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(materialTitle, { materialTitle = it }, label = { Text("标题") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(materialContent, { materialContent = it }, label = { Text("条款摘录/聊天承诺") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                Button(onClick = {
                    onAddMaterial(session, materialTitle, materialContent)
                    materialTitle = ""
                    materialContent = ""
                }) { Text("保存材料") }
                materials.forEach { Text("• ${it.title}: ${it.textContent}", style = MaterialTheme.typography.bodySmall) }
            }
        }
        item {
            PrototypeCard {
                Text("AI 签约审查", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("把签约 checklist 和材料发给外部 AI，回贴 JSON 后在这里展示。", style = MaterialTheme.typography.bodySmall)
                Button(onClick = { onBuildAiShare(property.id, session.id) }) { Text("生成 Quick Share") }
                OutlinedTextField(aiJson, { aiJson = it }, label = { Text("粘贴 AI JSON") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                OutlinedButton(onClick = { onImportAi(property.id, session.id, aiJson) }) { Text("导入 AI 审查") }
            }
        }
        items(categories, key = { it.code }) { category ->
            PrototypeCard {
                val categoryResults = sessionResults.filter { it.categoryCode == category.code }
                Text(
                    "${category.label} (${categoryResults.count { it.resultValue != ResultValue.UNCHECKED }}/${category.items.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                category.items.forEach { item ->
                    val existing = sessionResults.firstOrNull { it.itemCode == item.code }
                    var comment by remember(session.id, item.code, existing?.updatedAt) {
                        mutableStateOf(existing?.comment.orEmpty())
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(item.label, fontWeight = FontWeight.SemiBold)
                                Text(item.oneLine, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            ResultChip(existing?.resultValue ?: ResultValue.UNCHECKED)
                        }
                        OutlinedTextField(comment, { comment = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(onClick = {
                                onUpdateChecklist(ChecklistStage.SIGNING, property.id, session.id, category.code, item.code, ResultValue.OK, Severity.NONE, comment)
                            }) { Text("OK") }
                            OutlinedButton(onClick = {
                                onUpdateChecklist(ChecklistStage.SIGNING, property.id, session.id, category.code, item.code, ResultValue.UNCERTAIN, Severity.MEDIUM, comment)
                            }) { Text("不确定") }
                            OutlinedButton(onClick = {
                                onUpdateChecklist(ChecklistStage.SIGNING, property.id, session.id, category.code, item.code, ResultValue.RISK, Severity.HIGH, comment)
                            }) { Text("风险") }
                        }
                    }
                }
            }
        }
        item {
            PrototypeCard {
                Text("终态确认", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("仍有必须确认项时，签约前请人工核验。", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(decisionNotes, { decisionNotes = it }, label = { Text("决策备注/放弃原因") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onFinishSigning(session, true, decisionNotes) }) { Text("已签约") }
                    OutlinedButton(onClick = { onFinishSigning(session, false, decisionNotes) }) { Text("签约放弃") }
                }
            }
        }
    }
}
