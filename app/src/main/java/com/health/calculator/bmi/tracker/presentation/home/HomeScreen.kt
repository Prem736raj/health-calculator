package com.health.calculator.bmi.tracker.presentation.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.health.calculator.bmi.tracker.core.navigation.Screen

import com.health.calculator.bmi.tracker.ui.components.home.*
import com.health.calculator.bmi.tracker.presentation.components.MedicalDisclaimerShort
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToBmi: () -> Unit,
    onNavigateToBmr: () -> Unit,
    onNavigateToBp: () -> Unit,
    onNavigateToWhr: () -> Unit,
    onNavigateToWater: () -> Unit,
    onNavigateToMetabolic: () -> Unit,
    onNavigateToBsa: () -> Unit,
    onNavigateToIbw: () -> Unit,
    onNavigateToCalorie: () -> Unit,
    onNavigateToHeartRate: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
    searchViewModel: HomeSearchViewModel = viewModel(),
    dailyViewModel: DailyContentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by searchViewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by searchViewModel.searchResults.collectAsStateWithLifecycle()
    val recentSearches by searchViewModel.recentSearches.collectAsStateWithLifecycle()
    val quickActions by searchViewModel.quickActions.collectAsStateWithLifecycle()
    val isRefreshing by searchViewModel.isRefreshing.collectAsStateWithLifecycle()
    
    val dailyContent by dailyViewModel.dailyContent.collectAsStateWithLifecycle()
    val favoriteQuoteIds by dailyViewModel.favoriteQuoteIds.collectAsStateWithLifecycle()

    val pullToRefreshState = rememberPullToRefreshState()
    var showScoreBreakdown by remember { mutableStateOf(false) }
    
    // Search overlay state
    var isSearchActive by remember { mutableStateOf(false) }
    
    // Handle back button when search is active
    androidx.activity.compose.BackHandler(enabled = isSearchActive || searchQuery.isNotEmpty()) {
        if (searchQuery.isNotEmpty()) {
            searchViewModel.updateSearchQuery("")
        } else {
            isSearchActive = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
            ) {
                PersonalizedGreeting(
                    userName = null, // Integration point for profile name in future prompts
                    modifier = Modifier.statusBarsPadding()
                )
                HomeSearchBar(
                    query = searchQuery,
                    onQueryChange = { 
                        searchViewModel.updateSearchQuery(it)
                        isSearchActive = it.isNotEmpty()
                    },
                    results = searchResults,
                    recentSearches = recentSearches,
                    onClearRecent = { searchViewModel.clearRecentSearches() },
                    onRemoveRecent = { searchViewModel.removeRecentSearch(it) },
                    onResultClick = { result ->
                        searchViewModel.onSearchResultClick(result)
                        isSearchActive = false
                        when (result.navigationRoute) {
                            "bmi_calculator" -> onNavigateToBmi()
                            "bmr_calculator" -> onNavigateToBmr()
                            "blood_pressure_calculator" -> onNavigateToBp()
                            "whr_calculator" -> onNavigateToWhr()
                            "water_calculator" -> onNavigateToWater()
                            "calorie_calculator" -> onNavigateToCalorie()
                            "heart_rate_calculator" -> onNavigateToHeartRate()
                            "metabolic_syndrome" -> onNavigateToMetabolic()
                            "bsa_calculator" -> onNavigateToBsa()
                            "ibw_calculator" -> onNavigateToIbw()
                            "history" -> onNavigateToHistory()
                            "profile" -> onNavigateToProfile()
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { 
                searchViewModel.refreshDashboard()
                dailyViewModel.refresh()
                // HomeViewModel doesn't have a public refresh yet, but could be added
            },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quick Actions Section
                item(span = { GridItemSpan(2) }) {
                    QuickActionsRow(
                        actions = quickActions,
                        onActionClick = { action ->
                            searchViewModel.onQuickActionClick(action)
                            when (action.route) {
                                "bmi_calculator" -> onNavigateToBmi()
                                "bmr_calculator" -> onNavigateToBmr()
                                "blood_pressure_calculator" -> onNavigateToBp()
                                "whr_calculator" -> onNavigateToWhr()
                                "water_calculator" -> onNavigateToWater()
                                "calorie_calculator" -> onNavigateToCalorie()
                                "heart_rate_calculator" -> onNavigateToHeartRate()
                                "metabolic_syndrome" -> onNavigateToMetabolic()
                                "bsa_calculator" -> onNavigateToBsa()
                                "ibw_calculator" -> onNavigateToIbw()
                            }
                        }
                    )
                }

                // Health Overview Section
                item(span = { GridItemSpan(2) }) {
                    HealthOverviewSection(
                        healthScore = uiState.healthScore,
                        quickStats = uiState.quickStats,
                        lastActivity = uiState.lastActivity,
                        onQuickStatClick = { route -> 
                            // Map route to navigation
                            when(route) {
                                "bmi_calculator" -> onNavigateToBmi()
                                "bmr_calculator" -> onNavigateToBmr()
                                "bp_calculator" -> onNavigateToBp()
                                "whr_calculator" -> onNavigateToWhr()
                                "water_calculator" -> onNavigateToWater()
                                "calorie_calculator" -> onNavigateToCalorie()
                                "heart_rate_calculator" -> onNavigateToHeartRate()
                                "metabolic_syndrome" -> onNavigateToMetabolic()
                                "bsa_calculator" -> onNavigateToBsa()
                                "ibw_calculator" -> onNavigateToIbw()
                            }
                        },
                        onLastActivityClick = { 
                            uiState.lastActivity?.let { activity ->
                                // Navigation logic for last activity
                            }
                        },
                        onViewBreakdown = { showScoreBreakdown = true },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Smart Recommendations
                item(span = { GridItemSpan(2) }) {
                    if (uiState.recommendations.isNotEmpty()) {
                        RecommendationsSection(
                            recommendations = uiState.recommendations,
                            onActionClick = { route ->
                                // Map route to navigation
                            },
                            onDismiss = { id -> viewModel.dismissRecommendation(id) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Daily Content Section
                dailyContent?.let { content ->
                    item(span = { GridItemSpan(2) }) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            DailyTipCard(tip = content.tip)
                            Spacer(modifier = Modifier.height(16.dp))
                            MotivationalQuoteCard(
                                quote = content.quote,
                                isFavorite = favoriteQuoteIds.contains(content.quote.id),
                                onFavoriteToggle = { dailyViewModel.toggleFavorite(content.quote) }
                            )
                        }
                    }
                }

                // Calculator Cards Header
                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = "Calculators",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Dynamic Calculator Cards Grid
                item(span = { GridItemSpan(2) }) {
                    CalculatorCardsGrid(
                        state = uiState.calculatorCardsState,
                        onNavigate = { route ->
                            when(route) {
                                "bmi_calculator" -> onNavigateToBmi()
                                "bmr_calculator" -> onNavigateToBmr()
                                "blood_pressure_checker" -> onNavigateToBp()
                                "whr_calculator" -> onNavigateToWhr()
                                "water_intake_calculator" -> onNavigateToWater()
                                "metabolic_syndrome_checker" -> onNavigateToMetabolic()
                                "bsa_calculator" -> onNavigateToBsa()
                                "ibw_calculator" -> onNavigateToIbw()
                                "calorie_calculator" -> onNavigateToCalorie()
                                "heart_rate_zone_calculator" -> onNavigateToHeartRate()
                            }
                        }
                    )
                }

                // Medical Disclaimer
                item(span = { GridItemSpan(2) }) {
                    MedicalDisclaimerShort(
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        
        if (showScoreBreakdown) {
            HealthScoreBreakdownSheet(
                healthScore = uiState.healthScore,
                onDismiss = { showScoreBreakdown = false }
            )
        }
    }
}
