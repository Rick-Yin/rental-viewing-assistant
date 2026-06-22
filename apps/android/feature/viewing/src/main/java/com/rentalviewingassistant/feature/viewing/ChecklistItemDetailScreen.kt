package com.rentalviewingassistant.feature.viewing

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
import com.rentalviewingassistant.domain.model.ChecklistItem
import com.rentalviewingassistant.domain.model.ChecklistPriority
import com.rentalviewingassistant.domain.model.ChecklistResult
import com.rentalviewingassistant.domain.model.ChecklistStage
import com.rentalviewingassistant.domain.model.ChecklistTemplateSelection
import com.rentalviewingassistant.domain.model.Property
import com.rentalviewingassistant.domain.model.ReferenceLink
import com.rentalviewingassistant.domain.model.ResultValue
import com.rentalviewingassistant.domain.model.Severity
import com.rentalviewingassistant.domain.model.enabledCategories
import com.rentalviewingassistant.feature.common.PrototypeCard
import com.rentalviewingassistant.feature.common.PrototypeChip
import com.rentalviewingassistant.feature.common.PrototypeError
import com.rentalviewingassistant.feature.common.PrototypeErrorContainer
import com.rentalviewingassistant.feature.common.PrototypeInfoContainer
import com.rentalviewingassistant.feature.common.PrototypeInfoText
import com.rentalviewingassistant.feature.common.PrototypeWarning
import com.rentalviewingassistant.feature.common.PrototypeWarningContainer
import com.rentalviewingassistant.feature.common.ResultChip

@Composable
fun ChecklistItemDetailScreen(
    stage: ChecklistStage,
    property: Property?,
    ownerId: String?,
    categoryCode: String?,
    itemCode: String?,
    checklist: ChecklistDefinition?,
    selection: ChecklistTemplateSelection?,
    results: List<ChecklistResult>,
    onBack: () -> Unit,
    onUpdateChecklist: (ChecklistStage, String, String, String, String, ResultValue, Severity, String) -> Unit,
) {
    val categories = checklist?.enabledCategories(selection.moduleCodes(stage)).orEmpty()
    val category = categories.firstOrNull { it.code == categoryCode }
    val item = category?.items?.firstOrNull { it.code == itemCode }

    if (property == null || ownerId == null || category == null || item == null) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailHeader(title = "检查项详情", item = null, onBack = onBack)
            PrototypeCard {
                Text("检查项不存在", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("可能是 checklist 模板未启用，或当前看房记录已经变化。")
            }
        }
        return
    }

    val existing = results.firstOrNull {
        it.stage == stage && it.ownerId == ownerId && it.categoryCode == category.code && it.itemCode == item.code
    }
    var comment by remember(ownerId, item.code, existing?.updatedAt) {
        mutableStateOf(existing?.comment.orEmpty())
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            DetailHeader(title = item.label, item = item, onBack = onBack)
        }
        item {
            PrototypeCard {
                Text("检查结果", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(category.label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    ResultChip(existing?.resultValue ?: ResultValue.UNCHECKED)
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            onUpdateChecklist(stage, property.id, ownerId, category.code, item.code, ResultValue.OK, Severity.NONE, comment)
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("OK")
                    }
                    OutlinedButton(
                        onClick = {
                            onUpdateChecklist(stage, property.id, ownerId, category.code, item.code, ResultValue.UNCERTAIN, Severity.MEDIUM, comment)
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("不确定")
                    }
                    OutlinedButton(
                        onClick = {
                            onUpdateChecklist(stage, property.id, ownerId, category.code, item.code, ResultValue.RISK, Severity.HIGH, comment)
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("风险")
                    }
                }
            }
        }
        if (item.whyCheck.isNotBlank() || item.oneLine.isNotBlank()) {
            item {
                TextSection(
                    title = "为什么查",
                    text = item.whyCheck.ifBlank { item.oneLine },
                )
            }
        }
        if (item.riskSignals.isNotEmpty()) {
            item {
                BulletSection(
                    title = "常见风险信号",
                    values = item.riskSignals,
                    marker = "!",
                    markerColor = PrototypeError,
                )
            }
        }
        if (item.howToCheck.isNotEmpty()) {
            item {
                OrderedSection("怎么查", item.howToCheck)
            }
        }
        if (item.captureAdvice.isNotEmpty()) {
            item {
                BulletSection(
                    title = "建议拍什么",
                    values = item.captureAdvice,
                    marker = "相",
                    markerColor = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
        if (item.recordAdvice.isNotBlank()) {
            item {
                TextSection("记录建议", item.recordAdvice)
            }
        }
        if (item.referenceLinks.isNotEmpty()) {
            item {
                ReferenceSection(item.referenceLinks)
            }
        }
    }
}

@Composable
private fun DetailHeader(title: String, item: ChecklistItem?, onBack: () -> Unit) {
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
        item?.let { PriorityChip(it.priority) }
    }
}

@Composable
private fun PriorityChip(priority: ChecklistPriority) {
    when (priority) {
        ChecklistPriority.HIGH -> PrototypeChip("高优先", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        ChecklistPriority.MEDIUM -> PrototypeChip("中优先", PrototypeInfoContainer, PrototypeInfoText)
        ChecklistPriority.LOW -> PrototypeChip("低优先", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TextSection(title: String, text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        PrototypeCard {
            Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BulletSection(title: String, values: List<String>, marker: String, markerColor: androidx.compose.ui.graphics.Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        PrototypeCard {
            values.forEach { value ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(marker, color = markerColor, fontWeight = FontWeight.Bold)
                    Text(value, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun OrderedSection(title: String, values: List<String>) {
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

@Composable
private fun ReferenceSection(links: List<ReferenceLink>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("参考链接", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        PrototypeCard {
            links.forEach { link ->
                Text(link.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                if (link.note.isNotBlank()) {
                    Text(link.note, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (link.url.isNotBlank()) {
                    Text(link.url, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun ChecklistTemplateSelection?.moduleCodes(stage: ChecklistStage): List<String> =
    when (stage) {
        ChecklistStage.VIEWING -> this?.viewingTemplateCodes.orEmpty()
        ChecklistStage.SIGNING -> this?.signingTemplateCodes.orEmpty()
    }
