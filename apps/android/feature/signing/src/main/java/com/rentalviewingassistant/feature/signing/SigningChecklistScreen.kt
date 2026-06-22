package com.rentalviewingassistant.feature.signing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import com.rentalviewingassistant.domain.model.ChecklistDefinition
import com.rentalviewingassistant.domain.model.ChecklistResult
import com.rentalviewingassistant.domain.model.ChecklistStage
import com.rentalviewingassistant.domain.model.ChecklistTemplateSelection
import com.rentalviewingassistant.domain.model.Property
import com.rentalviewingassistant.domain.model.ResultValue
import com.rentalviewingassistant.domain.model.Severity
import com.rentalviewingassistant.domain.model.SigningSession
import com.rentalviewingassistant.domain.model.enabledCategories
import com.rentalviewingassistant.feature.common.MetricTile
import com.rentalviewingassistant.feature.common.PrototypeCard
import com.rentalviewingassistant.feature.common.ResultChip
import com.rentalviewingassistant.feature.common.StatusChip

@Composable
fun SigningChecklistScreen(
    session: SigningSession?,
    property: Property?,
    checklist: ChecklistDefinition?,
    selection: ChecklistTemplateSelection?,
    results: List<ChecklistResult>,
    onBack: () -> Unit,
    onUpdateChecklist: (ChecklistStage, String, String, String, String, ResultValue, Severity, String) -> Unit,
    onOpenItemDetail: (propertyId: String, ownerId: String, categoryCode: String, itemCode: String) -> Unit,
) {
    if (session == null || property == null) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ChecklistHeader(title = "签约 Checklist", subtitle = "暂无签约中房源", property = null, onBack = onBack)
            PrototypeCard {
                Text("请先从对比页选择一套候选房源进入签约。")
            }
        }
        return
    }

    val sessionResults = results.filter { it.stage == ChecklistStage.SIGNING && it.ownerId == session.id }
    val riskCount = sessionResults.count { it.resultValue == ResultValue.RISK }
    val uncertainCount = sessionResults.count { it.resultValue == ResultValue.UNCERTAIN }
    val checkedCount = sessionResults.count { it.resultValue != ResultValue.UNCHECKED }
    val categories = checklist?.enabledCategories(selection?.signingTemplateCodes.orEmpty()).orEmpty()
    val totalCount = categories.sumOf { it.items.size }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ChecklistHeader(
                title = "签约 Checklist",
                subtitle = "${property.title} · 签约中",
                property = property,
                onBack = onBack,
            )
        }
        item {
            PrototypeCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricTile("已检查", "$checkedCount/$totalCount", Modifier.weight(1f))
                    MetricTile("风险", riskCount.toString(), Modifier.weight(1f))
                    MetricTile("不确定", uncertainCount.toString(), Modifier.weight(1f))
                }
                Text("先处理高优先级和合同阻断项，再做最终签约确认。", style = MaterialTheme.typography.bodySmall)
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
                        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column(Modifier.weight(1f)) {
                                Text(item.label, fontWeight = FontWeight.SemiBold)
                                Text(item.oneLine, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            ResultChip(existing?.resultValue ?: ResultValue.UNCHECKED)
                        }
                        TextButton(
                            onClick = { onOpenItemDetail(property.id, session.id, category.code, item.code) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("查看检查方法和风险信号")
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
    }
}

@Composable
private fun ChecklistHeader(title: String, subtitle: String, property: Property?, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        property?.let { StatusChip(it.status) }
    }
}
