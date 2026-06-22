package com.rentalviewingassistant.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.rentalviewingassistant.domain.model.ChecklistDefinition
import com.rentalviewingassistant.domain.model.ChecklistTemplateSelection
import com.rentalviewingassistant.domain.model.RentalProfile
import com.rentalviewingassistant.feature.common.MetricTile
import com.rentalviewingassistant.feature.common.PrototypeCard
import com.rentalviewingassistant.feature.common.ScreenHeader

@Composable
fun ProfileScreen(
    profile: RentalProfile?,
    selection: ChecklistTemplateSelection?,
    viewingChecklist: ChecklistDefinition?,
    signingChecklist: ChecklistDefinition?,
    onSaveProfile: (String, String, String, String, String, String) -> Unit,
    onSeedDemoData: () -> Unit,
) {
    var tenantType by remember(profile?.id) { mutableStateOf(profile?.tenantType.orEmpty()) }
    var gender by remember(profile?.id) { mutableStateOf(profile?.gender.orEmpty()) }
    var coLivingType by remember(profile?.id) { mutableStateOf(profile?.coLivingType.orEmpty()) }
    var hasPet by remember(profile?.id) { mutableStateOf(profile?.hasPet.orEmpty()) }
    var hasChildren by remember(profile?.id) { mutableStateOf(profile?.hasChildren.orEmpty()) }
    var rentalRegion by remember(profile?.id) { mutableStateOf(profile?.rentalRegion.orEmpty()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScreenHeader("开始看房前", "先设定租房条件，后面的 checklist 会按场景变轻或变细。")

        PrototypeCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
            Text("建议先完成租房画像", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("预算、同住情况、宠物、孩子、通勤和特殊用途会影响需要重点检查的项目。")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile("设偏好", "1", Modifier.weight(1f))
                MetricTile("看房记录", "2", Modifier.weight(1f))
                MetricTile("签约核查", "3", Modifier.weight(1f))
            }
        }

        PrototypeCard {
            Text("租房画像", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(tenantType, { tenantType = it }, label = { Text("身份，例如 在职人员/大学生") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(gender, { gender = it }, label = { Text("性别，可不填") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(coLivingType, { coLivingType = it }, label = { Text("同住情况，例如 独居") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(hasPet, { hasPet = it }, label = { Text("宠物情况") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(hasChildren, { hasChildren = it }, label = { Text("孩子/陪读情况") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(rentalRegion, { rentalRegion = it }, label = { Text("租住区域") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { onSaveProfile(tenantType, gender, coLivingType, hasPet, hasChildren, rentalRegion) }) {
                    Text("保存画像")
                }
                OutlinedButton(onClick = onSeedDemoData) {
                    Text("保留示例继续体验")
                }
            }
        }

        PrototypeCard {
            Text("模板推荐", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            val reasons = selection?.recommendationReasons.orEmpty()
            if (reasons.isEmpty()) {
                Text("保存画像后生成推荐；也可以跳过画像直接使用默认模块。")
            } else {
                reasons.forEach { Text("• $it") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile("看房模块", "${selection?.viewingTemplateCodes?.size ?: viewingChecklist?.templateModules?.count { it.alwaysEnabled } ?: 0}", Modifier.weight(1f))
                MetricTile("签约模块", "${selection?.signingTemplateCodes?.size ?: signingChecklist?.templateModules?.count { it.alwaysEnabled } ?: 0}", Modifier.weight(1f))
            }
        }
    }
}
