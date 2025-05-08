package com.example.glowgirls.navigation

import SplashScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.glowgirls.data.journal.JournalViewModel
import com.example.glowgirls.ui.theme.screens.journal.JournalEntryScreen
import com.example.glowgirls.ui.theme.screens.screens.budget.BudgetInputScreen
import com.example.glowgirls.ui.theme.screens.screens.budget.BudgetOverviewScreen
import com.example.glowgirls.ui.theme.screens.screens.budget.SpendingScreen
import com.example.glowgirls.ui.theme.screens.screens.chat.ChatScreen
import com.example.glowgirls.ui.theme.screens.screens.cycle.CycleGraphScreen
import com.example.glowgirls.ui.theme.screens.screens.cycle.CycleScreen
import com.example.glowgirls.ui.theme.screens.screens.home.HomeScreen
import com.example.glowgirls.ui.theme.screens.screens.journal.JournalListScreen
import com.example.glowgirls.ui.theme.screens.screens.login.LoginScreen
import com.example.glowgirls.ui.theme.screens.screens.register.RegisterScreen
import com.example.glowgirls.ui.theme.screens.screens.vision.VisionBoardScreen
import com.example.glowgirls.ui.theme.screens.screens.vision.VisionScreen

import com.google.firebase.auth.FirebaseAuth

// Add these route constants for learning hub screens
//const val ROUTE_LEARNING_HUB = "learning_hub"
//const val ROUTE_COURSE_DETAIL = "course_detail/{courseId}"
//const val ROUTE_LESSON_DETAIL = "lesson_detail/{courseId}/{lessonId}"

// Add route constants for wellbeing hub
const val ROUTE_WELLBEING_HUB = "wellbeing_hub"
const val ROUTE_WELLBEING_TIP_DETAIL = "wellbeing_tip_detail/{tipId}"

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(//    val learningViewModel: LearningViewModel = viewModel()

    navController: NavHostController = rememberNavController(),
    startDestination: String = ROUTE_SPLASH
) {
    // Create ViewModels to share across screens
    val journalViewModel: JournalViewModel = viewModel()
//    val wellbeingViewModel: WellbeingViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Your existing screens
        composable(ROUTE_SPLASH) {
            SplashScreen {
                navController.navigate(ROUTE_REGISTER) {
                    popUpTo(ROUTE_SPLASH) { inclusive = true }
                }
            }
        }

        composable(ROUTE_REGISTER) {
            RegisterScreen(navController)
        }

        composable(ROUTE_LOGIN) { LoginScreen(navController) }
        composable(ROUTE_HOME) { HomeScreen(navController) }
        composable(ROUTE_CYCLE) { CycleScreen(navController) }
        composable(ROUTE_CYCLE_GRAPH) { CycleGraphScreen(navController) }
        composable(ROUTE_BUDGET_OVERVIEW) { BudgetOverviewScreen(navController) }
        composable(ROUTE_SPENDING_SCREEN) { SpendingScreen(navController) }
        composable(ROUTE_VISION) { VisionScreen(navController) }
        composable(ROUTE_VISION_BOARD) { VisionBoardScreen(navController) }
//        composable(ROUTE_LEARNING) { LearningHubScreen(navController) }
        // Inside the NavHost builder, add this composable entry:
//        composable(ROUTE_KNOWLEDGE_HUB) {
//            val auth = FirebaseAuth.getInstance()
//            if (auth.currentUser != null) {
//                val knowledgeHubViewModel: KnowledgeHubViewModel = viewModel()
//                KnowledgeHubScreen(viewModel = knowledgeHubViewModel)
//            } else {
//                LaunchedEffect(Unit) {
//                    navController.navigate(ROUTE_LOGIN) {
//                        popUpTo(ROUTE_HOME)
//                    }
//                }
//            }
//        }

        composable(ROUTE_BUDGET_INPUT) {
            BudgetInputScreen(
                navController = navController,
                onBudgetSaved = {
                    navController.navigate(ROUTE_BUDGET_OVERVIEW)
                }
            )
        }

        // Chat screen destination
        composable(ROUTE_CHAT) {
            // Check if user is authenticated
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser != null) {
                // User is logged in, show chat screen
                ChatScreen(navController)
            } else {
                // Redirect to login if not authenticated
                LaunchedEffect(Unit) {
                    navController.navigate(ROUTE_LOGIN) {
                        popUpTo(ROUTE_HOME)
                    }
                }
            }
        }

//        // Learning Hub Screens
//        composable(ROUTE_LEARNING_HUB) {
//            LearningDashboardScreen(
//                onCourseClick = { courseId ->
//                    navController.navigate("course_detail/$courseId")
//                },
//                viewModel = learningViewModel
//            )
//        }
//
//        composable(
//            route = ROUTE_COURSE_DETAIL,
//            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
//        ) { backStackEntry ->
//            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
//            CourseDetailScreen(
//                courseId = courseId,
//                onLessonClick = { lessonId ->
//                    navController.navigate("lesson_detail/$courseId/$lessonId")
//                },
//                onBackClick = {
//                    learningViewModel.clearSelections()
//                    navController.popBackStack()
//                },
//                viewModel = learningViewModel
//            )
//        }
//
//        composable(
//            route = "lesson_detail/{courseId}/{lessonId}",
//            arguments = listOf(
//                navArgument("courseId") { type = NavType.StringType },
//                navArgument("lessonId") { type = NavType.StringType }
//            )
//        ) { backStackEntry ->
//            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
//            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: return@composable
//            val auth = FirebaseAuth.getInstance()
//            if (auth.currentUser != null) {
//                LessonDetailScreen(
//                    courseId = courseId,
//                    lessonId = lessonId,
//                    onBackClick = { navController.popBackStack() },
//                    viewModel = learningViewModel
//                )
//            } else {
//                LaunchedEffect(Unit) {
//                    navController.navigate(ROUTE_LOGIN) {
//                        popUpTo(ROUTE_HOME)
//                    }
//                }
//            }
//        }

        // Journal list screen
        composable("journal_list") {
            JournalListScreen(
                viewModel = journalViewModel,
                onNavigateToEntry = { entryId ->
                    if (entryId == null) {
                        navController.navigate("journal_entry/new")
                    } else {
                        navController.navigate("journal_entry/edit/$entryId")
                    }
                }
            )
        }

        // New journal entry screen
        composable("journal_entry/new") {
            JournalEntryScreen(
                viewModel = journalViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Edit journal entry screen
        composable(
            route = "journal_entry/edit/{entryId}",
            arguments = listOf(
                navArgument("entryId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId")
            JournalEntryScreen(
                viewModel = journalViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                existingEntryId = entryId
            )
        }

        // Women's Wellbeing Hub Screens
    }
}