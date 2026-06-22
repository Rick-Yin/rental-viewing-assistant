package com.rentalviewingassistant.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rentalviewingassistant.app.ui.theme.RentalViewingAssistantTheme
import com.rentalviewingassistant.domain.model.ChecklistStage
import com.rentalviewingassistant.domain.model.Property
import com.rentalviewingassistant.domain.model.PropertyStatus
import com.rentalviewingassistant.domain.model.ScoreCard
import com.rentalviewingassistant.domain.model.Viewing
import com.rentalviewingassistant.feature.common.MetricTile
import com.rentalviewingassistant.feature.common.PrototypeCard
import com.rentalviewingassistant.feature.common.StatusChip
import com.rentalviewingassistant.feature.common.AiReviewScreen
import com.rentalviewingassistant.feature.compare.CompareScreen
import com.rentalviewingassistant.feature.profile.ProfileScreen
import com.rentalviewingassistant.feature.property.PropertyDetailScreen
import com.rentalviewingassistant.feature.property.PropertyEditorScreen
import com.rentalviewingassistant.feature.property.PropertyScreen
import com.rentalviewingassistant.feature.signing.SigningChecklistScreen
import com.rentalviewingassistant.feature.signing.SigningScreen
import com.rentalviewingassistant.feature.viewing.ChecklistItemDetailScreen
import com.rentalviewingassistant.feature.viewing.ViewingScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RentalViewingAssistantTheme {
                RentalViewingAssistantApp()
            }
        }
    }
}

