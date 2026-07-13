package com.ebchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ebchat.data.remote.FirebaseConfig
import com.ebchat.ui.navigation.NavRoutes
import com.ebchat.ui.screens.auth.LoginScreen
import com.ebchat.ui.screens.auth.OnboardingScreen
import com.ebchat.ui.screens.auth.SignupScreen
import com.ebchat.ui.screens.main.ChatDetailScreen
import com.ebchat.ui.screens.main.ChatListScreen
import com.ebchat.ui.screens.main.GroupChatScreen
import com.ebchat.ui.screens.main.GroupsScreen
import com.ebchat.ui.screens.main.MainScreen
import com.ebchat.ui.screens.main.StoryViewerScreen
import com.ebchat.ui.screens.post.CreatePostScreen
import com.ebchat.ui.screens.post.PostDetailScreen
import com.ebchat.ui.screens.post.PostFeedScreen
import com.ebchat.ui.screens.profile.EditProfileScreen
import com.ebchat.ui.screens.profile.ProfileScreen
import com.ebchat.ui.screens.profile.UserProfileScreen
import com.ebchat.ui.screens.search.SearchScreen
import com.ebchat.ui.screens.settings.AboutScreen
import com.ebchat.ui.screens.settings.BlockedUsersScreen
import com.ebchat.ui.screens.settings.MutedUsersScreen
import com.ebchat.ui.screens.settings.NotificationsScreen
import com.ebchat.ui.screens.settings.PrivacyScreen
import com.ebchat.ui.screens.settings.SettingsScreen
import com.ebchat.ui.screens.settings.ThemesScreen
import com.ebchat.ui.theme.EBChatTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            EBChatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EBChatApp()
                }
            }
        }
    }
}

@Composable
fun EBChatApp() {
    val navController = rememberNavController()
    var showSplash by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf(NavRoutes.ONBOARDING) }

    LaunchedEffect(Unit) {
        delay(2000)
        showSplash = false
        startDestination = if (FirebaseConfig.isLoggedIn()) {
            NavRoutes.MAIN
        } else {
            NavRoutes.ONBOARDING
        }
    }

    AnimatedVisibility(
        visible = showSplash,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        SplashScreen()
    }

    if (!showSplash) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            // Auth Flow
            composable(NavRoutes.ONBOARDING) {
                OnboardingScreen(navController = navController)
            }
            composable(NavRoutes.LOGIN) {
                LoginScreen(navController = navController)
            }
            composable(NavRoutes.SIGNUP) {
                SignupScreen(navController = navController)
            }

            // Main Flow
            composable(NavRoutes.MAIN) {
                MainScreen(navController = navController)
            }
            composable(NavRoutes.HOME) {
                PostFeedScreen(navController = navController)
            }
            composable(NavRoutes.POST_FEED) {
                PostFeedScreen(navController = navController)
            }
            composable(NavRoutes.CREATE_POST) {
                CreatePostScreen(navController = navController)
            }
            composable(
                route = "post_detail/{postId}",
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""
                PostDetailScreen(navController = navController, postId = postId)
            }
            composable(NavRoutes.CHAT_LIST) {
                ChatListScreen(navController = navController)
            }
            composable(
                route = "chat_detail/{chatId}",
                arguments = listOf(navArgument("chatId") { type = NavType.StringType })
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                ChatDetailScreen(navController = navController, chatId = chatId)
            }
            composable(
                route = "group_chat/{groupId}",
                arguments = listOf(navArgument("groupId") { type = NavType.StringType })
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                GroupChatScreen(navController = navController, groupId = groupId)
            }
            composable(NavRoutes.GROUPS) {
                GroupsScreen(navController = navController)
            }
            composable(NavRoutes.STORIES) {
                StoryViewerScreen(navController = navController)
            }
            composable(
                route = "story_viewer/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                StoryViewerScreen(navController = navController, initialUserId = userId)
            }
            composable(NavRoutes.SEARCH) {
                SearchScreen(navController = navController)
            }
            composable(NavRoutes.PROFILE) {
                ProfileScreen(navController = navController)
            }
            composable(NavRoutes.EDIT_PROFILE) {
                EditProfileScreen(navController = navController)
            }
            composable(
                route = "user_profile/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                UserProfileScreen(navController = navController, userId = userId)
            }
            composable(NavRoutes.SETTINGS) {
                SettingsScreen(navController = navController)
            }
            composable(NavRoutes.THEMES) {
                ThemesScreen(navController = navController)
            }
            composable(NavRoutes.NOTIFICATIONS) {
                NotificationsScreen(navController = navController)
            }
            composable(NavRoutes.BLOCKED_USERS) {
                BlockedUsersScreen(navController = navController)
            }
            composable(NavRoutes.MUTED_USERS) {
                MutedUsersScreen(navController = navController)
            }
            composable(NavRoutes.PRIVACY) {
                PrivacyScreen(navController = navController)
            }
            composable(NavRoutes.ABOUT) {
                AboutScreen(navController = navController)
            }
        }
    }
}

@Composable
fun SplashScreen() {
    // Splash screen handled by system splash API
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primary
    ) {}
}
