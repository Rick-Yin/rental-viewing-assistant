package com.rentalviewingassistant.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.rentalviewingassistant.feature.compare.CompareScreen
import com.rentalviewingassistant.feature.profile.ProfileScreen
import com.rentalviewingassistant.feature.property.PropertyScreen
import com.rentalviewingassistant.feature.signing.SigningScreen
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
    BottomDestination("properties", "房源", Icons.Outlined.HomeWork),
    BottomDestination("compare", "对比", Icons.AutoMirrored.Outlined.CompareArrows),
    BottomDestination("signing", "签约", Icons.Outlined.AssignmentTurnedIn),
    BottomDestination("profile", "我的", Icons.Outlined.Person),
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

    Scaffold(
        topBar = {
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
        },
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = route == destination.route,
                        onClick = { navController.go(destination.route) },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) },
                    )
                }
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
                activeSigningSession = uiState.activeSigningSession,
                onCreateProperty = viewModel::createProperty,
                onOpenViewing = { propertyId ->
                    viewModel.selectProperty(propertyId)
                    navController.go("viewing")
                },
                onToggleCandidate = viewModel::toggleCandidate,
                onBuildExport = viewModel::buildExport,
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
                onUpdateChecklist = viewModel::updateChecklistResult,
                onAddMaterial = viewModel::addSigningMaterial,
                onFinishSigning = viewModel::finishSigning,
                onBuildAiShare = { propertyId, sessionId ->
                    viewModel.buildAiShare(ChecklistStage.SIGNING, propertyId, sessionId)
                },
                onImportAi = { propertyId, sessionId, raw ->
                    viewModel.importAiResult(ChecklistStage.SIGNING, propertyId, sessionId, raw)
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

private fun NavHostController.go(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(graph.startDestinationId) {
            saveState = true
        }
    }
}
