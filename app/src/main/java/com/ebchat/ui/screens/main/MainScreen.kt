package com.ebchat.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ebchat.data.remote.FirebaseConfig
import com.ebchat.ui.screens.post.PostFeedScreen
import com.ebchat.ui.screens.profile.ProfileScreen
import com.ebchat.ui.screens.search.SearchScreen
import com.ebchat.ui.screens.settings.SettingsScreen
import com.ebchat.ui.theme.GlassSurface
import com.ebchat.ui.theme.OnlineGreen
import com.ebchat.ui.theme.PinkLight
import com.ebchat.ui.theme.PinkPrimary
import com.ebchat.ui.theme.PinkPrimaryDark
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean = false,
    open val badgeCount: Int = 0
) {
    data class Home(override val badgeCount: Int = 0) : BottomNavItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    data class Chat(override val badgeCount: Int = 0) : BottomNavItem("chat", "Chat", Icons.Filled.Chat, Icons.Outlined.Chat)
    data class Groups(override val badgeCount: Int = 0) : BottomNavItem("groups", "Groups", Icons.Filled.Group, Icons.Outlined.Group)
    data class Profile(override val badgeCount: Int = 0) : BottomNavItem("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
    data class Settings(override val badgeCount: Int = 0) : BottomNavItem("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun MainScreen(navController: NavHostController) {
    var selectedItem by remember { mutableIntStateOf(0) }
    var unreadCount by remember { mutableIntStateOf(0) }
    val userId = FirebaseConfig.getCurrentUserId()

    // Listen for unread messages
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            FirebaseConfig.chatsRef().addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var count = 0
                    snapshot.children.forEach { chatSnapshot ->
                        val unreadMap = chatSnapshot.child("unreadCount").child(userId).getValue(Int::class.java)
                        count += unreadMap ?: 0
                    }
                    unreadCount = count
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    val items = listOf(
        BottomNavItem.Home(),
        BottomNavItem.Chat().copy(badgeCount = unreadCount),
        BottomNavItem.Groups(),
        BottomNavItem.Profile(),
        BottomNavItem.Settings()
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .shadow(8.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                containerColor = GlassSurface.copy(alpha = 0.98f),
                tonalElevation = 8.dp
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (item.badgeCount > 0) {
                                        Badge(
                                            containerColor = PinkPrimary,
                                            contentColor = Color.White
                                        ) {
                                            Text(item.badgeCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (selectedItem == index) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            }
                        },
                        label = { Text(item.title) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PinkPrimary,
                            selectedTextColor = PinkPrimaryDark,
                            indicatorColor = PinkLight.copy(alpha = 0.3f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedItem == 0) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate("create_post")
                    },
                    containerColor = PinkPrimary,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Icon(Icons.Default.Add, "Create Post", tint = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedItem) {
                0 -> PostFeedScreen(navController = navController)
                1 -> ChatListScreen(navController = navController)
                2 -> GroupsScreen(navController = navController)
                3 -> ProfileScreen(navController = navController)
                4 -> SettingsScreen(navController = navController)
            }
        }
    }
}
