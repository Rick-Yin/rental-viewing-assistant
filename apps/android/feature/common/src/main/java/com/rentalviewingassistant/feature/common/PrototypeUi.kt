package com.rentalviewingassistant.feature.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rentalviewingassistant.domain.model.PropertyStatus
import com.rentalviewingassistant.domain.model.ResultValue

val PrototypeSuccess = Color(0xFF386A20)
val PrototypeSuccessContainer = Color(0xFFC4F3A5)
val PrototypeWarning = Color(0xFF7C5800)
val PrototypeWarningContainer = Color(0xFFFFDEA6)
val PrototypeError = Color(0xFFB3261E)
val PrototypeErrorContainer = Color(0xFFF9DEDC)
val PrototypeInfoContainer = Color(0xFFD3E3FD)
val PrototypeInfoText = Color(0xFF041E49)
val PrototypeNeutralContainer = Color(0xFFE7E0EC)
val PrototypeNeutralText = Color(0xFF49454F)

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}

@Composable
fun PrototypeCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
fun PrototypeChip(
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun StatusChip(status: PropertyStatus, modifier: Modifier = Modifier) {
    val (label, bg, fg) = when (status) {
        PropertyStatus.DRAFT -> Triple("草稿", PrototypeNeutralContainer, PrototypeNeutralText)
        PropertyStatus.TO_VIEW -> Triple("待看房", PrototypeInfoContainer, PrototypeInfoText)
        PropertyStatus.VIEWED -> Triple("已看房", PrototypeInfoContainer, PrototypeInfoText)
        PropertyStatus.SHORTLISTED -> Triple("候选", PrototypeSuccessContainer, Color(0xFF1B3A10))
        PropertyStatus.SIGNING -> Triple("签约中", PrototypeWarningContainer, Color(0xFF3E2E00))
        PropertyStatus.SIGNED -> Triple("已签约", PrototypeSuccessContainer, Color(0xFF1B3A10))
        PropertyStatus.SIGNING_ABANDONED -> Triple("放弃", PrototypeNeutralContainer, PrototypeNeutralText)
        PropertyStatus.REJECTED -> Triple("已排除", PrototypeErrorContainer, Color(0xFF410E0B))
        PropertyStatus.ARCHIVED -> Triple("已归档", PrototypeNeutralContainer, PrototypeNeutralText)
    }
    PrototypeChip(label, bg, fg, modifier)
}

@Composable
fun ResultChip(resultValue: ResultValue, modifier: Modifier = Modifier) {
    val (label, bg, fg) = when (resultValue) {
        ResultValue.OK -> Triple("✓ OK", PrototypeSuccessContainer, Color(0xFF1B3A10))
        ResultValue.RISK -> Triple("⚠ 风险", PrototypeErrorContainer, Color(0xFF410E0B))
        ResultValue.UNCERTAIN -> Triple("? 不确定", PrototypeWarningContainer, Color(0xFF3E2E00))
        ResultValue.UNCHECKED -> Triple("○ 未检查", PrototypeNeutralContainer, PrototypeNeutralText)
    }
    PrototypeChip(label, bg, fg, modifier)
}

@Composable
fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ScoreRow(label: String, score: Double?) {
    val value = (score ?: 0.0).coerceIn(0.0, 100.0)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(label, modifier = Modifier.width(48.dp), style = MaterialTheme.typography.labelMedium)
        LinearProgressIndicator(
            progress = { (value / 100.0).toFloat() },
            modifier = Modifier.weight(1f).height(8.dp),
            color = if (value >= 60.0) MaterialTheme.colorScheme.primary else PrototypeError,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Text(
            if (score == null) "-" else value.toInt().toString(),
            modifier = Modifier.width(34.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}
