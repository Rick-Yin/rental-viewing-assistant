package com.rentalviewingassistant.feature.viewing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
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
import com.rentalviewingassistant.domain.model.ChecklistDefinition
import com.rentalviewingassistant.domain.model.ChecklistResult
import com.rentalviewingassistant.domain.model.ChecklistStage
import com.rentalviewingassistant.domain.model.ChecklistTemplateSelection
import com.rentalviewingassistant.domain.model.MediaAsset
import com.rentalviewingassistant.domain.model.Property
import com.rentalviewingassistant.domain.model.ResultValue
import com.rentalviewingassistant.domain.model.Severity
import com.rentalviewingassistant.domain.model.Viewing
import com.rentalviewingassistant.domain.model.enabledCategories
import com.rentalviewingassistant.feature.common.MetricTile
import com.rentalviewingassistant.feature.common.PrototypeCard
import com.rentalviewingassistant.feature.common.ResultChip
import com.rentalviewingassistant.feature.common.ScreenHeader
import com.rentalviewingassistant.feature.common.StatusChip

@Composable
fun ViewingScreen(
    property: Property?,
    viewings: List<Viewing>,
    checklist: ChecklistDefinition?,
    selection: ChecklistTemplateSelection?,
    results: List<ChecklistResult>,
    mediaAssets: List<MediaAsset>,
    onCreateViewing: (String, String) -> Unit,
    onUpdateChecklist: (ChecklistStage, String, String, String, String, ResultValue, Severity, String) -> Unit,
    onAddMediaNote: (String, String, String, String) -> Unit,
    onBuildAiShare: (String) -> Unit,
    onImportAi: (String, String) -> Unit,
) {
    if (property == null) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ScreenHeader("看房 Checklist", "请先在房源页选择或创建房源")
        }
        return
    }
    val propertyViewings = viewings.filter { it.propertyId == property.id }
    val currentViewing = propertyViewings.firstOrNull()
    val moduleCodes = selection?.viewingTemplateCodes.orEmpty()
    val categories = checklist?.enabledCategories(moduleCodes).orEmpty()
    val currentResults = currentViewing?.let { viewing ->
        results.filter { it.ownerId == viewing.id && it.stage == ChecklistStage.VIEWING }
    }.orEmpty()
    val checkedCount = currentResults.count { it.resultValue != ResultValue.UNCHECKED }
    val totalCount = categories.sumOf { it.items.size }
    val riskCount = currentResults.count { it.resultValue == ResultValue.RISK }
    val uncertainCount = currentResults.count { it.resultValue == ResultValue.UNCERTAIN }

    var impression by remember(property.id) { mutableStateOf("") }
    var mediaNote by remember(property.id, currentViewing?.id) { mutableStateOf("") }
    var mediaUri by remember(property.id, currentViewing?.id) { mutableStateOf("") }
    var aiJson by remember(property.id) { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ScreenHeader(
                title = "看房 Checklist",
                subtitle = property.title,
                trailing = { StatusChip(property.status) },
            )
        }
        item {
            PrototypeCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricTile("已检查", "$checkedCount/$totalCount", Modifier.weight(1f))
                    MetricTile("风险", riskCount.toString(), Modifier.weight(1f))
                    MetricTile("不确定", uncertainCount.toString(), Modifier.weight(1f))
                }
                Text("优先标记风险和不确定项，照片和备注只记录会影响决策的证据。", style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            PrototypeCard {
                Text("看房记录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("已记录 ${propertyViewings.size} 次")
                OutlinedTextField(impression, { impression = it }, label = { Text("本次第一印象/风险点") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = { onCreateViewing(property.id, impression); impression = "" }) {
                    Text("新增本次看房")
                }
            }
        }
        if (currentViewing != null) {
            item {
                PrototypeCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("照片与备注", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    OutlinedTextField(mediaUri, { mediaUri = it }, label = { Text("图片 URI/文件路径，可选") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(mediaNote, { mediaNote = it }, label = { Text("照片说明/现场备注") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { onAddMediaNote(property.id, currentViewing.id, mediaNote, mediaUri); mediaNote = ""; mediaUri = "" }) {
                        Text("保存照片说明")
                    }
                    mediaAssets.filter { it.viewingId == currentViewing.id }.forEach {
                        Text("• ${it.note.ifBlank { it.fileName }}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item {
                PrototypeCard {
                    Text("AI 看房分析", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("生成结构化内容，粘贴给外部 AI 后再把 JSON 回贴。", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { onBuildAiShare(property.id) }) { Text("生成 Quick Share") }
                    OutlinedTextField(aiJson, { aiJson = it }, label = { Text("粘贴 AI JSON") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    OutlinedButton(onClick = { onImportAi(property.id, aiJson) }) { Text("导入 AI 结果") }
                }
            }
            items(categories, key = { it.code }) { category ->
                PrototypeCard {
                    val categoryResults = currentResults.filter { it.categoryCode == category.code }
                    Text(
                        "${category.label} (${categoryResults.count { it.resultValue != ResultValue.UNCHECKED }}/${category.items.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    category.items.forEach { item ->
                        val existing = currentResults.firstOrNull { it.itemCode == item.code }
                        var comment by remember(currentViewing.id, item.code, existing?.updatedAt) {
                            mutableStateOf(existing?.comment.orEmpty())
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                Column(Modifier.weight(1f)) {
                                    Text(item.label, fontWeight = FontWeight.SemiBold)
                                    Text(item.oneLine, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                ResultChip(existing?.resultValue ?: ResultValue.UNCHECKED)
                            }
                            OutlinedTextField(comment, { comment = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth())
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(onClick = {
                                    onUpdateChecklist(ChecklistStage.VIEWING, property.id, currentViewing.id, category.code, item.code, ResultValue.OK, Severity.NONE, comment)
                                }) { Text("OK") }
                                OutlinedButton(onClick = {
                                    onUpdateChecklist(ChecklistStage.VIEWING, property.id, currentViewing.id, category.code, item.code, ResultValue.UNCERTAIN, Severity.MEDIUM, comment)
                                }) { Text("不确定") }
                                OutlinedButton(onClick = {
                                    onUpdateChecklist(ChecklistStage.VIEWING, property.id, currentViewing.id, category.code, item.code, ResultValue.RISK, Severity.HIGH, comment)
                                }) { Text("风险") }
                            }
                        }
                    }
                }
            }
        } else {
            item {
                PrototypeCard {
                    Text("先创建一次看房记录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("创建后即可填写 checklist、照片说明和 AI 分析。")
                }
            }
        }
    }
}
