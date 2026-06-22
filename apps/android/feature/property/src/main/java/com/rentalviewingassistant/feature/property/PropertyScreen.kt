package com.rentalviewingassistant.feature.property

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.rentalviewingassistant.domain.model.ComparisonEntry
import com.rentalviewingassistant.domain.model.Property
import com.rentalviewingassistant.domain.model.ScoreCard
import com.rentalviewingassistant.domain.model.SigningSession
import com.rentalviewingassistant.domain.model.Viewing

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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("房源", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("新增房源", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(title, { title = it }, label = { Text("标题") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(community, { community = it }, label = { Text("小区") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(address, { address = it }, label = { Text("地址") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(rent, { rent = it }, label = { Text("月租") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(layout, { layout = it }, label = { Text("户型") }, modifier = Modifier.fillMaxWidth())
                    Button(
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
        }
        if (activeSigningSession != null) {
            item {
                Text("当前有签约中房源，其他房源进入签约会被限制。", color = MaterialTheme.colorScheme.primary)
            }
        }
        if (properties.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                    Text("还没有房源，先添加第一套准备看的房子。", modifier = Modifier.padding(16.dp))
                }
            }
        }
        items(properties, key = { it.id }) { property ->
            val viewingCount = viewings.count { it.propertyId == property.id }
            val score = scoreCards.firstOrNull { it.propertyId == property.id }?.totalScore
            val selected = comparisonEntries.any { it.propertyId == property.id && it.selected }
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(property.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${property.communityName} ${property.address}".trim())
                    Text("月租：${property.rentAmount ?: "-"}  户型：${property.layoutText.ifBlank { "-" }}")
                    Text("状态：${property.status}  看房：$viewingCount 次  总分：${score ?: "待评分"}")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onOpenViewing(property.id) }) {
                            Text("看房记录")
                        }
                        OutlinedButton(onClick = { onToggleCandidate(property.id) }) {
                            Text(if (selected) "隐藏候选" else "加入候选")
                        }
                        OutlinedButton(onClick = { onBuildExport(property.id) }) {
                            Text("导出")
                        }
                    }
                }
            }
        }
    }
}