private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val destinations = listOf(
    BottomDestination("properties", "房源", Icons.Outlined.Home),
    BottomDestination("compare", "对比", Icons.AutoMirrored.Outlined.CompareArrows),
    BottomDestination("profile", "设置", Icons.Outlined.Settings),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RentalViewingAssistantApp(
    viewModel: MainViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route ?: "properties"
    val showAppChrome = route in chromeRoutes
    val showLegacyTopBar = false

    Scaffold(
        topBar = {
            if (showLegacyTopBar) {
                TopAppBar(
                    title = {
                        Column {
                            Text("看房助手", fontWeight = FontWeight.Bold)
                            Text(
                                text = "本地优先 · Android MVP",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (showAppChrome) {
                PrototypeBottomBar(
                    currentRoute = route,
                    onNavigate = { navController.go(it) },
                )
            }
        },
    ) { padding ->
        RentalNavHost(
            navController = navController,
            padding = padding,
            uiState = uiState,
            viewModel = viewModel,
        )
    }

    uiState.message?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::clearMessage,
            confirmButton = {
                TextButton(onClick = viewModel::clearMessage) {
                    Text("知道了")
                }
            },
            title = { Text("提示") },
            text = { Text(message) },
        )
    }

    uiState.shareText?.let { text ->
        AlertDialog(
            onDismissRequest = viewModel::clearShareText,
            confirmButton = {
                TextButton(onClick = viewModel::clearShareText) {
                    Text("关闭")
                }
            },
            title = { Text("导出 / Quick Share") },
            text = {
                Text(
                    text = text,
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(top = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            },
        )
    }
}

@Composable
private fun RentalNavHost(
    navController: NavHostController,
    padding: PaddingValues,
    uiState: AppUiState,
    viewModel: MainViewModel,
) {
    val state = uiState.rentalState
    NavHost(
        navController = navController,
        startDestination = "properties",
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
    ) {
        composable("properties") {
            PropertyScreen(
                properties = state.properties,
                viewings = state.viewings,
                scoreCards = state.scoreCards,
                comparisonEntries = state.comparisonEntries,
                checklistResults = state.checklistResults,
                activeSigningSession = uiState.activeSigningSession,
                onOpenCreateProperty = { navController.navigate("property/new") },
                onOpenEditProperty = { propertyId -> navController.navigate("property/$propertyId/edit") },
                onOpenCalendar = { navController.navigate("calendar") },
                onOpenStats = { navController.navigate("stats") },
                onOpenPropertyDetail = { propertyId ->
                    viewModel.selectProperty(propertyId)
                    navController.navigate("property/$propertyId")
                },
                onOpenViewing = { propertyId ->
                    viewModel.selectProperty(propertyId)
                    navController.go("viewing")
                },
                onOpenSigning = { navController.go("signing") },
                onOpenCompare = { navController.go("compare") },
                onOpenSettings = { navController.go("profile") },
                onToggleCandidate = viewModel::toggleCandidate,
                onBuildExport = viewModel::buildExport,
                onSeedDemoData = viewModel::seedDemoData,
            )
        }
        composable("calendar") {
            CalendarOverviewScreen(
                properties = state.properties,
                viewings = state.viewings,
                onBack = { navController.backOrProperties() },
            )
        }
        composable("stats") {
            StatsOverviewScreen(
                properties = state.properties,
                viewings = state.viewings,
                scoreCards = state.scoreCards,
                onBack = { navController.backOrProperties() },
            )
        }
        composable("property/new") {
            PropertyEditorScreen(
                property = null,
                onBack = { navController.backOrProperties() },
                onSave = { propertyId, title, communityName, address, district, rentAmount, depositRule, agencyFee,
                        layoutText, areaSquareMeter, floorInfo, orientation, contactName, contactPhone, sourcePlatform,
                        listingUrl ->
                    viewModel.saveProperty(
                        propertyId,
                        title,
                        communityName,
                        address,
                        district,
                        rentAmount,
                        depositRule,
                        agencyFee,
                        layoutText,
                        areaSquareMeter,
                        floorInfo,
                        orientation,
                        contactName,
                        contactPhone,
                        sourcePlatform,
                        listingUrl,
                    )
                    navController.backOrProperties()
                },
            )
        }
        composable("property/{propertyId}") { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getString("propertyId")
            val property = state.properties.firstOrNull { it.id == propertyId }
            PropertyDetailScreen(
                property = property,
                viewings = state.viewings,
                scoreCards = state.scoreCards,
                comparisonEntries = state.comparisonEntries,
                checklistResults = state.checklistResults,
                aiResults = state.aiAnalysisResults,
                activeSigningSession = uiState.activeSigningSession,
                onBack = { navController.backOrProperties() },
                onEditProperty = { id -> navController.navigate("property/$id/edit") },
                onOpenViewing = { id ->
                    viewModel.selectProperty(id)
                    navController.go("viewing")
                },
                onToggleCandidate = viewModel::toggleCandidate,
                onStartSigning = viewModel::startSigning,
                onOpenCompare = { navController.go("compare") },
                onOpenSigning = { navController.go("signing") },
                onOpenAiReview = { id -> navController.navigate("ai/viewing/$id") },
                onBuildAiShare = { propertyId -> viewModel.buildAiShare(ChecklistStage.VIEWING, propertyId) },
                onBuildExport = viewModel::buildExport,
            )
        }
        composable("property/{propertyId}/edit") { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getString("propertyId")
            val property = state.properties.firstOrNull { it.id == propertyId }
            PropertyEditorScreen(
                property = property,
                onBack = { navController.backOrProperties() },
                onSave = { savedPropertyId, title, communityName, address, district, rentAmount, depositRule, agencyFee,
                        layoutText, areaSquareMeter, floorInfo, orientation, contactName, contactPhone, sourcePlatform,
                        listingUrl ->
                    viewModel.saveProperty(
                        savedPropertyId,
                        title,
                        communityName,
                        address,
                        district,
                        rentAmount,
                        depositRule,
                        agencyFee,
                        layoutText,
                        areaSquareMeter,
                        floorInfo,
                        orientation,
                        contactName,
                        contactPhone,
                        sourcePlatform,
                        listingUrl,
                    )
                    navController.backOrProperties()
                },
            )
        }
        composable("viewing") {
            ViewingScreen(
                property = uiState.selectedProperty,
                viewings = state.viewings,
                checklist = uiState.viewingChecklist,
                selection = state.templateSelection,
                results = state.checklistResults,
                mediaAssets = state.mediaAssets,
                onCreateViewing = viewModel::createViewing,
                onUpdateChecklist = viewModel::updateChecklistResult,
                onAddMediaNote = viewModel::addMediaNote,
                onBuildAiShare = { propertyId -> viewModel.buildAiShare(ChecklistStage.VIEWING, propertyId) },
                onImportAi = { propertyId, raw -> viewModel.importAiResult(ChecklistStage.VIEWING, propertyId, null, raw) },
                onOpenItemDetail = { propertyId, ownerId, categoryCode, itemCode ->
                    navController.navigate("checklist/viewing/$propertyId/$ownerId/$categoryCode/$itemCode")
                },
            )
        }
        composable("ai/viewing/{propertyId}") { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getString("propertyId")
            val property = state.properties.firstOrNull { it.id == propertyId }
            val latestResult = state.aiAnalysisResults
                .filter { it.propertyId == propertyId && it.stage == ChecklistStage.VIEWING && it.signingSessionId == null }
                .maxByOrNull { it.updatedAt }
            AiReviewScreen(
                stage = ChecklistStage.VIEWING,
                property = property,
                result = latestResult,
                scoreCard = state.scoreCards.filter { it.propertyId == propertyId }.maxByOrNull { it.updatedAt },
                onBack = { navController.backOrProperties() },
                onBuildAiShare = { propertyId?.let { viewModel.buildAiShare(ChecklistStage.VIEWING, it) } },
                onOpenImportEntry = {
                    propertyId?.let { viewModel.selectProperty(it) }
                    navController.go("viewing")
                },
            )
        }
        composable("ai/signing/{propertyId}/{signingSessionId}") { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getString("propertyId")
            val signingSessionId = backStackEntry.arguments?.getString("signingSessionId")
            val property = state.properties.firstOrNull { it.id == propertyId }
            val latestResult = state.aiAnalysisResults
                .filter {
                    it.propertyId == propertyId &&
                        it.stage == ChecklistStage.SIGNING &&
                        it.signingSessionId == signingSessionId
                }
                .maxByOrNull { it.updatedAt }
            AiReviewScreen(
                stage = ChecklistStage.SIGNING,
                property = property,
                result = latestResult,
                scoreCard = state.scoreCards.filter { it.propertyId == propertyId }.maxByOrNull { it.updatedAt },
                onBack = { navController.backOrProperties() },
                onBuildAiShare = {
                    if (propertyId != null && signingSessionId != null) {
                        viewModel.buildAiShare(ChecklistStage.SIGNING, propertyId, signingSessionId)
                    }
                },
                onOpenImportEntry = { navController.go("signing") },
            )
        }
        composable("checklist/viewing/{propertyId}/{ownerId}/{categoryCode}/{itemCode}") { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getString("propertyId")
            val ownerId = backStackEntry.arguments?.getString("ownerId")
            val categoryCode = backStackEntry.arguments?.getString("categoryCode")
            val itemCode = backStackEntry.arguments?.getString("itemCode")
            ChecklistItemDetailScreen(
                stage = ChecklistStage.VIEWING,
                property = state.properties.firstOrNull { it.id == propertyId },
                ownerId = ownerId,
                categoryCode = categoryCode,
                itemCode = itemCode,
                checklist = uiState.viewingChecklist,
                selection = state.templateSelection,
                results = state.checklistResults,
                onBack = { navController.backOrProperties() },
                onUpdateChecklist = viewModel::updateChecklistResult,
            )
        }
        composable("checklist/signing/{propertyId}/{ownerId}/{categoryCode}/{itemCode}") { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getString("propertyId")
            val ownerId = backStackEntry.arguments?.getString("ownerId")
            val categoryCode = backStackEntry.arguments?.getString("categoryCode")
            val itemCode = backStackEntry.arguments?.getString("itemCode")
            ChecklistItemDetailScreen(
                stage = ChecklistStage.SIGNING,
                property = state.properties.firstOrNull { it.id == propertyId },
                ownerId = ownerId,
                categoryCode = categoryCode,
                itemCode = itemCode,
                checklist = uiState.signingChecklist,
                selection = state.templateSelection,
                results = state.checklistResults,
                onBack = { navController.backOrProperties() },
                onUpdateChecklist = viewModel::updateChecklistResult,
            )
        }
        composable("compare") {
            CompareScreen(
                properties = state.properties,
                comparisonEntries = state.comparisonEntries,
                scoreCards = state.scoreCards,
                activeSigningSession = uiState.activeSigningSession,
                onToggleCandidate = viewModel::toggleCandidate,
                onStartSigning = viewModel::startSigning,
                onOpenViewing = { propertyId ->
                    viewModel.selectProperty(propertyId)
                    navController.go("viewing")
                },
                onBuildExport = { viewModel.buildExport(null) },
            )
        }
        composable("signing") {
            SigningScreen(
                session = uiState.activeSigningSession,
                property = uiState.activeSigningProperty,
                checklist = uiState.signingChecklist,
                selection = state.templateSelection,
                results = state.checklistResults,
                materials = state.signingMaterialAssets.filter {
                    it.signingSessionId == uiState.activeSigningSession?.id
                },
                aiResults = state.aiAnalysisResults,
                onAddMaterial = viewModel::addSigningMaterial,
                onFinishSigning = viewModel::finishSigning,
                onBuildAiShare = { propertyId, sessionId ->
                    viewModel.buildAiShare(ChecklistStage.SIGNING, propertyId, sessionId)
                },
                onImportAi = { propertyId, sessionId, raw ->
                    viewModel.importAiResult(ChecklistStage.SIGNING, propertyId, sessionId, raw)
                },
                onOpenAiReview = { propertyId, sessionId -> navController.navigate("ai/signing/$propertyId/$sessionId") },
                onOpenChecklist = { navController.navigate("signing/checklist") },
            )
        }
        composable("signing/checklist") {
            SigningChecklistScreen(
                session = uiState.activeSigningSession,
                property = uiState.activeSigningProperty,
                checklist = uiState.signingChecklist,
                selection = state.templateSelection,
                results = state.checklistResults,
                onBack = { navController.go("signing") },
                onUpdateChecklist = viewModel::updateChecklistResult,
                onOpenItemDetail = { propertyId, ownerId, categoryCode, itemCode ->
                    navController.navigate("checklist/signing/$propertyId/$ownerId/$categoryCode/$itemCode")
                },
            )
        }
        composable("profile") {
            ProfileScreen(
                profile = state.profile,
                selection = state.templateSelection,
                viewingChecklist = uiState.viewingChecklist,
                signingChecklist = uiState.signingChecklist,
                onSaveProfile = viewModel::saveProfile,
                onSeedDemoData = viewModel::seedDemoData,
            )
        }
    }
}

@Composable
private fun CalendarOverviewScreen(
    properties: List<Property>,
    viewings: List<Viewing>,
    onBack: () -> Unit,
) {
    val recentViewings = viewings.take(20)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            UtilityHeader(title = "看房日历", subtitle = "${viewings.size} 条看房记录", onBack = onBack)
        }
        if (recentViewings.isEmpty()) {
            item {
                PrototypeCard {
                    Text("暂无看房安排", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("先从房源列表进入看房流程，创建第一条现场记录。")
                }
            }
        } else {
            items(recentViewings, key = { it.id }) { viewing ->
                val property = properties.firstOrNull { it.id == viewing.propertyId }
                PrototypeCard {
                    Text(
                        listOf(viewing.visitedAt, viewing.scheduledAt).firstOrNull { it.isNotBlank() }?.take(16) ?: "时间待补充",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(property?.title ?: "未知房源", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        viewing.firstImpression.ifBlank { viewing.notes.ifBlank { property?.address ?: "现场印象待补充" } },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsOverviewScreen(
    properties: List<Property>,
    viewings: List<Viewing>,
    scoreCards: List<ScoreCard>,
    onBack: () -> Unit,
) {
    val averageScore = scoreCards.mapNotNull { it.totalScore }.average().takeIf { !it.isNaN() }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            UtilityHeader(title = "统计", subtitle = "本地房源记录概览", onBack = onBack)
        }
        item {
            PrototypeCard {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricTile("房源", properties.size.toString(), Modifier.weight(1f))
                    MetricTile("看房", viewings.size.toString(), Modifier.weight(1f))
                    MetricTile("均分", averageScore?.toInt()?.toString() ?: "-", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricTile("候选", properties.count { it.status == PropertyStatus.SHORTLISTED }.toString(), Modifier.weight(1f))
                    MetricTile("签约中", properties.count { it.status == PropertyStatus.SIGNING }.toString(), Modifier.weight(1f))
                    MetricTile("已排除", properties.count { it.status == PropertyStatus.REJECTED }.toString(), Modifier.weight(1f))
                }
            }
        }
        items(properties.take(12), key = { it.id }) { property ->
            PrototypeCard {
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(property.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "月租 ${property.rentAmount ?: "-"} · 看房 ${viewings.count { it.propertyId == property.id }} 次",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    StatusChip(property.status)
                }
            }
        }
    }
}

@Composable
private fun UtilityHeader(title: String, subtitle: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun NavHostController.go(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(graph.startDestinationId) {
            saveState = true
        }
    }
}

private val chromeRoutes = setOf("properties", "compare", "profile")

private fun BottomDestination.isSelected(route: String): Boolean =
    route == this.route

@Composable
private fun PrototypeBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.navigationBarsPadding()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                destinations.forEach { destination ->
                    val selected = destination.isSelected(currentRoute)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigate(destination.route) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                            modifier = Modifier.size(24.dp),
                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = destination.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private fun NavHostController.backOrProperties() {
    if (!popBackStack()) {
        go("properties")
    }
}
